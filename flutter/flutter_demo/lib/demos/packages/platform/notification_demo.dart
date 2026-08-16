import 'package:flutter/material.dart';
import 'package:flutter_demo/core/utils/ui/notification.dart';

/// Notifications
/// https://pub.dev/packages/flutter_local_notifications
class NotificationDemoPage extends StatelessWidget {
  const NotificationDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return NotificationDemoView(title: title);
  }
}

class NotificationDemoView extends StatefulWidget {
  const NotificationDemoView({super.key, required this.title});

  final String title;

  @override
  State<NotificationDemoView> createState() => _NotificationDemoViewState();
}

class _NotificationDemoViewState extends State<NotificationDemoView> {
  late NotificationHelper _notificationHelper;

  @override
  void initState() {
    super.initState();
    _notificationHelper = NotificationHelper.instance;
    _notificationHelper.initialize();
  }

  void _showNotification() {
    _notificationHelper.showNotification(
      id: 1,
      title: 'Hello',
      body: 'This is a notification!',
      payload: null,
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: getBody(),
      floatingActionButton: getFAB(),
    );
  }

  Widget getBody() {
    return const Center();
  }

  Widget getFAB() {
    return FloatingActionButton(
      onPressed: () => _showNotification(),
      tooltip: 'notification',
      child: const Icon(Icons.add),
    );
  }
}
