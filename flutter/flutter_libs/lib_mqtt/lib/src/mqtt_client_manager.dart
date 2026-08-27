import 'dart:async';

import 'package:lib_mqtt/src/mqtt_client_listener.dart';
import 'package:mqtt_client/mqtt_client.dart';
import 'package:mqtt_client/mqtt_server_client.dart';

/// MQTT 客户端门面，封装 mqtt_client 的连接、订阅、发布与断开。
///
/// 与 Android 侧 `HiveMqClientManager` 对齐，回调统一通过 [MqttClientListener]
/// 抛出。默认使用 MQTT 3.1.1，支持用户名密码鉴权（如阿里云 IoT 一机一密）。
class MqttClientManager {
  MqttClientManager._();

  static final MqttClientManager _instance = MqttClientManager._();

  static MqttClientManager get instance => _instance;

  factory MqttClientManager() {
    return _instance;
  }

  MqttServerClient? _client;
  MqttClientListener? _listener;
  StreamSubscription<List<MqttReceivedMessage<MqttMessage>>>? _subscription;
  bool _connected = false;

  /// 当前是否已连接。
  bool isConnected() => _connected;

  /// 建立 MQTT 3.1.1 连接。
  ///
  /// [host] Broker 主机名，形如 `broker.emqx.io`。
  /// [port] Broker 端口，默认 1883。
  /// [clientId] 客户端 ID，同一 Broker 下需唯一，缺省自动生成。
  /// [username] / [password] 可选鉴权，两者同时提供才生效。
  /// [cleanSession] 是否清除会话，默认 true。
  /// [keepAlivePeriod] 心跳间隔（秒），默认 60（阿里云 IoT 要求 30~1200）。
  Future<void> connect({
    required String host,
    int port = 1883,
    String? clientId,
    String? username,
    String? password,
    bool cleanSession = true,
    int keepAlivePeriod = 60,
    MqttClientListener? listener,
  }) async {
    _listener = listener;
    _connected = false;
    _releaseOldClient();

    final String resolvedClientId =
        clientId ?? 'android_${DateTime.now().millisecondsSinceEpoch}';

    final MqttServerClient client =
        MqttServerClient.withPort(host, resolvedClientId, port);
    client.setProtocolV311();
    client.keepAlivePeriod = keepAlivePeriod;
    client.connectTimeoutPeriod = 5000;
    client.autoReconnect = true;
    client.onConnected = _onConnected;
    client.onDisconnected = _onDisconnected;
    client.onAutoReconnected = _onAutoReconnected;
    client.onSubscribeFail = _onSubscribeFail;

    final MqttConnectMessage message =
        MqttConnectMessage().withClientIdentifier(resolvedClientId);
    if (cleanSession) {
      message.startClean();
    }
    if (username != null && password != null) {
      message.authenticateAs(username, password);
    }
    client.connectionMessage = message;

    try {
      await client.connect().timeout(const Duration(seconds: 10));
    } catch (error) {
      _listener?.onError('connect failed: $error');
      return;
    }

    _client = client;
    _subscription = client.updates!.listen(_onUpdates);
  }

  /// 订阅主题。
  void subscribe(String topic, {int qos = 1}) {
    final MqttServerClient? client = _client;
    if (client == null) return;
    client.subscribe(topic, _toQos(qos));
  }

  /// 发布消息。
  void publish(String topic, String payload, {int qos = 1}) {
    final MqttServerClient? client = _client;
    if (client == null) return;
    final MqttClientPayloadBuilder builder = MqttClientPayloadBuilder();
    builder.addString(payload);
    client.publishMessage(topic, _toQos(qos), builder.payload!);
  }

  /// 断开连接并释放资源。
  void disconnect() {
    _connected = false;
    _listener = null;
    _releaseOldClient();
  }

  void _releaseOldClient() {
    final MqttServerClient? old = _client;
    _client = null;
    _subscription?.cancel();
    _subscription = null;
    if (old == null) return;
    try {
      old.disconnect();
    } catch (_) {
      // ignore
    }
  }

  void _onConnected() {
    _connected = true;
    _listener?.onConnectSuccess(false);
  }

  void _onDisconnected() {
    _connected = false;
    _listener?.onConnectionLost();
  }

  void _onAutoReconnected() {
    _connected = true;
    _listener?.onConnectSuccess(true);
  }

  void _onSubscribeFail(String topic) {
    _listener?.onError('subscribe failed: $topic');
  }

  void _onUpdates(List<MqttReceivedMessage<MqttMessage>> messages) {
    for (final MqttReceivedMessage<MqttMessage> received in messages) {
      final MqttMessage message = received.payload;
      if (message is MqttPublishMessage) {
        final String payload = MqttPublishPayload.bytesToStringAsString(
          message.payload.message,
        );
        _listener?.onMessageArrived(received.topic, payload);
      }
    }
  }

  MqttQos _toQos(int qos) {
    return switch (qos) {
      0 => MqttQos.atMostOnce,
      2 => MqttQos.exactlyOnce,
      _ => MqttQos.atLeastOnce,
    };
  }
}
