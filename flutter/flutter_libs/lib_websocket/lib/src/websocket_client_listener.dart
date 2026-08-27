import 'dart:typed_data';

/// WebSocket 客户端回调接口，与 Android `JavaWebSocketClientListener` 对齐。
///
/// 所有回调均在 Dart 事件循环（主 isolate）中执行，可直接更新 UI。
abstract class WebSocketClientListener {
  /// 连接已建立。
  void onOpen() {}

  /// 收到文本消息。
  ///
  /// [message] 消息内容（UTF-8 解码）。
  void onMessage(String message) {}

  /// 收到二进制消息。
  ///
  /// [bytes] 原始字节。
  void onMessageBytes(Uint8List bytes) {}

  /// 连接已关闭。
  ///
  /// [code] 关闭状态码；[reason] 关闭原因。
  void onClose(int? code, String? reason) {}

  /// 发生错误。
  ///
  /// [message] 错误描述。
  void onError(String message) {}
}
