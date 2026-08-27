# lib_websocket

MyApplication 的 Flutter Demo Catalog 使用的 WebSocket 客户端封装库，与 Android `lib_websocket_java` / `lib_websocket_okhttp` 对齐。

公共 API 从 `package:lib_websocket/lib_websocket.dart` 导出，不依赖 `flutter_demo`。

## 核心契约

- `WebSocketClient` 为全局单例（`instance` 与工厂构造返回同一实例），业务侧统一通过 `WebSocketClient.instance` 使用。
- 回调统一通过 `WebSocketClientListener` 抛出（`onOpen` / `onMessage` / `onMessageBytes` / `onClose` / `onError`），与 Android `JavaWebSocketClientListener` 对齐。
- 按 url 管理多条连接（对齐 Android `JavaWebSocketClient` 的 Map 管理），`connect` / `send` / `close` / `isConnected` 均以 url 为键。
- 所有回调在 Dart 事件循环（主 isolate）中执行，可直接更新 UI，无需切线程。
- `connect` 支持 `autoReconnect`（意外断开自动重连，默认 true）与 `reconnectInterval`（默认 5 秒）；主动调用 `close(url)` 不触发重连。
- 底层基于 web_socket_channel 包，默认走 `ws://` / `wss://`；浏览器端自动使用 WebSocket 实现。

## 基本用法

```dart
final client = WebSocketClient.instance;
const url = 'wss://echo.websocket.org';

await client.connect(url: url, listener: _listener);

client.send(url, 'Hello WebSocket!');
client.close(url);
```
