import 'package:event_bus/event_bus.dart';

/// EventBus 单例封装
class FlutterEventBus {
  FlutterEventBus._() : _bus = EventBus();

  final EventBus _bus;

  static final FlutterEventBus _instance = FlutterEventBus._();

  static FlutterEventBus get instance => _instance;

  factory FlutterEventBus() {
    return _instance;
  }

  /// 发送事件
  void post<T>(T event) {
    _bus.fire(event);
  }

  /// 监听指定类型事件
  Stream<T> onEvent<T>() {
    return _bus.on<T>();
  }

  /// 监听所有事件
  Stream<Object?> onAllEvents() {
    return _bus.on();
  }
}
