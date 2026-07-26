# lib_okhttp 重构说明

> 本文档记录 lib_okhttp 模块的完整重构过程，包括每个文件的改动内容和原因。

## 重构目标

将 lib_okhttp 从「全局单例 + Builder 链式修改全局状态」的旧架构，重构为「Kotlin DSL + 多实例 + 可插拔接口」的新架构。

## 改动总览

```
新增文件：OkHttpDsl、OkHttpClientBuilder、LoggingConfig、CookieStore、MemoryCookieStore、NetworkCheck、
         InterceptorBaseUrl、InterceptorDownloadProgress、InterceptorUploadProgress、
         ProgressRequestBody、ProgressResponseBody、CompatTimeout、CompatRetry、CompatConnectionPool、CompatInterceptor
修改文件：CompatLogging、CompatCookieJar、CompatCache、CompatHttpsSSL、
         InterceptorCache、InterceptorLogging、InterceptorCookie、
         FormatPrinterImpl、ParseUtils、BaseData、MediaType、Header、
         HttpLogger、NetworkUtils、RequestBody、build.gradle.kts、OkHttpHelperActivity
删除文件：OkHttpHelper、OkHttpConfig
```

---

## 新增文件

### 1. `OkHttpDsl.kt` — Kotlin DSL 入口

**改动**：新建

**原因**：提供 `okHttpClient {}` DSL 函数，每次调用创建独立的 `OkHttpClient` 实例。同时提供 `cachedClient {}` 按名称缓存复用。

**核心设计**：
- `okHttpClient { }` — 每次创建新实例
- `cachedClient("name") { }` — 按名称缓存，同名只创建一次，后续复用
- `getCachedClient("name")` — 获取已缓存的 Client，不存在抛异常
- `removeCachedClient("name")` — 移除指定缓存
- `clearCachedClients()` — 清空所有缓存
- `OkHttpClientBuilder` — 封装 `OkHttpClient.Builder`，提供 `timeout()`、`logging()`、`cookieJar()` 等 DSL 方法
- `raw { }` — 逃生口，可直接操作底层 `OkHttpClient.Builder`
- `createClient(Consumer)` — Java 兼容 API（每次新建）
- `cachedClient(name, Consumer)` — Java 兼容 API（缓存复用）
- `@DslMarker` — 防止嵌套作用域误用

**使用示例**：
```kotlin
// 每次新建
val client = okHttpClient {
    timeout(30)
    logging()
    loggingFormat()
    addInterceptor(myInterceptor)
}

// 缓存复用，同名返回同一个实例
val apiClient = cachedClient("api") {
    timeout(30)
    logging()
}
val same = cachedClient("api") { timeout(30) }
assert(apiClient === same) // true
```

**Java 使用**：
```java
OkHttpClient client = OkHttpDsl.cachedClient("api", b -> {
    b.timeout(30);
    b.logging(HttpLoggingInterceptor.Level.BASIC);
});
```

---

### 2. `LoggingConfig.kt` — 日志配置 sealed interface

**改动**：新建

**原因**：用 sealed interface 让日志模式成为类型安全的可选配置。

**两种模式**：
- `Basic` — OkHttp 官方 `HttpLoggingInterceptor`
- `None` — 不添加日志

---

### 3. `CookieStore.kt` — Cookie 存储接口

**改动**：新建

**原因**：Cookie 存储硬编码为内存或 SharedPreferences，无法替换。接口化后用户可自定义存储策略（MMKV、DataStore 等）。

**内容**：
- `CookieStore` 接口 — `save(url, cookies)` / `load(url)`
- `MemoryCookieStore` — 仅内存存储，应用重启后丢失

---

### 4. `NetworkCheck.kt` — 网络检测接口

**改动**：新建

**原因**：`InterceptorCache` 直接调用 `NetworkUtils.isConnected(context)`，硬编码 Android API，不可测试。接口化后可注入测试实现。

**内容**：
- `NetworkCheck` — `fun interface`，`isConnected(): Boolean`
- `AndroidNetworkCheck` — 基于 `ConnectivityManager` 的默认实现，兼容 API 23+

---

### 5. `InterceptorDownloadProgress.kt` — 下载进度拦截器

**改动**：新建

**原因**：基于 Interceptor 实现，对调用方完全透明，无需手动包装 RequestBody。

**核心设计**：
- Lambda 接口：`(url, currentBytes, totalBytes) -> Unit`
- 内部用 `ForwardingSource` 包装响应体，监听 `read()` 进度
- 当 `contentLength()` 返回 -1 时，`totalBytes` 为 -1，调用方需处理

---

### 6. `InterceptorUploadProgress.kt` — 上传进度拦截器

**改动**：新建

**原因**：提供与下载进度对称的上传进度监听，基于 Interceptor 实现。

**关键适配**：OkHttp 5.x 中 `Request.Builder.body` 变为 `internal` property，无法直接设置。通过 `request.newBuilder().method(request.method, wrappedBody)` 绕过。

---

### 7. `InterceptorBaseUrl.kt` — 动态 BaseUrl 重定向拦截器

**改动**：新建

**原因**：实现 `Header.RETROFIT_URL_REDIRECT` 常量描述的功能——通过 Request Header 动态切换 BaseUrl。

**工作原理**：拦截请求 → 读取 `Retrofit-Url-Redirect` Header → 替换请求 URL → 移除 Header → 发送。

---

### 8. `CompatTimeout.kt` — 超时配置

**改动**：新建

**原因**：将超时配置逻辑封装到 compat 层，`OkHttpClientBuilder` 委托调用。

---

### 9. `CompatRetry.kt` — 重试配置

**改动**：新建

**原因**：将重试配置逻辑封装到 compat 层，`OkHttpClientBuilder` 委托调用。

---

### 10. `CompatConnectionPool.kt` — 连接池配置

**改动**：新建

**原因**：将连接池配置逻辑封装到 compat 层，`OkHttpClientBuilder` 委托调用。

---

### 11. `CompatInterceptor.kt` — 拦截器配置

**改动**：新建

**原因**：将拦截器添加逻辑封装到 compat 层，`OkHttpClientBuilder` 委托调用。

---

### 12. `ProgressRequestBody.kt` — 上传进度包装

**改动**：新建

**原因**：从 `InterceptorUploadProgress` 中提取，包装请求体实现上传进度监听。

---

### 13. `ProgressResponseBody.kt` — 下载进度包装

**改动**：新建

**原因**：从 `InterceptorDownloadProgress` 中提取，包装响应体实现下载进度监听。

---

## 修改文件

### 14. `OkHttpConfig.kt` — 全局配置

**改动**：已移除

---

### 15. `OkHttpHelper.kt`

**改动**：已移除

---

### 16. `CompatLogging.kt` — 日志适配

**改动**：
- `setBasicLog()` + `setFormatLog()` 合并为 `applyLogging(builder, LoggingConfig)` + `applyBasicLog()` + `applyFormatLog()`
- 用 `when` 表达式分发四种日志模式
- 恢复拦截器类型说明注释（addInterceptor vs addNetworkInterceptor、Level.BODY 卡死警告）

**原因**：单一入口，类型安全，消除两个 boolean 标志的歧义。

---

### 17. `CompatCookieJar.kt` — Cookie 适配

**改动**：
- `cookieJar(builder)` 内部改为 `cookieJar(builder, MemoryCookieStore())`
- 新增 `cookieJar(builder, store: CookieStore)` 重载
- 用 `OkHttpCookieJarAdapter` 适配 OkHttp `CookieJar` 接口

**原因**：Cookie 存储从硬编码改为可插拔 `CookieStore` 接口。

---

### 18. `InterceptorCache.kt` — 缓存拦截器

**改动**：
- 构造参数从 `Context` 改为 `NetworkCheck`
- `NetworkUtils.isConnected(mContext)` 改为 `networkCheck.isConnected()`
- 恢复 `buildRequest()` 和 `buildResponse()` 方法的中文注释

---

### 20. `InterceptorLogging.kt` — 日志拦截器

**改动**：
- `FormatPrinterImpl` 改为构造注入：`FormatPrinterImpl(filters)` 替代 `FormatPrinterImpl` 单例 + `setFilters()`
- 移除多余 try-catch（catch 后直接 throw）

**原因**：消除共享可变状态竞态，每个拦截器实例持有独立的 FormatPrinter。

---

### 21. `FormatPrinterImpl.kt` — 格式化日志

**改动**：
- 从 `object` 改为 `class`，构造时注入 `mFilters: List<String>`
- 移除 `setFilters()` 方法
- `const val` 移入 `companion object`
- 移除未使用的 `CONTENT_TYPE_TAG` / `CONTENT_LENGTH_TAG`

---

### 22. `InterceptorCookie.kt` — Cookie 拦截器

**改动**：
- 重构为使用 `CookieStore` 接口存取 Cookie
- 简化无效 while 循环为单次 `response.headers("set-cookie")` 查询
- 移除 `saveCookie()` 中 url 为空的 NPE 死代码检查

---

### 23. `InterceptorProgress.kt` — 下载进度拦截器

**改动**：标记 `@Deprecated`，指向 `InterceptorDownloadProgress`

---

### 24. `ResponseProgressBody.kt` / `RequestProgressBody.kt` — 进度 Body

**改动**：均标记 `@Deprecated`，分别指向 `InterceptorDownloadProgress` / `InterceptorUploadProgress`

> 注：`body/` 包保留供学习参考，展示 OkHttp 装饰器模式的实现方式。

---

### 25. `ResponseProgressListener.kt` / `RequestProgressListener.kt` — 进度接口

**改动**：均标记 `@Deprecated`，指向对应 Interceptor 的 lambda

---

### 26. `CompatHttpsSSL.kt` — SSL 适配

**改动**：移除 `ignoreSSLForHttpsURLConnection()` 方法

**原因**：该方法设置 `HttpsURLConnection` 全局默认值，影响整个进程内所有 HTTPS 连接（包括其他库）。仅保留 per-client 的 `ignoreSSLForOkHttp()`。

---

### 27. `CompatInterceptor.kt` — 拦截器适配

**改动**：重构为 `addInterceptor(builder, interceptor)` + `addNetworkInterceptor(builder, interceptor)`，由 `OkHttpClientBuilder` 委托调用

---

### 28. `HttpLogger.kt` — 日志工具

**改动**：TAG 改为硬编码 `"OkHttp"`，不再依赖 OkHttpConfig

---

### 29. `NetworkUtils.kt` — 网络工具

**改动**：
- 替换废弃 `activeNetworkInfo` API 为 `NetworkCapabilities`（API 23+）
- 标记 `@Deprecated`，指向 `NetworkCheck`

---

### 30. `ParseUtils.kt` — 解析工具

**改动**：移除未使用的 `bodyHasUnknownEncoding()` 和 `Buffer.isProbablyUtf8()` 私有方法

**原因**：死代码清理。

---

### 31. `BaseData.kt` — 基础数据类

**改动**：`Gson()` 提取为 `companion object` 中的共享实例

**原因**：`Gson` 是线程安全的，每次 `string()` 调用都 new 一个浪费内存。

---

### 32. `MediaType.kt` — 媒体类型

**改动**：`MULTIPART` 去掉无效的 `charset=utf-8`

**原因**：`multipart/form-data` 不支持 `charset` 参数，发送的 Content-Type 不符合 RFC 规范。

---

### 33. `Header.kt` — Header 常量

**改动**：恢复 `RETROFIT_URL_REDIRECT` 常量及注释

**原因**：常量描述的功能（动态 BaseUrl 重定向）已通过 `InterceptorBaseUrl` 实现，需要保留常量。

---

### 34. `RequestBody.kt`（builder） — 请求体构建器

**改动**：整个类标记 `@Deprecated`，`addListener()` / `buildForm()` / `buildMultipart()` / `buildJson()` 均标记 `@Deprecated`

> 注：`body/` 和 `builder/` 包保留供学习参考，展示 OkHttp 请求体构建的多种方式。

---

### 35. `build.gradle.kts` — 构建配置

**改动**：
- 移除未使用的 `dataBinding = true`
- 修正注释 `SharedPreferences.edit` → `core-ktx extension`
- `google.gson` 和 `okhttp.logging` 从 `api` 改为 `implementation`

---

### 36. `OkHttpActivity.kt` / `OkHttpHelperActivity.kt` — 示例页面

**改动**：
- 继承改为 `BasicResponseActivity`，使用 `appendLog` / `appendFormatLog` 显示结果
- `OkHttpActivity` 新增 JSON POST 示例（`toRequestBody` + `application/json`）
- `OkHttpHelperActivity` 使用 DSL 方式创建 `OkHttpClient`（无配置）
- 移除对 `OkHttpHelper` 的依赖

---

## 架构变化对比

```
重构前                              重构后
┌─────────────────────┐           ┌──────────────────────────┐
│ OkHttpHelper        │           │ OkHttpDsl.kt             │
│ (全局单例, 链式API)  │           │ okHttpClient { } 每次新建 │
├─────────────────────┤           │ cachedClient("n") { } 缓存│
│ OkHttpConfig        │           │ getCachedClient("n")     │
│ (全局单例, Builder)  │           │ removeCachedClient("n")  │
│ mShowBasicLog       │           ├──────────────────────────┤
│ mShowFormatLog      │           │ OkHttpClientBuilder       │
├─────────────────────┤           │ 所有操作委托 Compat 层     │
│ 进度: 装饰器模式     │           ├──────────────────────────┤
│ RequestProgressBody │           │ compat/                   │
│ ResponseProgressBody│           │ CompatTimeout             │
│ 需手动构建请求       │           │ CompatRetry               │
├─────────────────────┤           │ CompatConnectionPool      │
│ Cookie: 硬编码       │           │ CompatInterceptor         │
│ Memory / SP 二选一  │           │ CompatLogging             │
├─────────────────────┤           │ CompatCookieJar           │
│ 网络检测: 硬编码     │           │ CompatCache               │
│ NetworkUtils        │           │ CompatHttpsSSL            │
├─────────────────────┤           │ CompatProxy               │
│ 日志: boolean 控制   │           ├──────────────────────────┤
│ showBasicLog        │           │ Interceptor 模式           │
│ showFormatLog       │           │ InterceptorDownload       │
└─────────────────────┘           │ InterceptorUpload         │
                                  │ InterceptorBaseUrl        │
                                  ├──────────────────────────┤
                                  │ 可插拔接口                  │
                                  │ CookieStore / NetworkCheck │
                                  ├──────────────────────────┤
                                  │ logging() / loggingFormat()│
                                  │ Java 兼容: createClient() │
                                  └──────────────────────────┘
```
