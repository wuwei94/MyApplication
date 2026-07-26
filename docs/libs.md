# 库封装层（libs/）

> 对第三方库进行封装，提供统一的 API 接口，不包含 Activity。

## 库总览

| 模块 | 职责 | 封装的库 |
|------|------|---------|
| lib_okhttp | 网络库封装 | OkHttp |
| lib_retrofit | 网络库封装 | Retrofit |
| lib_volley | 网络库封装 | Volley |
| lib_ktor | 网络库封装 | Ktor |
| lib_imageloader | 图片加载库封装 | Glide |
| lib_eventbus | 事件总线封装 | EventBus |
| lib_download | 下载功能封装 | 自定义下载管理器 |
| lib_websocket_okhttp | OkHttp WebSocket 封装 | OkHttp WebSocket |
| lib_websocket_java | Java-WebSocket 封装 | Java-WebSocket |
| lib_netty | Netty TCP 封装 | Netty |
| lib_widget | 自定义 Widget 库 | 自定义控件集合 |
| lib_ninepatch | NinePatch 图片处理 | NinePatch 工具 |
| lib_nanohttpd | HTTP 服务器封装 | NanoHTTPD |

---

## 库详情

### lib_okhttp（OkHttp 封装）

对 OkHttp 网络库的封装，提供 Kotlin DSL 风格的配置 API，支持多实例、可插拔日志、进度监听等。

- 权限：`INTERNET`、`ACCESS_NETWORK_STATE`
- 依赖：OkHttp、OkHttp Logging Interceptor
- 包名：`com.example.william.my.core.okhttp`

#### Kotlin DSL 快速上手

```kotlin
import com.example.william.my.core.okhttp.okHttpClient
import com.example.william.my.core.okhttp.cachedClient

// 每次创建独立的 client 实例
val apiClient = okHttpClient {
    timeout(30)                                    // 统一超时 30 秒
    retryOnConnectionFailure(true)                 // 失败重试
    logging()                                      // 官方日志
    loggingFormat()                                // 自定义格式化日志
}

// 按名称缓存，同名只创建一次，后续复用
val cachedApiClient = cachedClient("api") {
    timeout(30)
    logging()
    loggingFormat(filters = listOf("/health"))     // 过滤指定 URL 后缀
}
// 再次调用返回同一个实例
val same = cachedClient("api") { timeout(30) }
assert(cachedApiClient === same) // true
```

#### 日志配置

```kotlin
// OkHttp 官方日志（默认 BASIC 级别）
logging()
logging(Level.BODY)                               // 自定义级别

// 自定义格式化日志（边框、对齐、耗时）
loggingFormat()                                    // 无过滤
loggingFormat(filters = listOf("/api/ping"))       // 过滤指定 URL 后缀
```

#### 其他配置

```kotlin
val client = okHttpClient {
    ignoreSSL()                                              // 忽略 SSL 证书校验（仅调试用）
    noProxy()                                                // 禁用代理
    cookieJar()                                              // 启用 Cookie 管理
    cache(app, dirName = "http_cache", dirSize = 50L * 1024L * 1024L)  // 启用缓存
    addInterceptor(CustomInterceptor())                      // 添加自定义拦截器
    addNetworkInterceptor(InterceptorDownloadProgress { url, cur, total ->
        Log.d("Download", "$url: $cur/$total")               // 下载进度监听
    })
    addNetworkInterceptor(InterceptorUploadProgress { cur, total ->
        Log.d("Upload", "$cur/$total")                       // 上传进度监听
    })
    raw {                                                    // 高级：直接操作 OkHttpClient.Builder
        dns(CustomDns())
    }
}
```

#### 请求示例

```kotlin
// FormBody
val formBody = FormBody.Builder()
    .add("username", "admin")
    .add("password", "123456")
    .build()

val request = Request.Builder()
    .url("https://api.example.com/login")
    .post(formBody)
    .build()

client.newCall(request).enqueue(callback)

// JSON Body
val json = JSONObject()
    .put("username", "admin")
    .put("password", "123456")

val jsonBody = json.toString()
    .toRequestBody("application/json; charset=utf-8".toMediaType())

val jsonRequest = Request.Builder()
    .url("https://api.example.com/login")
    .post(jsonBody)
    .build()

// MultipartBody（文件上传）
val multipartBody = MultipartBody.Builder()
    .setType(MultipartBody.FORM)
    .addFormDataPart("file", "photo.jpg", file.asRequestBody("image/jpeg".toMediaType()))
    .build()
```

### lib_retrofit（Retrofit 封装）

对 Retrofit 网络库的封装，提供 Kotlin DSL 风格的配置 API，支持多实例、命名缓存、RxJava 集成。

- 权限：`INTERNET`
- 依赖：Retrofit、Retrofit Gson Converter、Retrofit Scalars Converter、Retrofit RxJava3 Adapter、RxAndroid、RxLifecycle
- 包名：`com.example.william.my.core.retrofit`

#### Kotlin DSL 快速上手

```kotlin
import com.example.william.my.core.retrofit.retrofit
import com.example.william.my.core.retrofit.cachedRetrofit

// 每次创建独立的 Retrofit 实例
val r = retrofit {
    baseUrl("https://api.example.com/")
    client(okHttpClient { timeout(30); logging() })
}

// 按名称缓存，同名只创建一次，后续复用
val apiRetrofit = cachedRetrofit("api") {
    baseUrl("https://api.example.com/")
    client(okHttpClient { timeout(30); logging() })
}
```

#### 创建 API 实例

```kotlin
// 使用 RetrofitHelper 便捷方法
val api = RetrofitHelper.buildApi(NetworkApi::class.java)

// 使用 DSL 创建的 Retrofit 实例
val api = r.create(NetworkApi::class.java)
```

#### RxJava 请求示例

```kotlin
RxRetrofit.builder<JsonElement>()
    .api("user/login")
    .addParam("username", "admin")
    .addParam("password", "123456")
    .post()
    .setProvider(lifecycleOwner)
    .buildSingle()
    .subscribe(object : RetrofitResponseCallback<JsonElement>() {
        override fun onResponse(response: JsonElement?) { /* handle success */ }
        override fun onFailure(e: ApiException) { /* handle error */ }
    })
```

### lib_volley（Volley 封装）

对 Volley 网络库的封装，提供轻量级 HTTP 请求。

- 依赖：Volley

### lib_ktor（Ktor 封装）

对 Ktor 客户端库的封装，提供 Kotlin 协程友好的网络请求。

- 权限：`INTERNET`
- 依赖：Ktor

### lib_imageloader（图片加载库封装）

对 Glide 图片加载库的封装，提供统一的图片加载接口。

- 依赖：Glide

### lib_eventbus（EventBus 封装）

对 EventBus 事件总线库的封装，提供事件注册和发送接口。

- 依赖：EventBus

### lib_download（下载功能封装）

自定义下载功能封装，支持断点续传和进度监听。

- 权限：`ACCESS_NETWORK_STATE`
- 依赖：无

### lib_websocket_okhttp（OkHttp WebSocket 封装）

对 OkHttp WebSocket 的封装，提供客户端长连接通信接口。

- 权限：`ACCESS_NETWORK_STATE`
- 依赖：OkHttp
- 包名：`com.example.william.my.core.okhttpws`

### lib_websocket_java（Java-WebSocket 封装）

对 Java-WebSocket 库的封装，提供客户端和服务端 WebSocket 通信接口。

- 权限：`ACCESS_NETWORK_STATE`、`INTERNET`
- 依赖：Java-WebSocket
- 包名：`com.example.william.my.core.javaws`

### lib_netty（Netty TCP 封装）

对 Netty 网络框架的封装，提供 TCP 客户端和服务端通信接口。

- 权限：`ACCESS_NETWORK_STATE`、`INTERNET`
- 依赖：Netty
- 包名：`com.example.william.my.core.netty`

### lib_widget（自定义 Widget 库）

自定义 UI 控件集合，供其他模块复用。

- 依赖：无

### lib_ninepatch（NinePatch 图片处理）

NinePatch 图片处理工具库。

- 依赖：无

### lib_nanohttpd（HTTP 服务器封装）

对 NanoHTTPD 轻量级 HTTP 服务器的封装，提供统一的服务启动、停止和配置接口。不包含业务逻辑，业务处理由调用方重写 `serve()` 方法实现。

- 权限：`INTERNET`、`ACCESS_NETWORK_STATE`
- 依赖：NanoHTTPD
- 包名：`com.example.william.my.core.nanohttpd`
- 核心类：
  - `NanoHttpServer` — 服务端基类（启动/停止/静态文件服务/MIME 类型检测/标准 HTTP 响应）
  - `NanoHttpLogger` — 日志工具（支持 debug 开关控制）
  - `ServerConfig` — 服务配置（端口、超时）
  - `ServerLifecycle` — 生命周期回调接口
