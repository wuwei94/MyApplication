# lib_retrofit 重构说明

> 本文档记录 lib_retrofit 模块的完整重构过程，包括每个文件的改动内容和原因。

## 重构目标

将 lib_retrofit 从「全局单例 + Builder 链式修改全局状态」的旧架构，重构为「Kotlin DSL + 多实例 + 调用方管理生命周期」的新架构，与 lib_okhttp 保持一致的风格。

## 改动总览

```
新增文件：RetrofitDsl、RetrofitBuilder（DSL）、RxRetrofitDsl、RequestBuilder
修改文件：RetrofitResponse、RetrofitConverterFactory、
         build.gradle.kts（注释）、RetrofitActivity、RetrofitRxJavaActivity
标记废弃：无
```

---

## RxJava 拆分（lib_retrofit_rx）

将 RxJava3 相关代码从 lib_retrofit 拆分到独立的 `lib_retrofit_rx` 模块，使核心 Retrofit 模块不再依赖 RxJava。

### 拆分到 lib_retrofit_rx 的文件

| 文件 | 说明 |
|------|------|
| `RxRetrofit.kt` | Rx 请求类 |
| `api/Api.java` | Retrofit API 接口（返回 Single） |
| `builder/RequestBuilder.kt` | Rx 请求构建器 |
| `callback/RetrofitLiveDataCallback.kt` | DisposableSingleObserver 回调 |
| `callback/RetrofitFileCallback.kt` | 文件下载回调 |
| `callback/RetrofitResponseCallback.kt` | 响应回调 |
| `function/HttpResultFunction.kt` | Rx 异常转换 |
| `function/RxRetrofitFunction.kt` | Rx 泛型转换 |

### 解耦改动

- `RetrofitBuilder.kt`：移除 `RxJava3CallAdapterFactory.create()` 硬编码，CallAdapter 改为按需配置
- CallAdapter 改为由 `RetrofitBuilder.callAdapter()` 按实例配置
- `RxRetrofitDsl.kt`：新增 `rxRetrofit {}`，由 Rx 专用入口自动安装 `RxJava3CallAdapterFactory`

### 网络契约验证

`lib_retrofit` 与 `lib_retrofit_rx` 均通过 MockWebServer 进行真实请求验证：

- Retrofit 验证强类型响应、直接对象与集合转换，以及非 2xx 原始错误体保留。
- Retrofit Rx 验证 `Single` 的真实强类型转换，以及 `ApiException` 对 HTTP 状态码和服务端错误消息的保留。
- `RequestBuilder.observeOn(scheduler)` 允许后台任务和 JVM 测试覆盖默认 Android 主线程观察器，不依赖全局 `RxAndroidPlugins` 状态。
- `RequestBuilder` 仅由带显式类型的 `RxRetrofit.builder` 创建，`buildSingle()` 会校验 `api(...)` 已配置；`ServerResultFunction` 继续用于现有 Article 数据源的业务结果校验。
- Converter 仅对 `RetrofitResponse<T>` 处理业务信封；直接 `Call<User>`、`Call<List<User>>` 等声明按原类型反序列化。
- 未显式注入 Client 时，每个 Retrofit 创建独立的默认 OkHttpClient；需要复用时由应用层显式注入并管理 Client。

### 依赖关系

```
lib_retrofit_rx  ──api──>  lib_retrofit  ──api──>  lib_okhttp
     │
     ├── retrofit-adapter-rxjava3
     ├── rxandroid
     └── rxlifecycle
```

### 下游模块

需要 Rx 支持的模块同时依赖 `lib_retrofit` 和 `lib_retrofit_rx`：
- `modules/module_okhttp`
- `libs/lib_download`
- `basic/basic_repo`

---

## 新增文件

### 1. `RetrofitDsl.kt` — Kotlin DSL 入口

**改动**：新建

**原因**：提供 `retrofit {}` DSL 函数，每次调用创建独立的 Retrofit 实例；默认 API 工厂复用一个内部 Retrofit。正式业务实例交给 Hilt/ServiceLocator 管理，同时为无 DI 场景保留可选的按名称缓存。

**核心设计**：
- `retrofit { }` — 每次创建新实例
- `cachedRetrofit("name") { }` — 无 DI 场景按名称缓存，同名原子初始化
- `getCachedRetrofit()` / `removeCachedRetrofit()` / `clearCachedRetrofits()` — 查询和清理命名缓存
- `createRetrofit(Consumer)` — Java 兼容 API（每次新建）
- `cachedRetrofit(name, Consumer)` — Java 兼容 API（按名称缓存）
- `@DslMarker` — 防止嵌套作用域误用

**使用示例**：
```kotlin
// 每次新建
val r = retrofit {
    baseUrl("https://api.example.com/")
    client(okHttpClient { logging() })
}

// 无 DI 的简单场景
val cached = cachedRetrofit("api") {
    baseUrl("https://api.example.com/")
}
```

**Java 使用**：
```java
Retrofit r = RetrofitDsl.createRetrofit(b -> {
    b.baseUrl("https://api.example.com/");
    b.client(client);
});

Retrofit cached = RetrofitDsl.cachedRetrofit("api", b -> {
    b.baseUrl("https://api.example.com/");
});
```

正式业务应由 Hilt 或 ServiceLocator 同时持有 OkHttpClient、Retrofit 和 API Service。命名缓存只作为无 DI/简单 Demo 的便捷能力，不用于重复注册已由应用层容器管理的同一组实例。

---

### 2. `RetrofitBuilder.kt`（DSL 版）— Retrofit DSL Builder

**改动**：新建（替换原 RetrofitBuilder）

**原因**：封装 `Retrofit.Builder`，提供 `baseUrl()`、`client()`、`converter()`、`callAdapter()`、`code()`、`message()`、`raw {}` 等 DSL 方法。

**核心设计**：
- 未配置 Client 时创建独立的默认 OkHttpClient；`client(...)` 注入的外部实例不会被覆盖或关闭
- 默认使用 `RetrofitConverterFactory`，支持自定义 code/message 字段名
- Factory 校验 `RetrofitResponse` 的 data 泛型类型，并把完整 `Type` 交给响应 Converter 识别 `RetrofitResponse<T>`；Converter 只整理标准信封 JSON，具体类型转换与字段校验交给 Gson `TypeAdapter`
- `converter(factory)` — 设置自定义 Converter 并替换默认 `RetrofitConverterFactory`，避免两个全类型 Converter 相互遮挡
- `callAdapter(factory)` — 设置单个 CallAdapter，重复配置时最后一次生效（如需 RxJava 支持，通过 lib_retrofit_rx 配置）
- `build()` 不修改内部 Builder，重复构建不会累积 ConverterFactory 或 CallAdapterFactory
- `raw { }` — 逃生口，可直接操作底层 `Retrofit.Builder`；Client 必须通过 `client(...)` 配置，以保留所有权跟踪

---

### 3. `RequestBuilder.kt` — 请求参数 Builder

**改动**：从原 `RetrofitBuilder<T>` 重命名而来

**原因**：原 `RetrofitBuilder<T>` 与新的 DSL `RetrofitBuilder` 类名冲突。重命名为 `RequestBuilder<T>` 更准确地表达其职责（构建请求参数）。

**改动内容**：
- 类名 `RetrofitBuilder<T>` → `RequestBuilder<T>`
- 构造入口收口到 `RxRetrofit.builder<T>()` / `builder(Type)`，移除默认 `JsonElement` 响应类型
- `buildSingle()` 前必须通过 `api(...)` 配置请求路径

---

## 修改文件

### 4. `RetrofitResponse.kt` — 修复 Gson 单例

**改动**：`Gson()` 提取为 `companion object` 中的共享实例。

**原因**：`Gson` 线程安全，每次 `string()` 调用都 new 一个浪费内存。

---

### 5. `RxRetrofit.kt` — 使用 RequestBuilder

**改动**：`RetrofitBuilder<T>` → `RequestBuilder<T>`

---

### 6. `RetrofitConverterFactory.java` — 显式响应字段配置

**改动**：
- `create()` 和 `create(Gson)` 标记 `@Deprecated`
- 新增 `create(String code, String message)` 和 `create(Gson, String, String)` 作为推荐用法
- code/message 由 Factory 构造参数显式传入

**原因**：DSL Builder 需要直接传入 code/message，而非从全局配置读取。

---

### 7. `RetrofitActivity.kt` — 使用 DSL

**改动**：`Retrofit.Builder()` 替换为 `retrofit { baseUrl(...) }`。

---

### 8. `RetrofitRxJavaActivity.kt` — 使用 DSL

**改动**：`Retrofit.Builder() + RetrofitConverterFactory.create()` 替换为 `retrofit { baseUrl(...) }`。

---

## 当前架构

```
┌─── lib_retrofit ────────────────┐
│ RetrofitDsl.kt                  │
│ retrofit { } / cachedRetrofit() │
│ createApi()                     │
│ RetrofitBuilder (DSL)           │
│ RetrofitConverterFactory        │
│ ApiException / ExceptionHandler │
│ RetrofitResponse / State        │
└───────────────────────────────────┘

                                  ┌─── lib_retrofit_rx ─────────────┐
                                  │ RxRetrofit.kt                   │
                                  │ api/Api.java（Single 返回值）    │
                                  │ builder/RequestBuilder.kt       │
                                  │ callback/RetrofitResponseCallback│
                                  │ callback/RetrofitLiveDataCallback│
                                  │ callback/RetrofitFileCallback    │
                                  │ function/HttpResultFunction      │
                                  │ function/RxRetrofitFunction      │
                                  └─────────────────────────────────┘
```
