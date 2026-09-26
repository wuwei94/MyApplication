import 'package:flutter_demo/core/basic/basic.dart';
import 'package:flutter_demo/core/constants/urls.dart';
import 'package:lib_mqtt/lib_mqtt.dart';

/// MQTT — 订阅 / 发布与 QoS 语义
///
/// 核心机制与避坑点：
/// 1. QoS：0 最多一次、1 至少一次、2 恰好一次；订阅与发布可分别指定。
/// 2. 会话：clientId 同 Broker 下需唯一；`cleanSession=true` 不保留离线消息。
/// 3. 重连：`autoReconnect` 开启后 `onConnectSuccess(true)` 表示自动重连成功。
///
/// 官方参考：
/// https://pub.dev/packages/mqtt_client
class MqttDemoPage extends BasicResponsePage {
  const MqttDemoPage({super.key, required super.title});

  @override
  BasicResponsePageState<MqttDemoPage> createState() => _MqttDemoPageState();
}

class _MqttDemoPageState extends BasicResponsePageState<MqttDemoPage>
    implements MqttClientListener {
  static const String _topic = Urls.mqttTopic;
  static const int _subscribeQos = 2;

  final MqttClientManager _manager = MqttClientManager.instance;
  int _publishQos = 0;

  @override
  void initState() {
    super.initState();
    showDescription(
      'MQTT 示例：连接 ${Urls.mqttHost}:${Urls.mqttPort}，'
      '订阅/发布主题 $_topic 并对比 QoS 0/1/2',
    );
  }

  @override
  void dispose() {
    _manager.disconnect();
    super.dispose();
  }

  @override
  List<String> buildList() => const <String>[
        '1. 连接 MQTT Broker',
        '2. 订阅主题（QoS 2）',
        '3. 切换发布 QoS',
        '4. 发布消息',
        '5. 断开 MQTT 连接',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _connect();
      case 1:
        _subscribe();
      case 2:
        _togglePublishQos();
      case 3:
        _publish();
      case 4:
        _disconnect();
    }
  }

  Future<void> _connect() async {
    if (_manager.isConnected()) {
      appendLog('→ [Connect] 已处于连接状态');
      return;
    }
    appendLog('→ [Connect] 正在连接 ${Urls.mqttHost}:${Urls.mqttPort} ...');
    await _manager.connect(
      host: Urls.mqttHost,
      port: Urls.mqttPort,
      listener: this,
    );
  }

  void _disconnect() {
    if (!_manager.isConnected()) {
      appendLog('→ [Disconnect] 当前未连接');
      return;
    }
    _manager.disconnect();
    appendLog('→ [Disconnect] 已断开 Broker 连接');
  }

  void _subscribe() {
    if (!_manager.isConnected()) {
      appendLog('✗ [Subscribe] 未连接，请先连接 Broker');
      return;
    }
    _manager.subscribe(_topic, qos: _subscribeQos);
    appendLog('→ [Subscribe] topic=$_topic qos=$_subscribeQos');
  }

  void _togglePublishQos() {
    _publishQos = (_publishQos + 1) % 3;
    appendLog('→ [QoS] 发布 QoS 切换为 $_publishQos');
  }

  void _publish() {
    if (!_manager.isConnected()) {
      appendLog('✗ [Publish] 未连接，请先连接 Broker');
      return;
    }
    final String payload =
        'Hello MQTT! qos=$_publishQos time=${DateTime.now().millisecondsSinceEpoch}';
    _manager.publish(_topic, payload, qos: _publishQos);
    appendLog('→ [Publish] topic=$_topic qos=$_publishQos payload=$payload');
  }

  @override
  void onConnectSuccess(bool reconnect) {
    if (!mounted) return;
    appendLog(
      reconnect
          ? '✓ [Connect] 已自动重连成功'
          : '✓ [Connect] 已连接 ${Urls.mqttHost}:${Urls.mqttPort}',
    );
  }

  @override
  void onConnectionLost() {
    if (!mounted) return;
    appendLog('✗ [ConnectionLost] 连接丢失，等待自动重连...');
  }

  @override
  void onMessageArrived(String topic, String payload) {
    if (!mounted) return;
    appendLog('✓ [Message] topic=$topic payload=$payload');
  }

  @override
  void onError(String message) {
    if (!mounted) return;
    appendLog('✗ [Error] $message');
  }
}
