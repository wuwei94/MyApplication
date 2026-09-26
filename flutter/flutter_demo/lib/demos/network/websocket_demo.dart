import 'dart:typed_data';

import 'package:flutter_demo/core/basic/basic.dart';
import 'package:flutter_demo/core/constants/urls.dart';
import 'package:lib_websocket/lib_websocket.dart';

/// WebSocket — 全双工双向实时通信
///
/// 核心机制与避坑点：
/// 1. URL 约束：仅接受 `ws://` / `wss://`，非法地址走 `onError`。
/// 2. 重连语义：主动 `close` 不触发自动重连；意外断开默认 5s 后重连。
/// 3. 发送契约：`send` 在未连接时返回 `false`，不抛异常。
///
/// 官方参考：
/// https://pub.dev/packages/web_socket_channel
class WebSocketDemoPage extends BasicResponsePage {
  const WebSocketDemoPage({super.key, required super.title});

  @override
  BasicResponsePageState<WebSocketDemoPage> createState() =>
      _WebSocketDemoPageState();
}

class _WebSocketDemoPageState extends BasicResponsePageState<WebSocketDemoPage>
    implements WebSocketClientListener {
  static const String _url = Urls.websocketEcho;

  final WebSocketClient _client = WebSocketClient.instance;

  bool get _connected => _client.isConnected(_url);

  @override
  void initState() {
    super.initState();
    showDescription('WebSocket 示例：连接 $_url，双向收发文本 / JSON 报文');
  }

  @override
  void dispose() {
    _client.close(_url);
    super.dispose();
  }

  @override
  List<String> buildList() => const <String>[
        '1. 建立 WebSocket 连接',
        '2. 发送文本消息',
        '3. 发送 JSON 报文',
        '4. 断开 WebSocket 连接',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _connect();
      case 1:
        _sendText();
      case 2:
        _sendJson();
      case 3:
        _disconnect();
    }
  }

  Future<void> _connect() async {
    if (_connected) {
      appendLog('→ [Connect] 已处于连接状态');
      return;
    }
    appendLog('→ [Connect] 正在连接 $_url ...');
    await _client.connect(url: _url, listener: this);
  }

  void _disconnect() {
    if (!_connected) {
      appendLog('→ [Disconnect] 当前未连接');
      return;
    }
    appendLog('→ [Disconnect] 主动断开连接');
    _client.close(_url);
  }

  void _sendText() {
    _send('Hello WebSocket!');
  }

  void _sendJson() {
    _send(
      '{"type":"echo","timestamp":${DateTime.now().millisecondsSinceEpoch}}',
    );
  }

  void _send(String message) {
    if (!_connected) {
      appendLog('✗ [Send] WebSocket 未连接');
      return;
    }
    if (_client.send(_url, message)) {
      appendLog('→ [Send] $message');
    } else {
      appendLog('✗ [Send] 发送失败：连接已断开');
    }
  }

  @override
  void onOpen() {
    if (!mounted) return;
    appendLog('✓ [Open] WebSocket 连接成功');
  }

  @override
  void onMessage(String message) {
    if (!mounted) return;
    appendLog('✓ [Message] $message');
  }

  @override
  void onMessageBytes(Uint8List bytes) {
    if (!mounted) return;
    appendLog('✓ [MessageBytes] 二进制消息（${bytes.length} 字节）');
  }

  @override
  void onClose(int? code, String? reason) {
    if (!mounted) return;
    final String reasonInfo =
        reason != null && reason.isNotEmpty ? ' ($reason)' : '';
    appendLog('✓ [Close] code=${code ?? '未知'}$reasonInfo');
  }

  @override
  void onError(String message) {
    if (!mounted) return;
    appendLog('✗ [Error] $message');
  }
}
