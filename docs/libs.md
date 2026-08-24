# 库封装层

`libs/` 只提供可复用能力，不包含 Activity。使用方式、行为约束和选型建议应记录在对应专题文档中，避免在本文件维护重复的 API 手册。

## 库总览

| 模块 | 职责 | 主要依赖 | 详细文档 |
|------|------|----------|----------|
| `lib_okhttp` | OkHttp Client DSL、配置适配和 Interceptor | OkHttp | [Android 网络请求封装](network.md) |
| `lib_retrofit` | Retrofit DSL、响应转换和统一异常 | Retrofit、`lib_okhttp` | [Android 网络请求封装](network.md) |
| `lib_retrofit_rx` | Retrofit 注解接口的 RxJava3 调用、默认调度与回调 | RxJava3、`lib_retrofit` | [Android 网络请求封装](network.md) |
| `lib_rx_request` | 运行时动态 Retrofit 请求 Builder | `lib_retrofit_rx` | [Android 网络请求封装](network.md) |
| `lib_ktor` | 固定使用 OkHttp Engine 的 Ktor 项目封装 | Ktor、OkHttp | [Android 网络请求封装](network.md) |
| `lib_volley` | 轻量级 HTTP 请求封装 | Volley | - |
| `lib_httpurl` | `HttpURLConnection` 基础请求工具 | Android SDK | - |
| `lib_rx_download` | Retrofit + Rx 单/批量下载、业务级并发、断点续传和聚合进度 | `lib_retrofit_rx` | [文件上传与下载](transfer.md) |
| `lib_rx_upload` | Retrofit + Rx 链式 Multipart 单/多文件上传和进度 | `lib_retrofit_rx` | [文件上传与下载](transfer.md) |
| `lib_websocket_okhttp` | OkHttp WebSocket 客户端 | OkHttp WebSocket | - |
| `lib_websocket_java` | Java-WebSocket 客户端与服务端 | Java-WebSocket | - |
| `lib_netty` | Netty TCP 客户端与服务端 | Netty | - |
| `lib_nanohttpd` | 嵌入式 HTTP Server | NanoHTTPD | - |
| `lib_imageloader` | 图片加载封装 | Glide、Coil | - |
| `lib_eventbus` | 事件总线封装 | EventBus | - |
| `lib_widget` | 自定义 Widget 集合 | Android View | - |
| `lib_ninepatch` | NinePatch 图片处理 | Android Graphics | - |
| `lib_network_dio` | Flutter Dio 请求封装 | Dio | [package README](../flutter/flutter_libs/lib_network_dio/README.md) |
| `lib_network_http` | Flutter `package:http` 请求封装 | `package:http` | [package README](../flutter/flutter_libs/lib_network_http/README.md) |
| `lib_storage` | Flutter 键值存储封装（内核可切换，默认 Hive） | Hive、shared_preferences | [package README](../flutter/flutter_libs/lib_storage/README.md) |

## 通用约定

- 正式业务的 Client、Retrofit 和 API Service 由 Hilt 或 ServiceLocator 管理；库内命名缓存仅用于无 DI 的简单场景和 Demo。
- 网络库负责传输、协议适配和错误转换，不持有业务 Token，也不固化 Token 刷新策略。
- 下载落盘与断点续传归 `lib_rx_download`，Multipart 文件上传归 `lib_rx_upload`；通用网络库只保留协议层传输能力。
- Retrofit 注解接口的 Rx 基础能力归 `lib_retrofit_rx`；只有运行时动态 URL、方法或请求体 Builder 归 `lib_rx_request`。
- 这些库仅由当前仓库消费，公开 API 调整采用一次性 breaking change：同步修改全部调用方和文档，不保留旧类型或 `@Deprecated` 过渡入口。
- Android 网络数据模型按实际组件传参需求直接实现 `Parcelable`。`lib_okhttp.base.BaseBean` 仅用于兼容旧 `Serializable` 模型。
- `lib_retrofit` 的 `ApiException.message` 始终提供非空展示文本；原始异常消息为空时使用统一的 `DEFAULT_MESSAGE`，示例页无需自行增加“未知错误”兜底方法。

## Flutter 与 Retrofit 普通请求契约

`lib_network_dio` 与 `lib_network_http` 只对齐 `lib_retrofit + Coroutines/Rx` 的普通请求，上传、下载、断点续传和任务队列不在该契约中。

| 能力 | Flutter Dio/http | Android Retrofit |
|------|------------------|------------------|
| 业务响应 | `NetworkResponse<T>(code/message/data)` | `RetrofitResponse<T>(code/message/data)` |
| JSON 字段 | `errorCode/errorMsg/data` | `errorCode/errorMsg/data` |
| 信封解析 | 按底层库的 JSON 响应解析业务信封 | 识别业务信封后交给 Gson `TypeAdapter` |
| 成功判断 | `code == 0` | `code == 0` |
| 业务失败 | 调用方检查，或 `requireSuccess()` 先构造 `ServerResultException` 再转异常 | 协程调用方检查；Rx 默认链通过 `ServerResultFunction` 转异常 |
| 统一异常 | `NetworkException(code/message/cause)` | `ApiException(code/message/cause)` |
| 错误码 | HTTP 状态码；`1000–1004` 表示未知/连接/超时/SSL/解析 | 相同 |
| 取消 | `CancelToken` / `CancelableOperation` 向上传播 | 协程取消 / Rx dispose 向上传播 |
| 固定 API | 业务层使用类和强类型方法组织 | Retrofit 注解接口 |

Flutter 的 `Future` 对应单次 suspend/`Single` 请求。线程调度、Parcelable、LiveData 回调和 Retrofit 命名缓存属于平台或框架差异，不在 Flutter package 中复制。认证、Cookie、缓存、重试、重定向和代理通过 Dio `Interceptor`/adapter 或注入的 `http.Client` 扩展，包装层不持有业务 Token。

## 其他库说明

### lib_retrofit_rx

标准 Retrofit Rx 能力按 `api/callback/function` 分层：`api` 提供 `rxRetrofit()`、`createRxApi()` 和 `Single` 默认网络策略，`callback` 提供 `ResponseCallback` 等订阅回调，`function` 负责传输异常和业务结果转换。包路径已经包含 `retrofit.rx`，类型名不再重复添加 `Rx` 前缀。

### lib_rx_request

`RxRequest.builder<T>()` 提供 GET/POST/PUT/PATCH/DELETE、Form、JSON、Raw Body 和 POST/PUT/PATCH Multipart 动态请求。Multipart 文本字段使用 `addMultipartField(s)`，文件使用 `addFile()`；上传进度仍由 `lib_rx_upload` 负责。模块以 `com.example.william.my.core.rx.request` 为根包，并依赖 `lib_retrofit_rx` 复用 Retrofit 实例、Rx 调度和异常转换。

```text
com.example.william.my.core.rx.request/
├── RxRequest.kt                      # 对外请求入口
├── api/                               # Retrofit 动态请求接口
├── builder/                           # 链式参数配置与请求构建
├── config/                            # 不可变请求配置
├── function/                          # Retrofit 响应转换
└── method/                            # HTTP 方法
```

调用方只从根包使用 `RxRequest`；其余目录按构建、配置、响应转换和 Retrofit 接口职责组织。

### lib_rx_download

通过根包中的 `RxDownload.builder()` 创建不可变单次下载请求；`RxDownloadManager.builder()` 创建可复用的业务级下载管理器。单任务代码与 `lib_rx_upload` 按 `builder/callback/config/exception/model/request` 对称分层。`RxDownloadCallback<P, R>` 同时服务单任务和下载队列，两种场景都通过请求对象的 `build().subscribeWith(callback)` 使用；原始 `Single`、`Flowable` 和队列任务事件作为高级入口保留。下载由 Retrofit `@Streaming GET` 执行，使用临时文件和 `If-Range` 校验资源身份；非 2xx 响应会在 `DownloadHttpException` 中保留服务端错误体。`RxDownloadManager` 为不同业务配置共享 `Retrofit` 与跨队列总并发数，默认并发 3。下载特有的批量执行能力集中在 `queue` 目录，旧 `DownloadTask` 状态机和兼容入口已移除。

### lib_rx_upload

通过 `RxUpload.builder()` 创建固定使用 Retrofit `POST @Body` 的不可变 Multipart 上传请求，支持通过 `addFile()` 或 `addFiles()` 添加单/多文件、表单字段、Header 和整体请求体进度，成功结果统一为 `UploadResult`。`RxUploadCallback` 与下载回调一样只描述业务状态，由请求对象负责适配 RxJava 订阅；库会为上传派生关闭连接失败重试的 Retrofit，其他可能重放请求的机制由调用方控制。详细调用与行为约束见[文件上传与下载](transfer.md)。

### WebSocket、TCP 与 HTTP Server

- `lib_websocket_okhttp`：使用 OkHttp 实现 WebSocket 客户端。
- `lib_websocket_java`：使用 Java-WebSocket 实现客户端和服务端。
- `lib_netty`：提供 Netty TCP 通信示例。
- `lib_nanohttpd`：提供 `NanoHttpServer`、`NanoHttpLogger`、`ServerConfig` 和 `ServerLifecycle`。

### UI 与通用能力

- `lib_imageloader`：图片加载封装（IImageLoader 接口 + Glide / Coil 内核）。
- `lib_eventbus`：EventBus 注册、注销和事件发送。
- `lib_widget`：项目自定义 View。
- `lib_ninepatch`：NinePatch 图片处理工具。
