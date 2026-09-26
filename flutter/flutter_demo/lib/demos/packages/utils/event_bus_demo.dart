import 'dart:async';

import 'package:flutter_demo/core/basic/basic.dart';
import 'package:lib_event_bus/lib_event_bus.dart';

/// EventBus — 事件发送与监听
///
/// 核心机制与避坑点：
/// 1. 单例总线：`FlutterEventBus.instance` 发送 / 监听，事件类型即订阅 key。
/// 2. 生命周期：页面订阅必须在 dispose 中 cancel，避免结果回写已卸载 UI。
///
/// 官方参考：
/// https://pub.dev/packages/event_bus
class EventBusDemoPage extends BasicResponsePage {
  const EventBusDemoPage({super.key, required super.title});

  @override
  BasicResponsePageState<EventBusDemoPage> createState() =>
      _EventBusDemoPageState();
}

class _EventBusDemoPageState extends BasicResponsePageState<EventBusDemoPage> {
  StreamSubscription<_CounterChangedEvent>? _counterSubscription;
  StreamSubscription<_MessagePublishedEvent>? _messageSubscription;
  int _counter = 0;

  @override
  void initState() {
    super.initState();
    showDescription('EventBus 示例：发送事件并监听回调');
    _counterSubscription = FlutterEventBus.instance
        .onEvent<_CounterChangedEvent>()
        .listen(_handleCounterEvent);
    _messageSubscription = FlutterEventBus.instance
        .onEvent<_MessagePublishedEvent>()
        .listen(_handleMessageEvent);
  }

  @override
  void dispose() {
    _counterSubscription?.cancel();
    _messageSubscription?.cancel();
    super.dispose();
  }

  @override
  List<String> buildList() => const <String>[
        '1. 发送计数事件',
        '2. 发送消息事件',
        '3. 监听全部事件一次',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _postCounterEvent();
      case 1:
        _postMessageEvent();
      case 2:
        _listenAllEventsOnce();
    }
  }

  void _handleCounterEvent(_CounterChangedEvent event) {
    appendLog('✓ [CounterChangedEvent] value=${event.value}');
  }

  void _handleMessageEvent(_MessagePublishedEvent event) {
    appendLog('✓ [MessagePublishedEvent] ${event.message}');
  }

  void _postCounterEvent() {
    final int next = _counter + 1;
    _counter = next;
    appendLog('→ [post] CounterChangedEvent(value: $next)');
    FlutterEventBus.instance
        .post<_CounterChangedEvent>(_CounterChangedEvent(value: next));
  }

  void _postMessageEvent() {
    final String message = '手动广播消息 #${_counter + 1}';
    appendLog('→ [post] MessagePublishedEvent');
    FlutterEventBus.instance
        .post<_MessagePublishedEvent>(_MessagePublishedEvent(message: message));
  }

  void _listenAllEventsOnce() {
    appendLog('→ [onAllEvents] 订阅一次全部事件');
    StreamSubscription<Object?>? subscription;
    subscription = FlutterEventBus.instance.onAllEvents().listen((Object? event) {
      appendLog('✓ [onAllEvents] ${event.runtimeType}');
      subscription?.cancel();
    });
  }
}

class _CounterChangedEvent {
  const _CounterChangedEvent({required this.value});

  final int value;
}

class _MessagePublishedEvent {
  const _MessagePublishedEvent({required this.message});

  final String message;
}
