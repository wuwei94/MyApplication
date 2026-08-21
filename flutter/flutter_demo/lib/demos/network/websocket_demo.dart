import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_demo/core/constants/urls.dart';
import 'package:web_socket_channel/status.dart' as status;
import 'package:web_socket_channel/web_socket_channel.dart';

/// WebSocket
/// https://pub.dev/packages/web_socket_channel
class WebSocketDemoPage extends StatelessWidget {
  const WebSocketDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return WebSocketDemoView(title: title);
  }
}

class WebSocketDemoView extends StatefulWidget {
  const WebSocketDemoView({super.key, required this.title});

  final String title;

  @override
  State<WebSocketDemoView> createState() => _WebSocketDemoViewState();
}

enum _ConnectionState {
  disconnected('未连接', Color(0xFF64748B), Icons.cloud_off_rounded),
  connecting('连接中...', Color(0xFFD97706), Icons.sync_rounded),
  connected('已连接', Color(0xFF10B981), Icons.cloud_done_rounded),
  error('连接异常', Color(0xFFEF4444), Icons.error_outline_rounded);

  const _ConnectionState(this.label, this.color, this.icon);

  final String label;
  final Color color;
  final IconData icon;
}

enum _RecordType {
  sent('发送', Color(0xFF0284C7), Icons.arrow_upward_rounded),
  received('接收', Color(0xFF10B981), Icons.arrow_downward_rounded),
  status('状态', Color(0xFF64748B), Icons.info_outline_rounded),
  error('异常', Color(0xFFEF4444), Icons.warning_amber_rounded);

  const _RecordType(this.label, this.color, this.icon);

  final String label;
  final Color color;
  final IconData icon;
}

class _LogRecord {
  const _LogRecord({
    required this.type,
    required this.content,
    required this.time,
  });

  final _RecordType type;
  final String content;
  final DateTime time;

  String get formattedTime {
    final String hour = time.hour.toString().padLeft(2, '0');
    final String minute = time.minute.toString().padLeft(2, '0');
    final String second = time.second.toString().padLeft(2, '0');
    final String millisecond = time.millisecond.toString().padLeft(3, '0');
    return '$hour:$minute:$second.$millisecond';
  }
}

class _WebSocketDemoViewState extends State<WebSocketDemoView> {
  final TextEditingController _urlController = TextEditingController(
    text: Urls.websocketEcho,
  );
  final TextEditingController _messageController = TextEditingController();
  final ScrollController _scrollController = ScrollController();

  WebSocketChannel? _channel;
  StreamSubscription<dynamic>? _subscription;
  _ConnectionState _connectionState = _ConnectionState.disconnected;
  final List<_LogRecord> _logs = <_LogRecord>[];

  @override
  void dispose() {
    _cleanupChannel();
    _urlController.dispose();
    _messageController.dispose();
    _scrollController.dispose();
    super.dispose();
  }

  void _cleanupChannel() {
    _subscription?.cancel();
    _subscription = null;
    _channel?.sink.close(status.normalClosure);
    _channel = null;
  }

  void _addLog(_RecordType type, String content) {
    if (!mounted) {
      return;
    }
    setState(() {
      _logs.insert(
        0,
        _LogRecord(type: type, content: content, time: DateTime.now()),
      );
    });
  }

  Future<void> _connect() async {
    final String url = _urlController.text.trim();
    if (url.isEmpty) {
      _addLog(_RecordType.error, 'WebSocket 地址不能为空');
      return;
    }

    final Uri? uri = Uri.tryParse(url);
    if (uri == null || (uri.scheme != 'ws' && uri.scheme != 'wss')) {
      _addLog(
        _RecordType.error,
        '无效的 WebSocket URL: $url（需以 ws:// 或 wss:// 开头）',
      );
      return;
    }

    _cleanupChannel();

    setState(() {
      _connectionState = _ConnectionState.connecting;
    });
    _addLog(_RecordType.status, '正在连接至 $url ...');

    try {
      final WebSocketChannel channel = WebSocketChannel.connect(uri);
      _channel = channel;

      await channel.ready;

      if (!mounted) {
        return;
      }

      setState(() {
        _connectionState = _ConnectionState.connected;
      });
      _addLog(_RecordType.status, 'WebSocket 连接成功');

      _subscription = channel.stream.listen(
        (dynamic message) {
          _addLog(_RecordType.received, message.toString());
        },
        onError: (Object error) {
          if (!mounted) return;
          setState(() {
            _connectionState = _ConnectionState.error;
          });
          _addLog(_RecordType.error, '通信异常: $error');
        },
        onDone: () {
          if (!mounted) return;
          setState(() {
            _connectionState = _ConnectionState.disconnected;
          });
          final int? closeCode = channel.closeCode;
          final String? closeReason = channel.closeReason;
          final String reasonInfo =
              closeReason != null && closeReason.isNotEmpty
              ? ' ($closeReason)'
              : '';
          _addLog(
            _RecordType.status,
            'WebSocket 连接已关闭: Code ${closeCode ?? "未知"}$reasonInfo',
          );
        },
      );
    } catch (error) {
      if (!mounted) {
        return;
      }
      setState(() {
        _connectionState = _ConnectionState.error;
      });
      _addLog(_RecordType.error, '连接失败: $error');
    }
  }

  void _disconnect() {
    if (_connectionState == _ConnectionState.disconnected) {
      return;
    }

    _addLog(_RecordType.status, '正在主动断开连接...');
    _cleanupChannel();

    setState(() {
      _connectionState = _ConnectionState.disconnected;
    });
    _addLog(_RecordType.status, '连接已断开');
  }

  void _sendMessage([String? customMessage]) {
    if (_connectionState != _ConnectionState.connected || _channel == null) {
      _addLog(_RecordType.error, '无法发送消息：WebSocket 未连接');
      return;
    }

    final String text = customMessage ?? _messageController.text.trim();
    if (text.isEmpty) {
      return;
    }

    try {
      _channel?.sink.add(text);
      _addLog(_RecordType.sent, text);
      if (customMessage == null) {
        _messageController.clear();
      }
    } catch (error) {
      _addLog(_RecordType.error, '发送消息失败: $error');
    }
  }

  void _clearLogs() {
    setState(() {
      _logs.clear();
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
        actions: <Widget>[
          IconButton(
            icon: const Icon(Icons.delete_outline_rounded),
            tooltip: '清空日志',
            onPressed: _logs.isEmpty ? null : _clearLogs,
          ),
        ],
      ),
      body: _buildBody(),
    );
  }

  Widget _buildBody() {
    return ListView(
      controller: _scrollController,
      padding: const EdgeInsets.all(16),
      children: <Widget>[
        _buildConnectionCard(),
        const SizedBox(height: 16),
        _buildMessageComposerCard(),
        const SizedBox(height: 16),
        _buildLogSectionHeader(),
        const SizedBox(height: 12),
        if (_logs.isEmpty)
          _buildEmptyLogsCard()
        else
          for (final _LogRecord record in _logs)
            Padding(
              padding: const EdgeInsets.only(bottom: 8),
              child: _buildLogItem(record),
            ),
      ],
    );
  }

  Widget _buildConnectionCard() {
    final ThemeData theme = Theme.of(context);
    final bool isConnected = _connectionState == _ConnectionState.connected;
    final bool isConnecting = _connectionState == _ConnectionState.connecting;

    return Container(
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(20),
        gradient: const LinearGradient(
          colors: <Color>[Color(0xFF0F172A), Color(0xFF1E293B)],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        boxShadow: const <BoxShadow>[
          BoxShadow(
            color: Color(0x1E0F172A),
            blurRadius: 16,
            offset: Offset(0, 8),
          ),
        ],
      ),
      padding: const EdgeInsets.all(18),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: <Widget>[
              Text(
                'WebSocket 连接配置',
                style: theme.textTheme.titleMedium?.copyWith(
                  color: Colors.white,
                  fontWeight: FontWeight.w700,
                ),
              ),
              Container(
                padding: const EdgeInsets.symmetric(
                  horizontal: 10,
                  vertical: 4,
                ),
                decoration: BoxDecoration(
                  color: _connectionState.color.withValues(alpha: 0.2),
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(
                    color: _connectionState.color.withValues(alpha: 0.5),
                  ),
                ),
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: <Widget>[
                    if (isConnecting)
                      const SizedBox(
                        width: 12,
                        height: 12,
                        child: CircularProgressIndicator(
                          strokeWidth: 2,
                          color: Color(0xFFD97706),
                        ),
                      )
                    else
                      Icon(
                        _connectionState.icon,
                        size: 14,
                        color: _connectionState.color,
                      ),
                    const SizedBox(width: 6),
                    Text(
                      _connectionState.label,
                      style: theme.textTheme.bodySmall?.copyWith(
                        color: _connectionState.color,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
          const SizedBox(height: 14),
          TextField(
            controller: _urlController,
            enabled: !isConnected && !isConnecting,
            style: const TextStyle(color: Colors.white, fontSize: 13),
            decoration: InputDecoration(
              isDense: true,
              labelText: '服务器地址',
              labelStyle: const TextStyle(color: Color(0xFF94A3B8)),
              filled: true,
              fillColor: Colors.white.withValues(alpha: 0.08),
              prefixIcon: const Icon(
                Icons.link_rounded,
                color: Color(0xFF94A3B8),
                size: 20,
              ),
              suffixIcon: IconButton(
                icon: const Icon(
                  Icons.restore_rounded,
                  color: Color(0xFF94A3B8),
                  size: 18,
                ),
                tooltip: '重置为默认地址',
                onPressed: (!isConnected && !isConnecting)
                    ? () {
                        _urlController.text = Urls.websocketEcho;
                      }
                    : null,
              ),
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(12),
                borderSide: BorderSide(
                  color: Colors.white.withValues(alpha: 0.15),
                ),
              ),
              enabledBorder: OutlineInputBorder(
                borderRadius: BorderRadius.circular(12),
                borderSide: BorderSide(
                  color: Colors.white.withValues(alpha: 0.15),
                ),
              ),
              focusedBorder: OutlineInputBorder(
                borderRadius: BorderRadius.circular(12),
                borderSide: const BorderSide(color: Color(0xFF38BDF8)),
              ),
            ),
          ),
          const SizedBox(height: 14),
          Row(
            children: <Widget>[
              Expanded(
                child: FilledButton.icon(
                  onPressed: (isConnected || isConnecting)
                      ? _disconnect
                      : _connect,
                  style: FilledButton.styleFrom(
                    backgroundColor: isConnected
                        ? const Color(0xFFEF4444)
                        : (isConnecting
                              ? const Color(0xFFD97706)
                              : const Color(0xFF0284C7)),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(12),
                    ),
                    padding: const EdgeInsets.symmetric(vertical: 12),
                  ),
                  icon: Icon(
                    isConnected
                        ? Icons.link_off_rounded
                        : Icons.play_arrow_rounded,
                  ),
                  label: Text(
                    isConnected ? '断开连接' : (isConnecting ? '取消连接' : '建立连接'),
                  ),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildMessageComposerCard() {
    final ThemeData theme = Theme.of(context);
    final bool isConnected = _connectionState == _ConnectionState.connected;

    return Container(
      decoration: BoxDecoration(
        color: const Color(0xFFF8FAFC),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: const Color(0xFFE2E8F0)),
      ),
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Text(
            '发送消息 (Echo 回显测试)',
            style: theme.textTheme.titleMedium?.copyWith(
              fontWeight: FontWeight.w700,
            ),
          ),
          const SizedBox(height: 12),
          Row(
            children: <Widget>[
              Expanded(
                child: TextField(
                  controller: _messageController,
                  enabled: isConnected,
                  decoration: InputDecoration(
                    hintText: isConnected
                        ? '输入要发送的消息内容...'
                        : '请先建立 WebSocket 连接',
                    isDense: true,
                    filled: true,
                    fillColor: Colors.white,
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(12),
                      borderSide: const BorderSide(color: Color(0xFFCBD5E1)),
                    ),
                    enabledBorder: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(12),
                      borderSide: const BorderSide(color: Color(0xFFCBD5E1)),
                    ),
                  ),
                  onSubmitted: (_) => _sendMessage(),
                ),
              ),
              const SizedBox(width: 8),
              FilledButton(
                onPressed: isConnected ? () => _sendMessage() : null,
                style: FilledButton.styleFrom(
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(12),
                  ),
                  padding: const EdgeInsets.all(14),
                ),
                child: const Icon(Icons.send_rounded, size: 20),
              ),
            ],
          ),
          const SizedBox(height: 12),
          Text(
            '快捷测试模板',
            style: theme.textTheme.bodySmall?.copyWith(
              color: theme.colorScheme.onSurfaceVariant,
              fontWeight: FontWeight.w600,
            ),
          ),
          const SizedBox(height: 8),
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: <Widget>[
              ActionChip(
                label: const Text('Hello WebSocket!'),
                onPressed: isConnected
                    ? () => _sendMessage('Hello WebSocket!')
                    : null,
              ),
              ActionChip(
                label: const Text('Ping 探测'),
                onPressed: isConnected ? () => _sendMessage('ping') : null,
              ),
              ActionChip(
                label: const Text('发送时间戳'),
                onPressed: isConnected
                    ? () => _sendMessage(
                        'Current Time: ${DateTime.now().toIso8601String()}',
                      )
                    : null,
              ),
              ActionChip(
                label: const Text('JSON 报文'),
                onPressed: isConnected
                    ? () => _sendMessage(
                        '{"type":"echo","timestamp":${DateTime.now().millisecondsSinceEpoch}}',
                      )
                    : null,
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildLogSectionHeader() {
    final ThemeData theme = Theme.of(context);
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: <Widget>[
        Text(
          '通信日志 (${_logs.length})',
          style: theme.textTheme.titleMedium?.copyWith(
            fontWeight: FontWeight.w700,
          ),
        ),
        if (_logs.isNotEmpty)
          Text(
            '按最新时间降序排列',
            style: theme.textTheme.bodySmall?.copyWith(
              color: theme.colorScheme.onSurfaceVariant,
            ),
          ),
      ],
    );
  }

  Widget _buildEmptyLogsCard() {
    final ThemeData theme = Theme.of(context);
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.symmetric(vertical: 36, horizontal: 24),
      decoration: BoxDecoration(
        color: const Color(0xFFF8FAFC),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: const Color(0xFFE2E8F0)),
      ),
      child: Column(
        children: <Widget>[
          const Icon(
            Icons.swap_vert_rounded,
            size: 40,
            color: Color(0xFF94A3B8),
          ),
          const SizedBox(height: 10),
          Text(
            '暂无通信日志',
            style: theme.textTheme.titleSmall?.copyWith(
              fontWeight: FontWeight.w600,
              color: const Color(0xFF475569),
            ),
          ),
          const SizedBox(height: 4),
          Text(
            '连接 WebSocket 服务器后，在此处实时查看发送与回显接收的消息。',
            textAlign: TextAlign.center,
            style: theme.textTheme.bodySmall?.copyWith(
              color: theme.colorScheme.onSurfaceVariant,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildLogItem(_LogRecord record) {
    final ThemeData theme = Theme.of(context);

    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: record.type.color.withValues(alpha: 0.2)),
        boxShadow: const <BoxShadow>[
          BoxShadow(
            color: Color(0x08000000),
            blurRadius: 8,
            offset: Offset(0, 2),
          ),
        ],
      ),
      padding: const EdgeInsets.all(12),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Container(
            padding: const EdgeInsets.all(6),
            decoration: BoxDecoration(
              color: record.type.color.withValues(alpha: 0.12),
              borderRadius: BorderRadius.circular(8),
            ),
            child: Icon(record.type.icon, size: 16, color: record.type.color),
          ),
          const SizedBox(width: 10),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: <Widget>[
                    Container(
                      padding: const EdgeInsets.symmetric(
                        horizontal: 6,
                        vertical: 2,
                      ),
                      decoration: BoxDecoration(
                        color: record.type.color.withValues(alpha: 0.1),
                        borderRadius: BorderRadius.circular(4),
                      ),
                      child: Text(
                        record.type.label,
                        style: TextStyle(
                          fontSize: 11,
                          fontWeight: FontWeight.w700,
                          color: record.type.color,
                        ),
                      ),
                    ),
                    Text(
                      record.formattedTime,
                      style: theme.textTheme.bodySmall?.copyWith(
                        color: theme.colorScheme.onSurfaceVariant,
                        fontSize: 11,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 6),
                SelectableText(
                  record.content,
                  style: theme.textTheme.bodyMedium?.copyWith(
                    fontFamily: 'monospace',
                    fontSize: 13,
                    color: const Color(0xFF1E293B),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
