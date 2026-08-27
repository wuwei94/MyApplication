import 'dart:async';
import 'dart:typed_data';

import 'package:lib_websocket/src/websocket_client_listener.dart';
import 'package:web_socket_channel/web_socket_channel.dart';

/// WebSocket 客户端门面，封装 web_socket_channel 的连接、发送与关闭。
///
/// 与 Android 侧 `JavaWebSocketClient` / `OkHttpWebSocketClient` 对齐，回调统一
/// 通过 [WebSocketClientListener] 抛出。按 url 管理多条连接，支持自动重连。
class WebSocketClient {
  WebSocketClient._();

  static final WebSocketClient _instance = WebSocketClient._();

  static WebSocketClient get instance => _instance;

  factory WebSocketClient() {
    return _instance;
  }

  final Map<String, _Connection> _connections = <String, _Connection>{};

  /// 指定 url 当前是否已连接。
  bool isConnected(String url) => _connections[url]?.connected ?? false;

  /// 建立 WebSocket 连接。
  ///
  /// [url] WebSocket 地址，需以 `ws://` 或 `wss://` 开头。
  /// [autoReconnect] 连接意外断开后是否自动重连，默认 true。
  /// [reconnectInterval] 自动重连间隔，默认 5 秒。
  Future<void> connect({
    required String url,
    bool autoReconnect = true,
    Duration reconnectInterval = const Duration(seconds: 5),
    WebSocketClientListener? listener,
  }) async {
    final _Connection conn = _connections[url] ?? _Connection();
    _connections[url] = conn;
    conn.listener = listener;
    conn.autoReconnect = autoReconnect;
    conn.reconnectInterval = reconnectInterval;
    conn.closedByUser = false;

    // 已有活跃连接时直接复用
    if (conn.connected) {
      return;
    }

    conn.reconnectTimer?.cancel();
    conn.reconnectTimer = null;
    await _connect(url, conn);
  }

  Future<void> _connect(String url, _Connection conn) async {
    final Uri? uri = Uri.tryParse(url);
    if (uri == null || (uri.scheme != 'ws' && uri.scheme != 'wss')) {
      conn.listener?.onError('无效的 WebSocket URL: $url');
      return;
    }

    try {
      final WebSocketChannel channel = WebSocketChannel.connect(uri);
      conn.channel = channel;
      await channel.ready;
      conn.connected = true;
      conn.listener?.onOpen();
      conn.subscription = channel.stream.listen(
        (dynamic message) => _onMessage(conn, message),
        onError: (Object error) => _onError(conn, error),
        onDone: () => _onDone(url, conn),
      );
    } catch (error) {
      conn.connected = false;
      conn.listener?.onError('连接失败: $error');
    }
  }

  /// 向指定 url 发送文本消息，连接不存在或未连接时返回 false。
  bool send(String url, String message) {
    final _Connection? conn = _connections[url];
    if (conn == null || !conn.connected) {
      return false;
    }
    conn.channel?.sink.add(message);
    return true;
  }

  /// 向指定 url 发送二进制消息，连接不存在或未连接时返回 false。
  bool sendBytes(String url, List<int> bytes) {
    final _Connection? conn = _connections[url];
    if (conn == null || !conn.connected) {
      return false;
    }
    conn.channel?.sink.add(bytes);
    return true;
  }

  /// 主动关闭指定 url 的连接（不触发自动重连）。
  void close(String url) {
    final _Connection? conn = _connections.remove(url);
    if (conn == null) {
      return;
    }
    conn.closedByUser = true;
    conn.connected = false;
    conn.reconnectTimer?.cancel();
    conn.reconnectTimer = null;
    conn.subscription?.cancel();
    conn.subscription = null;
    conn.channel?.sink.close();
    conn.channel = null;
  }

  /// 关闭所有连接。
  void closeAll() {
    final List<String> urls = _connections.keys.toList();
    for (final String url in urls) {
      close(url);
    }
  }

  void _onMessage(_Connection conn, dynamic message) {
    if (message is String) {
      conn.listener?.onMessage(message);
    } else if (message is Uint8List) {
      conn.listener?.onMessageBytes(message);
    }
  }

  void _onError(_Connection conn, Object error) {
    conn.connected = false;
    conn.listener?.onError('通信异常: $error');
  }

  void _onDone(String url, _Connection conn) {
    final int? code = conn.channel?.closeCode;
    final String? reason = conn.channel?.closeReason;
    conn.connected = false;
    conn.listener?.onClose(code, reason);
    conn.subscription = null;
    conn.channel = null;

    if (!conn.closedByUser && conn.autoReconnect && conn.reconnectTimer == null) {
      conn.reconnectTimer = Timer(conn.reconnectInterval, () {
        conn.reconnectTimer = null;
        _connect(url, conn);
      });
    }
  }
}

/// 单条连接的上下文，按 url 独立管理。
class _Connection {
  WebSocketChannel? channel;
  // ignore: cancel_subscriptions — 由 WebSocketClient.close/onDone 统一取消
  StreamSubscription<dynamic>? subscription;
  WebSocketClientListener? listener;
  Timer? reconnectTimer;

  bool connected = false;
  bool closedByUser = false;
  bool autoReconnect = true;
  Duration reconnectInterval = const Duration(seconds: 5);
}
