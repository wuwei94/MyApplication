# lib_mqtt

MyApplication 的 Flutter Demo Catalog 使用的 MQTT 客户端封装库，与 Android `lib_mqtt` 系列结构对齐。

公共 API 从 `package:lib_mqtt/lib_mqtt.dart` 导出，不依赖 `flutter_demo`。

## 核心契约

- `MqttClientManager` 为全局单例（`instance` 与工厂构造返回同一实例），业务侧统一通过 `MqttClientManager.instance` 收发消息。
- 连接使用 MQTT 3.1.1，回调统一通过 `MqttClientListener` 抛出（`onConnectSuccess` / `onConnectionLost` / `onMessageArrived` / `onError`）。
- `onConnectSuccess(bool reconnect)`：`reconnect` 为 true 表示自动重连成功，false 表示首次连接成功，与 Android `MqttClientListener` 对齐。
- 所有回调在 Dart 事件循环（主 isolate）中执行，可直接更新 UI，无需切线程。
- `connect` 支持可选 `username` / `password` 鉴权（如阿里云 IoT 一机一密，签名由调用方自行计算后传入）。
- `cleanSession` 默认 true；物联网设备建议 false 以接收离线期间下发的消息。
- `keepAlivePeriod` 默认 60 秒（阿里云 IoT 要求 30~1200 秒，EMQX 无此下限）。
- 底层基于 mqtt_client 包，使用 `MqttServerClient`（TCP）；浏览器端 WebSocket 场景不在本包范围内。

## 基本用法

```dart
final manager = MqttClientManager.instance;

await manager.connect(
  host: 'broker.emqx.io',
  port: 1883,
  listener: _listener,
);

manager.subscribe('mqtt/example', qos: 1);
manager.publish('mqtt/example', 'Hello MQTT!', qos: 1);
manager.disconnect();
```
