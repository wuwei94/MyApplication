# Server-Sent Events（SSE）流式传输开发指南

> 本文档系统梳理 `:modules:module_sse` 的 SSE 流式传输体系：面向现代 AI 大模型（DeepSeek 官方 API）「POST + SSE 逐 Token 流式响应」的标准协议落地，覆盖 **OkHttp SSE**（普通 / RxJava / Coroutines Flow）与 **Ktor SSE**（普通 / Coroutines Flow）五条示例链路，以及统一的 LLM 流解析工具。模块 Activity 清单见 [模块总览](../05-catalog/modules.md)。

---

## 一、核心概念与协议

**Server-Sent Events (SSE)** 是一种基于 HTTP 的服务端单向推送协议，与 WebSocket 的差异在于：

| 维度 | SSE | WebSocket |
| :--- | :--- | :--- |
| 方向 | 服务端 → 客户端单向（客户端用普通 HTTP POST 发送请求） | 全双工双向 |
| 载体 | 普通 HTTP 长连接，文本分帧（`data:` / `[DONE]`） | TCP 升级后的二进制/文本帧 |
| 复杂度 | 极低，标准 HTTP 库即可实现 | 需要握手与帧协议 |
| 典型场景 | AI 大模型流式对话、行情推送、进度通知 | 实时双向交互（聊天、协同） |

**AI 对话的标准姿势**：客户端向 `POST /chat/completions`（DeepSeek 兼容 OpenAI 协议）提交 `{"model": "deepseek-chat", "messages": [...], "stream": true}`，服务端通过 SSE 逐行推送 `data: {choices:[{delta:{content:"..."}}]}`，以 `data: [DONE]` 收尾。完整格式转换由 `LlmStreamParser` 统一处理。

---

## 二、封装库与调用形态

### 1. OkHttp 线（`lib_sse_okhttp`）

| 调用形态 | 封装类 | 事件模型 | 对应示例路由 |
| :--- | :--- | :--- | :--- |
| 普通回调 | `OkHttpSseClient` + `OkHttpSseListener` | `OkHttpSseInfo`（Open / Event / Closed） | `OkHttpSseClient` |
| RxJava | `OkHttpSseClientRx` | Observable 事件流 | `OkHttpSseClientRx` |
| Coroutines Flow | `OkHttpSseClientFlow` | `collect` 逐 Token 消费 | `OkHttpSseClientFlow` |

### 2. Ktor 线（`lib_sse_ktor`）

| 调用形态 | 封装类 | 事件模型 | 对应示例路由 |
| :--- | :--- | :--- | :--- |
| 普通回调 | `KtorSseClient` + `KtorSseListener` | `KtorSseInfo`（Open / Event / Closed） | `KtorSseClient` |
| Coroutines Flow | `KtorSseClientFlow` | Flow 逐事件消费 | `KtorSseClientFlow` |

> 两套库都遵循本工程 `lib_*` 的统一外壳约定：`OkHttpSseInfo` / `KtorSseInfo` 密封类定义事件，`OkHttpSseLogger` / `KtorSseLogger` 负责日志，示例页通过 `cancel(url)` 结束会话。

---

## 三、LLM 流解析（`LlmStreamParser`）

`module_sse` 的 `utils/LlmStreamParser` 把「大模型流式协议」固化为三个纯函数，客户端/服务端解析共用：

* `DEFAULT_PROMPT`：默认对话示例提示词；
* `buildChatRequestBody(prompt, model = "deepseek-chat")`：构造 OpenAI / DeepSeek 格式的 `stream: true` POST 请求体（JSON）；
* `parseDeltaContent(data)`：把 SSE 事件行中的 `data: {...}` 解析为增量文本 `delta.content`（忽略空事件与 `[DONE]`），供 UI 直接追加渲染。

页面侧约定：收到 `[DONE]` 即视为完整结束（Rx 版触发 `onComplete()`，Flow 版流终止），打字机式逐字上屏逻辑参见 [markdown.md](markdown.md)（流式打字机与语法容错引擎）。

---

## 四、示例链路（DeepSeek 对话）

五条示例链路共享同一目标：`Constants.Url_DeepSeek` + `model = deepseek-chat`，差异只在「如何消费 SSE 事件」：

| 示例路由 | 封装 | 流式上屏方式 |
| :--- | :--- | :--- |
| `OkHttpSseClient` | OkHttp 原始 Listener 回调 | 回调内逐 Token `appendLog` 追加 |
| `OkHttpSseClientRx` | OkHttp → Observable | `subscribe` 消费，页面销毁自动释放 |
| `OkHttpSseClientFlow` | OkHttp → Flow | `collect` 消费，`repeatOnLifecycle` 安全收集 |
| `KtorSseClient` | Ktor 原始 Listener 回调 | 主线程回调逐 Token 上屏 |
| `KtorSseClientFlow` | Ktor → Flow | `collect` 逐事件消费 |

**约定**：接入真实服务时仅需替换 `Constants.Url_DeepSeek` 与鉴权头（示例使用 DeepSeek 官方兼容接口，需要填入自己的 API Key）；请求体构造、增量解析与结束判定一律走 `LlmStreamParser`，不要在各页面重复实现协议细节。

---

## 五、工程示例索引

入口：`SseMainActivity`（路由 `/SSE/Main`）。完整 Activity 与路由清单见 [modules.md](../05-catalog/modules.md)；封装库位于 `libs/lib_sse_okhttp` 与 `libs/lib_sse_ktor`。
