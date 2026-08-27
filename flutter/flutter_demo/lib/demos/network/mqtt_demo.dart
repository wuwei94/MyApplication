import 'package:flutter/material.dart';
import 'package:flutter_demo/core/constants/urls.dart';
import 'package:lib_mqtt/lib_mqtt.dart';

/// MQTT
/// https://pub.dev/packages/mqtt_client
class MqttDemoPage extends StatelessWidget {
  const MqttDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return MqttDemoView(title: title);
  }
}

class MqttDemoView extends StatefulWidget {
  const MqttDemoView({super.key, required this.title});

  final String title;

  @override
  State<MqttDemoView> createState() => _MqttDemoViewState();
}

enum _ConnectionState {
  disconnected('未连接', Color(0xFF64748B), Icons.cloud_off_rounded),
  connecting('连接中...', Color(0xFFD97706), Icons.sync_rounded),
  connected('已连接', Color(0xFF10B981), Icons.cloud_done_rounded);

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

class _MqttDemoViewState extends State<MqttDemoView>
    implements MqttClientListener {
  final TextEditingController _topicController = TextEditingController(
    text: Urls.mqttTopic,
  );
  final TextEditingController _messageController = TextEditingController();
  final ScrollController _scrollController = ScrollController();

  final MqttClientManager _manager = MqttClientManager.instance;

  _ConnectionState _connectionState = _ConnectionState.disconnected;
  final int _subscribeQos = 2;
  int _publishQos = 0;
  final List<_LogRecord> _logs = <_LogRecord>[];

  @override
  void dispose() {
    _manager.disconnect();
    _topicController.dispose();
    _messageController.dispose();
    _scrollController.dispose();
    super.dispose();
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
    setState(() {
      _connectionState = _ConnectionState.connecting;
    });
    _addLog(
      _RecordType.status,
      '正在连接 ${Urls.mqttHost}:${Urls.mqttPort} ...',
    );

    await _manager.connect(
      host: Urls.mqttHost,
      port: Urls.mqttPort,
      listener: this,
    );
  }

  void _disconnect() {
    if (_connectionState == _ConnectionState.disconnected) {
      return;
    }
    _manager.disconnect();
    setState(() {
      _connectionState = _ConnectionState.disconnected;
    });
    _addLog(_RecordType.status, '已断开连接');
  }

  void _subscribe() {
    if (!_manager.isConnected()) {
      _addLog(_RecordType.error, '未连接，请先连接 Broker');
      return;
    }
    final String topic = _topicController.text.trim();
    if (topic.isEmpty) {
      _addLog(_RecordType.error, '主题不能为空');
      return;
    }
    _manager.subscribe(topic, qos: _subscribeQos);
    _addLog(_RecordType.status, '已订阅 $topic（QoS $_subscribeQos）');
  }

  void _publish([String? customMessage]) {
    if (!_manager.isConnected()) {
      _addLog(_RecordType.error, '未连接，请先连接 Broker');
      return;
    }
    final String topic = _topicController.text.trim();
    if (topic.isEmpty) {
      _addLog(_RecordType.error, '主题不能为空');
      return;
    }
    final String payload =
        customMessage ??
        'Hello MQTT! qos=$_publishQos time=${DateTime.now().millisecondsSinceEpoch}';
    _manager.publish(topic, payload, qos: _publishQos);
    _addLog(_RecordType.sent, '$topic（QoS $_publishQos）\n$payload');
    if (customMessage == null) {
      _messageController.clear();
    }
  }

  void _clearLogs() {
    setState(() {
      _logs.clear();
    });
  }

  @override
  void onConnectSuccess(bool reconnect) {
    if (!mounted) {
      return;
    }
    setState(() {
      _connectionState = _ConnectionState.connected;
    });
    _addLog(
      _RecordType.status,
      reconnect ? '已自动重连成功' : '已连接 ${Urls.mqttHost}:${Urls.mqttPort}',
    );
  }

  @override
  void onConnectionLost() {
    if (!mounted) {
      return;
    }
    setState(() {
      _connectionState = _ConnectionState.disconnected;
    });
    _addLog(_RecordType.status, '连接丢失，等待自动重连...');
  }

  @override
  void onMessageArrived(String topic, String payload) {
    _addLog(_RecordType.received, 'topic=$topic\npayload=$payload');
  }

  @override
  void onError(String message) {
    if (!mounted) {
      return;
    }
    if (_connectionState == _ConnectionState.connecting) {
      setState(() {
        _connectionState = _ConnectionState.disconnected;
      });
    }
    _addLog(_RecordType.error, message);
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
        _buildActionCard(),
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
                'MQTT Broker 连接',
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
          const SizedBox(height: 12),
          Text(
            '${Urls.mqttHost}:${Urls.mqttPort} · EMQX 公共 Broker（无需账号）',
            style: theme.textTheme.bodySmall?.copyWith(
              color: const Color(0xFF94A3B8),
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
                    isConnected ? '断开连接' : (isConnecting ? '连接中...' : '建立连接'),
                  ),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildActionCard() {
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
            '主题与消息',
            style: theme.textTheme.titleMedium?.copyWith(
              fontWeight: FontWeight.w700,
            ),
          ),
          const SizedBox(height: 12),
          TextField(
            controller: _topicController,
            enabled: !isConnected,
            decoration: InputDecoration(
              hintText: '订阅 / 发布主题',
              isDense: true,
              filled: true,
              fillColor: Colors.white,
              prefixIcon: const Icon(
                Icons.topic_rounded,
                size: 20,
                color: Color(0xFF64748B),
              ),
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(12),
                borderSide: const BorderSide(color: Color(0xFFCBD5E1)),
              ),
              enabledBorder: OutlineInputBorder(
                borderRadius: BorderRadius.circular(12),
                borderSide: const BorderSide(color: Color(0xFFCBD5E1)),
              ),
            ),
          ),
          const SizedBox(height: 12),
          Row(
            children: <Widget>[
              Expanded(
                child: OutlinedButton.icon(
                  onPressed: isConnected ? _subscribe : null,
                  icon: const Icon(Icons.add_link_rounded, size: 18),
                  label: Text('订阅（QoS $_subscribeQos）'),
                  style: OutlinedButton.styleFrom(
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(12),
                    ),
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          Row(
            children: <Widget>[
              Expanded(
                child: TextField(
                  controller: _messageController,
                  enabled: isConnected,
                  decoration: InputDecoration(
                    hintText: isConnected ? '输入要发布的消息...' : '请先连接并订阅',
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
                  onSubmitted: (_) => _publish(),
                ),
              ),
              const SizedBox(width: 8),
              FilledButton(
                onPressed: isConnected ? () => _publish() : null,
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
          _buildQosSelector(),
        ],
      ),
    );
  }

  Widget _buildQosSelector() {
    final ThemeData theme = Theme.of(context);
    final bool isConnected = _connectionState == _ConnectionState.connected;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        Text(
          '发布 QoS',
          style: theme.textTheme.bodySmall?.copyWith(
            color: theme.colorScheme.onSurfaceVariant,
            fontWeight: FontWeight.w600,
          ),
        ),
        const SizedBox(height: 4),
        Text(
          '0 最多一次 · 1 至少一次 · 2 恰好一次',
          style: theme.textTheme.bodySmall?.copyWith(
            color: theme.colorScheme.onSurfaceVariant,
            fontSize: 11,
          ),
        ),
        const SizedBox(height: 8),
        SegmentedButton<int>(
          segments: const <ButtonSegment<int>>[
            ButtonSegment<int>(value: 0, label: Text('QoS 0')),
            ButtonSegment<int>(value: 1, label: Text('QoS 1')),
            ButtonSegment<int>(value: 2, label: Text('QoS 2')),
          ],
          selected: <int>{_publishQos},
          onSelectionChanged: isConnected
              ? (Set<int> selection) {
                  setState(() {
                    _publishQos = selection.first;
                  });
                }
              : null,
        ),
      ],
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
            '连接 Broker 并订阅主题后，在此处实时查看发布与接收的消息。',
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
