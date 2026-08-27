/// MQTT 客户端回调接口，与 Android `MqttClientListener` 对齐。
///
/// 所有回调均在 Dart 事件循环（主 isolate）中执行，可直接更新 UI。
abstract class MqttClientListener {
  /// 连接成功。
  ///
  /// [reconnect] 为 true 表示自动重连成功后触发，false 表示首次连接成功。
  void onConnectSuccess(bool reconnect) {}

  /// 连接丢失（等待自动重连）。
  void onConnectionLost() {}

  /// 收到订阅消息。
  ///
  /// [topic] 消息主题；[payload] 消息内容（UTF-8 解码）。
  void onMessageArrived(String topic, String payload) {}

  /// 发生错误。
  ///
  /// [message] 错误描述。
  void onError(String message) {}
}
