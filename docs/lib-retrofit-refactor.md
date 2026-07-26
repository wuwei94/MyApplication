# lib_retrofit 重构说明

> 本文档记录 lib_retrofit 模块的完整重构过程，包括每个文件的改动内容和原因。

## 重构目标

将 lib_retrofit 从「全局单例 + Builder 链式修改全局状态」的旧架构，重构为「Kotlin DSL + 多实例 + 命名缓存」的新架构，与 lib_okhttp 保持一致的风格。

## 改动总览

```
新增文件：RetrofitDsl、RetrofitBuilder（DSL）、RequestBuilder
修改文件：RetrofitHelper、RetrofitResponse、RetrofitConverterFactory、
         build.gradle.kts（注释）、RetrofitActivity、RetrofitRxJavaActivity
标记废弃：RetrofitConfig
删除文件：无（旧文件保留并标记废弃）
```

---

## 新增文件

### 1. `RetrofitDsl.kt` — Kotlin DSL 入口

**改动**：新建

**原因**：提供 `retrofit {}` DSL 函数，每次调用创建独立的 Retrofit 实例。同时提供 `cachedRetrofit {}` 按名称缓存复用。

**核心设计**：
- `retrofit { }` — 每次创建新实例
- `cachedRetrofit("name") { }` — 按名称缓存，同名只创建一次，后续复用
- `getCachedRetrofit("name")` — 获取已缓存的 Retrofit，不存在抛异常
- `removeCachedRetrofit("name")` — 移除指定缓存
- `clearCachedRetrofits()` — 清空所有缓存
- `createRetrofit(Consumer)` — Java 兼容 API（每次新建）
- `cachedRetrofit(name, Consumer)` — Java 兼容 API（缓存复用）
- `@DslMarker` — 防止嵌套作用域误用

**使用示例**：
```kotlin
// 每次新建
val r = retrofit {
    baseUrl("https://api.example.com/")
    client(okHttpClient { logging() })
}

// 缓存复用，同名返回同一个实例
val api = cachedRetrofit("api") {
    baseUrl("https://api.example.com/")
    client(okHttpClient { timeout(30); logging() })
}
val same = cachedRetrofit("api") { baseUrl("...") }
assert(api === same) // true
```

**Java 使用**：
```java
Retrofit r = RetrofitDsl.cachedRetrofit("api", b -> {
    b.baseUrl("https://api.example.com/");
    b.client(client);
});
```

---

### 2. `RetrofitBuilder.kt`（DSL 版）— Retrofit DSL Builder

**改动**：新建（替换原 RetrofitBuilder）

**原因**：封装 `Retrofit.Builder`，提供 `baseUrl()`、`client()`、`converter()`、`callAdapter()`、`code()`、`message()`、`raw {}` 等 DSL 方法。

**核心设计**：
- 默认使用全局兼容配置（baseUrl、OkHttpClient、RxJava3CallAdapterFactory）
- 默认使用 `RetrofitConverterFactory`，支持自定义 code/message 字段名
- `converter(factory)` — 覆盖默认 Converter
- `callAdapter(factory)` — 覆盖默认 CallAdapter
- `raw { }` — 逃生口，可直接操作底层 `Retrofit.Builder`

---

### 3. `RequestBuilder.kt` — 请求参数 Builder

**改动**：从原 `RetrofitBuilder<T>` 重命名而来

**原因**：原 `RetrofitBuilder<T>` 与新的 DSL `RetrofitBuilder` 类名冲突。重命名为 `RequestBuilder<T>` 更准确地表达其职责（构建请求参数）。

**改动内容**：
- 类名 `RetrofitBuilder<T>` → `RequestBuilder<T>`
- 其余逻辑不变

---

## 修改文件

### 5. `RetrofitHelper.kt` — 重构为 DSL 委托

**改动**：
- 移除 `mRetrofit` 全局缓存和 `createRetrofit()` 私有方法
- `retrofit()` 委托给 `cachedRetrofit("default") {}`
- 移除 `baseUrl()` / `client()` / `converter()` / `callAdapter()` 链式方法
- 保留 `buildApi()` 和 `buildSingle()` 便捷方法

**原因**：消除全局单例，Retrofit 实例创建委托给 DSL。

---

### 6. `RetrofitConfig.kt` — 标记废弃

**改动**：整个 `object RetrofitConfig` 标记 `@Deprecated`，指向 `RetrofitDsl`。

---

### 4. `RetrofitResponse.kt` — 修复 Gson 单例

**改动**：`Gson()` 提取为 `companion object` 中的共享实例。

**原因**：`Gson` 线程安全，每次 `string()` 调用都 new 一个浪费内存。

---

### 5. `RxRetrofit.kt` — 使用 RequestBuilder

**改动**：`RetrofitBuilder<T>` → `RequestBuilder<T>`

---

### 6. `RetrofitConverterFactory.java` — 解耦 RetrofitConfig

**改动**：
- `create()` 和 `create(Gson)` 标记 `@Deprecated`
- 新增 `create(String code, String message)` 和 `create(Gson, String, String)` 作为推荐用法
- 不再依赖 `RetrofitConfig` 获取 code/message

**原因**：DSL Builder 需要直接传入 code/message，而非从全局配置读取。

---

### 7. `RetrofitActivity.kt` — 使用 DSL

**改动**：`Retrofit.Builder()` 替换为 `retrofit { baseUrl(...) }`。

---

### 8. `RetrofitRxJavaActivity.kt` — 使用 DSL

**改动**：`Retrofit.Builder() + RetrofitConverterFactory.create()` 替换为 `retrofit { baseUrl(...) }`。

---

## 架构变化对比

```
重构前                              重构后
┌─────────────────────┐           ┌──────────────────────────┐
│ RetrofitConfig      │           │ RetrofitDsl.kt            │
│ (全局单例, Builder)  │           │ retrofit { } 每次新建      │
│ mBaseUrl            │           │ cachedRetrofit("n") { }   │
│ mOkHttpClient       │           │ getCachedRetrofit("n")    │
│ mConverterFactory   │           │ removeCachedRetrofit("n") │
│ mCallAdapterFactory │           ├──────────────────────────┤
├─────────────────────┤           │ RetrofitBuilder (DSL)     │
│ RetrofitHelper      │           │ baseUrl() / client()      │
│ (全局单例, lazy)     │           │ converter() / callAdapter │
│ mRetrofit           │           │ code() / message()        │
│ 链式方法             │           │ raw { } 逃生口            │
├─────────────────────┤           ├──────────────────────────┤
│ RetrofitBuilder     │           │ RequestBuilder (请求参数)  │
│ (请求参数, 链式API)  │           │ api() / get() / post()    │
│ Method 枚举          │           │ Method 枚举（不变）        │
├─────────────────────┤           ├──────────────────────────┤
│ RetrofitConverterFactory│        │ RetrofitHelper (便捷方法)  │
└─────────────────────┘           │ buildApi() / buildSingle()│
                                  │ 委托 DSL 创建 Retrofit     │
                                  ├──────────────────────────┤
                                  │ RetrofitConverterFactory  │
                                  │ create(code, message)     │
                                  │ 不再依赖全局配置            │
                                  ├──────────────────────────┤
                                  │ 废弃: RetrofitConfig      │
                                  └──────────────────────────┘
```
