# 库封装层

`libs/` 只提供可复用能力，不包含 Activity。使用方式、行为约束和选型建议应记录在对应专题文档中，避免在本文件维护重复的 API 手册。

## 库总览

| 模块 | 职责 | 主要依赖 | 详细文档 |
|------|------|----------|----------|
| `lib_okhttp` | OkHttp Client DSL、配置适配和 Interceptor | OkHttp | [Android 网络请求封装](network.md) |
| `lib_retrofit` | Retrofit DSL、响应转换和统一异常 | Retrofit、`lib_okhttp` | [Android 网络请求封装](network.md) |
| `lib_retrofit_rx` | Retrofit 的 RxJava3 调用与动态请求 | RxJava3、`lib_retrofit` | [Android 网络请求封装](network.md) |
| `lib_ktor` | 固定使用 OkHttp Engine 的 Ktor 项目封装 | Ktor、OkHttp | [Android 网络请求封装](network.md) |
| `lib_volley` | 轻量级 HTTP 请求封装 | Volley | - |
| `lib_httpurl` | `HttpURLConnection` 基础请求工具 | Android SDK | - |
| `lib_download` | 断点续传、下载进度和响应流写入 | - | - |
| `lib_websocket_okhttp` | OkHttp WebSocket 客户端 | OkHttp WebSocket | - |
| `lib_websocket_java` | Java-WebSocket 客户端与服务端 | Java-WebSocket | - |
| `lib_netty` | Netty TCP 客户端与服务端 | Netty | - |
| `lib_nanohttpd` | 嵌入式 HTTP Server | NanoHTTPD | - |
| `lib_imageloader` | 图片加载封装 | Glide | - |
| `lib_eventbus` | 事件总线封装 | EventBus | - |
| `lib_widget` | 自定义 Widget 集合 | Android View | - |
| `lib_ninepatch` | NinePatch 图片处理 | Android Graphics | - |
| `network_dio` | Flutter Dio 请求封装 | Dio | [package README](../basic/basic_flutter_libs/network_dio/README.md) |
| `network_http` | Flutter `package:http` 请求封装 | `package:http` | [package README](../basic/basic_flutter_libs/network_http/README.md) |

## 通用约定

- 正式业务的 Client、Retrofit 和 API Service 由 Hilt 或 ServiceLocator 管理；库内命名缓存仅用于无 DI 的简单场景和 Demo。
- 网络库负责传输、协议适配和错误转换，不持有业务 Token，也不固化 Token 刷新策略。
- 上传下载文件的落盘、断点续传等业务归 `lib_download`；网络库只保留其自身已有的传输能力。
- 新代码优先使用明确的公开类型；标记为 `@Deprecated` 的兼容类型不得继续扩展，也不能在没有迁移方案时直接删除。
- Android 网络数据模型按实际组件传参需求直接实现 `Parcelable`。`lib_okhttp.base.BaseBean` 仅用于兼容旧 `Serializable` 模型。

## 其他库说明

### lib_download

提供断点续传和进度监听，由 `DownloadFileWriter` 负责将响应流写入文件。文件系统职责不放入 Retrofit 或 Ktor。

### WebSocket、TCP 与 HTTP Server

- `lib_websocket_okhttp`：使用 OkHttp 实现 WebSocket 客户端。
- `lib_websocket_java`：使用 Java-WebSocket 实现客户端和服务端。
- `lib_netty`：提供 Netty TCP 通信示例。
- `lib_nanohttpd`：提供 `NanoHttpServer`、`NanoHttpLogger`、`ServerConfig` 和 `ServerLifecycle`。

### UI 与通用能力

- `lib_imageloader`：Glide 图片加载。
- `lib_eventbus`：EventBus 注册、注销和事件发送。
- `lib_widget`：项目自定义 View。
- `lib_ninepatch`：NinePatch 图片处理工具。
