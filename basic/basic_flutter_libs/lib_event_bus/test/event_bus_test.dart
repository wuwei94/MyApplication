import 'package:lib_event_bus/lib_event_bus.dart';
import 'package:test/test.dart';

void main() {
  test('instance 与工厂构造返回同一单例', () {
    expect(FlutterEventBus(), same(FlutterEventBus.instance));
  });

  test('onEvent 只接收指定类型事件', () async {
    final List<String> received = <String>[];

    final Stream<String> stream = FlutterEventBus.instance.onEvent<String>();
    stream.listen(received.add);

    FlutterEventBus.instance.post<String>('hello');

    await Future<void>.delayed(Duration.zero);

    expect(received, <String>['hello']);
  });

  test('不同类型事件互不干扰', () async {
    final List<String> strings = <String>[];
    final List<int> ints = <int>[];

    final Stream<String> stringStream =
        FlutterEventBus.instance.onEvent<String>();
    final Stream<int> intStream = FlutterEventBus.instance.onEvent<int>();
    stringStream.listen(strings.add);
    intStream.listen(ints.add);

    FlutterEventBus.instance.post<String>('only string');

    await Future<void>.delayed(Duration.zero);

    expect(strings, <String>['only string']);
    expect(ints, isEmpty);
  });

  test('onAllEvents 接收任意类型事件', () async {
    final List<Object?> received = <Object?>[];

    final Stream<Object?> stream = FlutterEventBus.instance.onAllEvents();
    stream.listen(received.add);

    FlutterEventBus.instance.post<String>('a');
    FlutterEventBus.instance.post<int>(1);

    await Future<void>.delayed(Duration.zero);

    expect(received, <Object?>['a', 1]);
  });
}
