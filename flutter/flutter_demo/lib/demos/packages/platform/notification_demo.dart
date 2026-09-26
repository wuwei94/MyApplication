import 'package:flutter_demo/core/basic/basic.dart';
import 'package:flutter_demo/core/utils/ui/notification.dart';

/// Notifications — 本地通知展示
///
/// 核心机制与避坑点：
/// 1. 初始化：`NotificationHelper.initialize()` 必须先于 `showNotification`。
/// 2. 触发边界：演示行为统一由下方操作列表触发，不再依赖 FAB。
///
/// 官方参考：
/// https://pub.dev/packages/flutter_local_notifications
class NotificationDemoPage extends BasicResponsePage {
  const NotificationDemoPage({super.key, required super.title});

  @override
  BasicResponsePageState<NotificationDemoPage> createState() =>
      _NotificationDemoPageState();
}

class _NotificationDemoPageState
    extends BasicResponsePageState<NotificationDemoPage> {
  late final NotificationHelper _notificationHelper;
  int _notificationId = 0;

  @override
  void initState() {
    super.initState();
    showDescription('本地通知示例：初始化并展示通知');
    _notificationHelper = NotificationHelper.instance;
    _notificationHelper.initialize();
  }

  @override
  List<String> buildList() => const <String>[
        '1. 展示基础通知',
        '2. 展示带 payload 通知',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _showBasicNotification();
      case 1:
        _showNotificationWithPayload();
    }
  }

  Future<void> _showBasicNotification() async {
    final int id = ++_notificationId;
    appendLog('→ [showNotification] id=$id title=Hello');
    await _notificationHelper.showNotification(
      id: id,
      title: 'Hello',
      body: 'This is a notification!',
      payload: null,
    );
    appendLog('✓ 通知 id=$id 已提交');
  }

  Future<void> _showNotificationWithPayload() async {
    final int id = ++_notificationId;
    appendLog('→ [showNotification] id=$id title=Payload');
    await _notificationHelper.showNotification(
      id: id,
      title: 'Payload',
      body: 'This notification carries a payload.',
      payload: 'demo_payload_$id',
    );
    appendLog('✓ 通知 id=$id payload=demo_payload_$id 已提交');
  }
}
