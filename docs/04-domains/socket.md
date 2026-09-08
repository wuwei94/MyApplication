# WebSocket 与 TCP Socket 长连接开发指南

> 本文档系统梳理 `:modules:module_socket` 的长连接通信体系：**WebSocket（应用层）**与 **TCP Socket（传输层）**两大类，OkHttp / Java-WebSocket / Netty 三种实现 × 普通回调 / RxJava / Coroutines Flow 三种调用形态，以及基于 `basic_server` 的**本地服务端打靶**方案。模块 Activity 清单见 [模块总览](../05-catalog/modules.md)。

---

## 一、核心体系全景

### 1. 技术线与调用形态矩阵

| 技术线 | 协议/定位 | 封装库 | 普通版本 | RxJava 版本 | Coroutines Flow 版本 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **OkHttp WebSocket** | 应用层 WebSocket，基于 OkHttp 客户端 | `lib_websocket_okhttp` | `OkHttpWebSocketClient` + `OkHttpWebSocketClientListener` | `OkHttpWebSocketClientRx` + `OkHttpWebSocketObserver` | `OkHttpWebSocketClientFlow` |
| **Java-WebSocket** | 应用层 WebSocket，轻量自包含库（客户端 + 服务端） | `lib_websocket_java` | `JavaWebSocketClient` + `JavaWebSocketClientListener` | `JavaWebSocketClientRx` + `JavaWebSocketRxObserver` | `JavaWebSocketClientFlow` |
| **Netty TCP** | 传输层 TCP，高性能 NIO 框架 | `lib_netty`（`NettyClient`） | `NettyClient`（Channel + 回调） | 模块内 Rx 封装示例 | 模块内 Flow 封装示例 |

> 三形态设计意图：**回调版**展示库原生监听器；**Rx 版**把回调桥接为 `Observable`（配合页面 `CompositeDisposable` 统一释放）；**Flow 版**把回调桥接为冷流/热流（`collect` 逐条消费、`cancel` 断开），三种形态共享同一套 `Info` 事件密封类，便于横向对照学习。

### 2. 本地服务端打靶（`basic_server`）

联调不依赖外部公网服务器：`basic_server` 提供三类嵌入式服务端，由客户端 Activity 通过 **ARouter ServerService 契约**按需启动，再直连 `127.0.0.1` 打靶：

| 服务端 | 实现 | 默认端口 | 服务契约路由 |
| :--- | :--- | :---: | :--- |
| Java-WebSocket 服务端 | `basic_server/javaws/JavaWebSocketServer` | **5566** | `RouterPath.Server.JavaWebSocket` |
| Netty TCP 服务端 | `basic_server/netty/NettyWebSocketServer` | **5567** | `RouterPath.Server.Netty` |
| NanoHTTPD 服务端 | `basic_server/nano/NanoServer` | HTTP（联调用） | `RouterPath.Server.Nano` |

服务端通过 `basic_server/service/*ServiceImpl`（均标注 `@Route`）暴露，例如 `JavaWebSocketServerServiceImpl`、`NettyServerServiceImpl`。客户端示例页面先 `ARouter.getInstance().build(...).navigation()` 拉起服务，再以 `127.0.0.1:端口` 连接；`module_socket` 的 `NetworkUtils` 可获取本机局域网 IP 供真机互联。

---

## 二、WebSocket（应用层）

### 1. OkHttp WebSocket（`lib_websocket_okhttp`）

OkHttp 原生支持 WebSocket（一次 HTTP Upgrade 握手后升级为全双工帧通信），适合已有 OkHttp 依赖栈的轻量实时场景。

* **普通版本**：`OkHttpWebSocketClient.connect(url, listener)`，事件经 `OkHttpWebSocketClientListener` 回调（打开 / 收到消息 / 关闭 / 失败），消息用 `send()` 发送；
* **Rx 版本**：`OkHttpWebSocketClientRx` + `OkHttpWebSocketObserver`，把生命周期回调桥接为 `Observable<OkHttpWebSocketInfo>` 事件流；
* **Flow 版本**：`OkHttpWebSocketClientFlow`（object 单例风格），`collect { info -> ... }` 消费事件、`send(url, message)` 发送、`cancel(url)` 主动断开；
* **事件模型**：`OkHttpWebSocketInfo` 密封类统一承载 `Open` / 消息事件 / `Closed`，三种形态共用同一套事件语义。

对应示例路由（`RouterPath.Socket`）：`OkHttpWebSocketClient` / `OkHttpWebSocketClientRx` / `OkHttpWebSocketClientFlow`。

### 2. Java-WebSocket（`lib_websocket_java`）

Java-WebSocket 是轻量自包含的 WebSocket 实现（不依赖 OkHttp 栈），库内同时提供**客户端**与**服务端**能力：

* 客户端三形态与 OkHttp 线一一对应：`JavaWebSocketClient`（Listener 回调）、`JavaWebSocketClientRx` + `JavaWebSocketRxObserver`（`RxOnSubscribe` 桥接）、`JavaWebSocketClientFlow`；
* 服务端：`lib_websocket_java/server/JavaWebSocketServer`（带 `JavaWebSocketServerListener`），与 `basic_server` 中 5566 端口的实现同源；
* 选择考量：当项目未引入 OkHttp 或希望一套依赖同时满足客户端与服务端场景时优先。

对应示例路由：`JavaWebSocketClient` / `JavaWebSocketClientRx` / `JavaWebSocketClientFlow`。

---

## 三、TCP Socket（传输层）：Netty（`lib_netty`）

Netty 是异步事件驱动的 NIO 网络框架，适合高并发、长连接、自定义协议（TCP）场景：

* **核心机制**：`EventLoopGroup` 线程模型、`Channel` / `ChannelPipeline` / `ChannelHandler` 责任链处理收发；
* **封装入口**：`lib_netty` 的 `NettyClient.connect(host, port, ...)` 建立连接，暴露连接状态与消息收发回调；
* **三种形态**：普通版（Channel 回调）与模块内 Rx / Flow 封装示例，便于在传统回调与响应式编程风格之间对比；
* **打靶路径**：Netty 客户端示例启动 `basic_server` 的 Netty 服务端（5567）后直连 `127.0.0.1`，代码内演示了服务端 `ServerService` 启动 + 客户端连接的最小闭环。

对应示例路由：`NettyTcpSocketClient` / `NettyTcpSocketClientRx` / `NettyTcpSocketClientFlow`。

---

## 四、方案对比与选型矩阵

| 维度 | OkHttp WebSocket | Java-WebSocket | Netty TCP |
| :--- | :--- | :--- | :--- |
| **协议层次** | 应用层 WebSocket | 应用层 WebSocket | 传输层 TCP（自定义协议自由） |
| **依赖关系** | 复用 OkHttp 栈 | 轻量自包含 | 独立 NIO 框架，较重 |
| **服务端能力** | 无 | 内置服务端 | 框架级双向（客户端/服务端） |
| **典型场景** | App 内轻量实时推送、与既有 OkHttp 栈共存 | 无 OkHttp 依赖的独立 WS 客户端/服务端 | 高并发长连接、私有二进制协议、IM/推送底座 |
| **调用形态** | Listener / Rx / Flow 三态齐全 | Listener / Rx / Flow 三态齐全 | 普通 + 模块内 Rx / Flow 示例 |

**选型建议**：应用层标准 WebSocket 通信优先 OkHttp 线（工程依赖最顺）；需要内置服务端或轻依赖时选 Java-WebSocket；底层长连接、自定义 TCP 协议、高吞吐场景直接上 Netty。调用形态上——简单演示用 Listener；需要组合操作符/自动释放用 Rx；倾向结构化并发与生命周期安全（`repeatOnLifecycle`）用 Flow。

---

## 五、工程示例索引

入口：`SocketMainActivity`（路由 `/Socket/Main`）。完整 Activity 与路由清单见 [modules.md](../05-catalog/modules.md)；封装库职责与 API 详见 `libs/` 下 `lib_websocket_okhttp`、`lib_websocket_java`、`lib_netty`。
