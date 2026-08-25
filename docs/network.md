# Android 网络请求封装

本文档集中说明 `lib_okhttp`、`lib_retrofit`、`lib_retrofit_rx`、`lib_rx_request` 和 `lib_ktor` 的当前职责、使用入口、生命周期和功能边界。这里只描述当前代码，不记录历史重构过程。

## 依赖关系与选型

```text
lib_rx_request -----┐
lib_rx_download ----┼-> lib_retrofit_rx -> lib_retrofit -> lib_okhttp -> OkHttp
lib_rx_upload ------┘

lib_ktor -> Ktor Client + OkHttp Engine
```

| 项目方案 | 接口形式 | 异步模型 | 适用场景 |
|----------|----------|----------|----------|
| `lib_okhttp + lib_retrofit` | Retrofit 注解接口 | `Call`、suspend、Flow | 新项目的标准 Retrofit 方案 |
| `lib_okhttp + lib_retrofit + lib_retrofit_rx` | Retrofit 注解接口 | RxJava3 `Single` | 需要维护既有 RxJava 调用链的项目 |
| 上述组合 + `lib_rx_request` | 运行时动态 URL、方法和请求体 Builder | RxJava3 `Single` | 请求结构必须在运行时确定的特殊场景 |
| `lib_ktor` | Ktor 请求 DSL 和挂起扩展 | Coroutines + `Result` | 希望使用 Ktor Plugin 与结构化并发的项目 |

新业务优先选择 Retrofit suspend/Flow 或 Ktor。`lib_retrofit_rx` 主要保留存量 RxJava 能力，不作为新业务的默认方案。

## 共同约定

- HTTP 非 2xx、连接、超时、SSL 和解析错误转换为简单的 `ApiException(code/message/cause)`；`message` 始终非空，原始异常没有有效消息时统一使用 `ApiException.DEFAULT_MESSAGE`（“未知错误”）。
- `RetrofitResponse<T>` 与 `KtorResponse<T>` 使用 `errorCode`、`errorMsg`、`data`，成功码为 `0`。
- 非零业务码保留在业务响应中，由调用方检查 `isSuccess/code/message`；不要和“自动转异常”的调用链混用。
- 响应对象可通过工厂方法创建，`success(data)` 的 `data` 可空。泛型响应通过 `@RawValue` 实现 `Parcelable`，实际数据仍必须是 Parcel 支持的类型。
- Token 刷新属于业务策略。OkHttp/Retrofit 使用 `Interceptor` 或 `Authenticator`，Ktor 使用 Auth Plugin 或注入的 OkHttp 配置。
- 日志、缓存和 Cookie 默认不应携带业务状态；正式业务实例应交给 Hilt 或 ServiceLocator 管理。
- `module_http`、`module_okhttp` 和 `module_rxretrofit` 的实际请求页统一继承 `BasicResponseActivity`：需要页面说明时使用 `showDescription` 居中展示，离散响应与错误追加到内联日志，高频传输进度按 key 原位更新；模块入口页只负责路由导航。

## lib_okhttp

### 入口与生命周期

```kotlin
val client = okHttpClient {
    timeout(30)
    retryOnConnectionFailure(true)
    logging()
}

val cached = cachedClient("api") {
    timeout(30)
}
```

- `okHttpClient {}` 每次创建独立实例，资源由调用方关闭。
- `cachedClient(name) {}` 由库按名称持有，适用于简单场景。
- `removeCachedClient()` 与 `clearCachedClients()` 会移除实例，并关闭 Cache、Dispatcher 线程池和 ConnectionPool。
- `removeCachedClient()` 返回的是已关闭实例，只能用于确认被移除对象，不能继续发起请求。
- `closeResources()` 用于关闭调用方拥有的独立 Client。

### 配置与目录

| 目录 | 职责 |
|------|------|
| `builder` | `OkHttpClientBuilder` 和旧请求体 Builder |
| `compat` | 把 DSL 配置适配到 `OkHttpClient.Builder` |
| `interceptor` | 日志、缓存、Cookie、BaseUrl 和进度 Interceptor |
| `body` | 上传和下载进度 Body |
| `cookie` | `CookieStore`、内存实现及内部适配器 |
| `header` | 客户端内部控制 Header |
| `format` | 格式化日志解析与输出 |

主要能力包括分项/统一超时、连接失败重试、连接池、代理、Cookie、磁盘缓存、官方日志、格式化日志、动态 BaseUrl 和底层 Builder 扩展。

`ControlHeaders` 当前定义：

| 常量 | 实际值 | 用途 |
|------|--------|------|
| `BASE_URL_REDIRECT` | `OkHttp-Url-Redirect` | 单次请求替换 BaseUrl |
| `CACHE_ALIVE_SECONDS` | `OkHttp-Cache-Alive-Second` | 设置单次 GET 响应缓存秒数 |

这些 Header 只在客户端内部使用，发往服务器前由对应 Interceptor 删除。

上传/下载进度的新类型为 `UploadProgressRequestBody`、`DownloadProgressResponseBody`、`InterceptorUploadProgress` 和 `InterceptorDownloadProgress`。旧 `RequestBodyProgress`、`ResponseBodyProgress` 与 `InterceptorProgress` 仅作为废弃兼容入口保留。

`ignoreSSL()` 只允许 Debug 构建。格式化日志跳过 one-shot/duplex、未知长度和超过 1 MiB 的请求体，响应预览上限为 1 MiB。

## lib_retrofit

### 入口

```kotlin
val retrofit = retrofit {
    baseUrl("https://api.example.com/")
    client(client)
}

val api = createApi(NetworkApi::class.java, retrofit)
```

- `retrofit {}` 每次创建实例。
- `cachedRetrofit(name) {}`、`getCachedRetrofit()`、`removeCachedRetrofit()` 和 `clearCachedRetrofits()` 仅管理 Retrofit 引用，不拥有注入的 OkHttpClient。
- `client()` 注入由应用层管理的 OkHttpClient；需要共享连接池和线程资源时应显式注入。
- `converter()` 和 `callAdapter()` 各保存一个自定义 Factory，重复配置时最后一次生效。
- `raw {}` 用于少量未封装的 `Retrofit.Builder` 配置。

`RetrofitResponse<T>`、`ApiException`、`ExceptionHandler` 和 `ServerResultException` 位于核心 Retrofit 模块。Rx Callback 不属于该模块。

### 数据解析与信封转换

`RetrofitConverterFactory` 提供智能响应体转换能力：

- **无信封响应/直接对象**：当接口声明类型为非 `RetrofitResponse` 类型（如 `Call<User>`、`Call<List<User>>`）时，直接反序列化为目标对象/集合，不执行信封包装。
- **声明信封但返回裸数据**：当接口声明 `Call<RetrofitResponse<T>>` 且服务端返回裸数据（如顶层 JSON 数组或无 code 字段的 JSON 对象）时，自动将其包装为 `RetrofitResponse.success(data)`。
- **自定义业务状态码**：支持通过 `code("status")` 和 `message("msg")` 配置自定义字段名，自动规范化并校验状态码类型（非数值类型抛出 `JsonParseException`）。
- **标准信封**：支持标准 `errorCode`/`errorMsg`/`data` 结构解析。

`loading/LoadingTipView` 和 `LoadingTipObserver` 是现有 View/LiveData 集成，不是网络协议能力，也不要求 Ktor 对齐。

## lib_retrofit_rx

### 标准接口模式

```kotlin
val api = createRxApi(LoginApi::class.java)

api.login(username, password)
    .withNetworkDefaults(lifecycleOwner)
    .subscribe(object : ResponseCallback<LoginData>() {
        override fun onResponse(response: LoginData?) = Unit
        override fun onFailure(e: ApiException) = Unit
    })
```

`rxRetrofit {}` 自动安装 `RxJava3CallAdapterFactory`。`withNetworkDefaults()` 负责业务失败与上游异常转换、IO 订阅、主线程观察和可选生命周期绑定。

该模块按 `api/callback/function` 分层。由于包路径已经明确为 `retrofit.rx`，回调和转换类型使用 `ResponseCallback`、`HttpResultFunction`、`ServerResultFunction` 等名称，不再重复添加 `Rx` 前缀。

## lib_rx_request

包名为 `com.example.william.my.core.rx.request`，依赖 `lib_retrofit_rx` 复用标准 Rx Retrofit 能力。业务只使用动态请求时显式依赖该模块，标准 Retrofit Rx 调用方不需要引入它。

对应示例位于 `module_rxretrofit` 的 `RxRequestActivity`。

源码按职责分层：根包的 `RxRequest` 是稳定的对外入口，`api` 声明 Retrofit 动态请求接口，`builder` 负责链式构建，`config` 保存不可变请求快照，`method` 定义 HTTP 方法，`function` 负责响应数据转换。测试目录采用相同的职责划分。

### 动态请求模式

`RxRequest` 只用于 URL、HTTP 方法或请求结构必须在运行时确定的场景。固定业务 API 应继续使用 Retrofit 注解接口。动态 Builder 支持 GET/POST/PUT/PATCH/DELETE、Form、JSON、Raw Body，以及 POST/PUT/PATCH Multipart，并在执行前生成不可变配置快照；GET Body 和 GET/DELETE Multipart 会在构建阶段直接拒绝。

| 请求方式 | Builder 方法 | 数据格式 |
|----------|--------------|----------|
| GET、DELETE | `addParam()` / `addParams()` | URL Query |
| POST、PUT、PATCH | `addParam()` / `addParams()` | Form URL Encoded |
| POST、PUT、PATCH、DELETE | `addJsonObject()` / `addJsonBody()` / `addRawBody()` / `addBody()` | JSON、Raw 或自定义 `RequestBody` |
| POST、PUT、PATCH | `addMultipartField()` / `addMultipartFields()` / `addFile()` | Multipart Form |

```kotlin
RxRequest.builder<Profile>()
    .api("profiles/current")
    .put()
    .addMultipartFields(mapOf("name" to "William", "source" to "android"))
    .addFile("avatar", avatarFile)
    .setProvider(this)
    .buildSingle()
    .subscribe(object : ResponseCallback<Profile>() {
        override fun onResponse(response: Profile?) = Unit
        override fun onFailure(e: ApiException) = Unit
    })
```

JSON、Raw Body 和 Multipart 是互斥的请求体模式，最后配置的模式生效。Multipart 的文本字段必须使用 `addMultipartField(s)`；存在 Body 或 Multipart 时，`addParam(s)` 不会附加到请求中。`addFile()` 默认使用 `application/octet-stream`，也可传入实际文件 `MediaType`。`RxRequest` 不提供上传进度，需要进度、主动取消和传输结果信息时使用 `lib_rx_upload`。

### 业务错误

`RxRequest` 通过 `ResponseFunction` 检查 `RetrofitResponse.isSuccess`。非零业务码会先转换为 `ServerResultException`，再由统一异常链转换为 `ApiException`，不会进入 `ResponseCallback.onResponse()`。

## lib_ktor

### 入口

```kotlin
val client = ktorClient {
    baseUrl("https://api.example.com/")
    client(sharedOkHttpClient)
    timeout(15)
    cookies()
    cache(app)
    logging()
}

lifecycleScope.launch {
    client.getResponse<User>("users/current")
        .onSuccess { response ->
            if (response.isSuccess) {
                // 使用 response.data
            }
        }
}
```

- 内部固定使用 `HttpClient(OkHttp)`，不向业务层开放 Engine 切换。
- `KtorClient` 是原生 `HttpClient` 的类型别名，调用方负责复用并调用 `close()`。
- `client(okHttpClient)` 通过 `newBuilder()` 派生 Engine Client；注入实例不会由 Ktor 关闭。
- 默认安装 ContentNegotiation、HttpTimeout；Cookie、日志和缓存按配置启用。
- `raw {}` 用于安装 Auth 或其他项目 Plugin。
- `getResult<T>()` 等直接响应 API 返回 `Result<T>`；`getResponse<T>()` 等业务 API 返回 `Result<KtorResponse<T>>`。
- 协程取消继续向上传播，不会被包装成 `ApiException`。

`plugin` 包只保存内置 Plugin 的安装配置，统一使用 `Plugin` 前缀；`converter` 负责业务信封解析；`request` 保存常用挂起请求扩展。

Ktor 日志默认关闭，启用后默认记录 Header，并脱敏认证与 Cookie Header。缓存使用标准 `Cache-Control`，不复制 OkHttp 的内部离线缓存 Header 协议。

## 功能对比

| 能力 | OkHttp + Retrofit + Coroutines | OkHttp + Retrofit + Rx | Ktor |
|------|--------------------------------|------------------------|------|
| 传输内核 | OkHttp | OkHttp | Ktor OkHttp Engine |
| API 定义 | Retrofit 注解 | Retrofit 注解/动态 Builder | Ktor 请求 DSL |
| 异步模型 | `Call`、suspend、Flow | RxJava3 `Single` | suspend + `Result` |
| 强类型转换 | Retrofit Converter | 继承 Retrofit | ContentNegotiation + 业务 Converter |
| 统一业务响应 | `RetrofitResponse` | `RetrofitResponse` | `KtorResponse` |
| 网络异常 | `ApiException` | Rx 异常转换 | `ApiException` |
| 业务失败 | 调用方检查 | 动态请求统一转 `ApiException`；标准接口按调用链配置 | 调用方检查 |
| 超时、Cookie、缓存 | OkHttp | 继承 OkHttp | Ktor Plugin + OkHttp Cache |
| 日志 | OkHttp Interceptor | 继承 OkHttp | Ktor Logging Plugin |
| Token 刷新扩展点 | Interceptor/Authenticator | 继承 OkHttp | Auth Plugin/底层 OkHttp |
| 取消 | 协程或 `Call.cancel()` | dispose/RxLifecycle | 协程取消 |
| Java 调用 | 支持较完整 | 支持 | 以 Kotlin suspend/reified 为主 |

## 不能强行对齐的能力

| 差异 | 原因与约定 |
|------|------------|
| Retrofit 注解接口与 Ktor 请求 DSL | API 模型不同，不为 Ktor 复制 Service/CallAdapter 架构 |
| OkHttp Interceptor 与 Ktor Plugin | 安装阶段和生命周期不同，只对齐最终业务效果 |
| Rx dispose 与协程取消 | 异步模型不同，只保证请求可取消且不吞取消异常 |
| OkHttp 内部缓存 Header 与 Ktor Cache-Control | 前者是项目私有协议，Ktor 保持标准缓存语义 |
| Java API | Ktor 依赖 suspend、reified 和 typealias，不追求与 Retrofit Java API 完全相同 |
| UI 加载状态 | `LoadingTipView` 属于现有 Retrofit UI 集成，不是网络核心能力 |

## basic_repo 网络 API 分层定位与约定

`basic_repo` 提供了两套针对不同演示目标设计的基础网络 API，其分层职责与调用约定如下：

| 网络接口 | 架构分层 | 封装形式 | 主要服务模块 | 设计意图与调用约定 |
|---|---|---|---|---|
| **`ArticleApi`** | **业务架构层（协程标准 API）** | 纯净挂起函数<br>(基于 `createApi`，未装配 Rx Adapter) | `module_arch`<br>`module_jetpack` | 专为协程、Flow 与 Paging 3 协程组件（`ArticlePagingSource`, `ArticleRemoteMediator`）提供挂起请求；统一由 `ServiceLocator.provideArticleApi()` 提供。 |
| **`ArticleRxApi`** | **业务架构层（Rx 响应式 API）** | RxJava 响应式流<br>(基于 `createRxApi`，装配 `RxJava3CallAdapterFactory`) | `module_arch`<br>`module_jetpack` | 专为 MVP 回调、RxJava 响应式流与 Paging 3 Rx 组件（`ArticleRxPagingSource`, `ArticleRxRemoteMediator`）提供 Single 请求；统一由 `ServiceLocator.provideArticleRxApi()` 提供。 |
| **`NetworkApi`** | **网络通信原语层** | 裸 Retrofit 注解接口<br>(无 Repository 包装) | `module_okhttp`<br>`module_kotlin` | 专为 OkHttp/Retrofit/Rx/协程/Flow 演示底层请求能力（Call、Single、Suspend、Multipart 上传、下载）；由各演示页面动态创建调用。 |

## 验证命令

```powershell
.\gradlew.bat :libs:lib_okhttp:testDemoDebugUnitTest
.\gradlew.bat :libs:lib_retrofit:testDemoDebugUnitTest
.\gradlew.bat :libs:lib_retrofit_rx:testDemoDebugUnitTest
.\gradlew.bat :libs:lib_rx_request:testDemoDebugUnitTest
.\gradlew.bat :libs:lib_ktor:testDemoDebugUnitTest
```

网络契约测试使用 MockWebServer。修改响应转换、错误处理、缓存、Cookie、重定向或资源所有权时，应同步补充对应模块的契约测试。
