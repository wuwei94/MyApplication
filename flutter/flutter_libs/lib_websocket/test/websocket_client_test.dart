import 'package:lib_websocket/lib_websocket.dart';
import 'package:test/test.dart';

void main() {
  group('WebSocketClient', () {
    test('instance 与工厂构造返回同一实例', () {
      expect(WebSocketClient.instance, same(WebSocketClient()));
    });

    test('未连接时 isConnected 返回 false', () {
      expect(
        WebSocketClient.instance.isConnected('ws://example.com'),
        isFalse,
      );
    });

    test('未连接时 send 返回 false', () {
      expect(
        WebSocketClient.instance.send('ws://example.com', 'hello'),
        isFalse,
      );
    });
  });

  group('WebSocketClientListener', () {
    test('默认实现为空操作，不抛异常', () {
      final listener = _TestListener();
      listener.onOpen();
      listener.onMessage('message');
      listener.onClose(1000, 'normal');
      listener.onError('error');
    });
  });
}

class _TestListener extends WebSocketClientListener {}
