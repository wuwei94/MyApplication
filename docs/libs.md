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
| lib_widget | 自定义 Widget 库 | 自定义控件集合 |
| lib_ninepatch | NinePatch 图片处理 | NinePatch 工具 |

---

## 库详情

### lib_okhttp（OkHttp 封装）

对 OkHttp 网络库的封装，提供统一的 HTTP 请求接口。

- 权限：`INTERNET`
- 依赖：OkHttp

### lib_retrofit（Retrofit 封装）

对 Retrofit 网络库的封装，提供 RESTful API 接口定义和调用。

- 权限：`INTERNET`
- 依赖：Retrofit

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

### lib_widget（自定义 Widget 库）

自定义 UI 控件集合，供其他模块复用。

- 依赖：无

### lib_ninepatch（NinePatch 图片处理）

NinePatch 图片处理工具库。

- 依赖：无
