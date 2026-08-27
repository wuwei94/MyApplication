import 'package:lib_mqtt/lib_mqtt.dart';
import 'package:test/test.dart';

void main() {
  group('MqttClientManager', () {
    test('instance 与工厂构造返回同一实例', () {
      expect(MqttClientManager.instance, same(MqttClientManager()));
    });

    test('未连接时 isConnected 返回 false', () {
      expect(MqttClientManager.instance.isConnected(), isFalse);
    });
  });

  group('MqttClientListener', () {
    test('默认实现为空操作，不抛异常', () {
      final listener = _TestListener();
      listener.onConnectSuccess(false);
      listener.onConnectionLost();
      listener.onMessageArrived('topic', 'payload');
      listener.onError('error');
    });
  });
}

class _TestListener extends MqttClientListener {}
