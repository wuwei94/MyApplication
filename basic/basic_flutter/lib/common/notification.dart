import 'package:flutter_local_notifications/flutter_local_notifications.dart';

/// 本地通知工具类
class NotificationHelper {
  static NotificationHelper? _instance;
  late final FlutterLocalNotificationsPlugin _notificationsPlugin;

  /// 初始化 NotificationHelper 实例
  NotificationHelper._internal() {
    _notificationsPlugin = FlutterLocalNotificationsPlugin();
  }

  /// 获取单例实例
  static NotificationHelper get instance {
    _instance ??= NotificationHelper._internal();
    return _instance!;
  }

  /// 通知插件实例
  FlutterLocalNotificationsPlugin get notificationsPlugin =>
      _notificationsPlugin;

  /// 初始化通知插件
  Future<void> initialize() async {
    // Android 初始化设置
    const AndroidInitializationSettings initializationSettingsAndroid =
        AndroidInitializationSettings('@mipmap/ic_launcher');
    // iOS 初始化设置
    const DarwinInitializationSettings initializationSettingsIOS =
        DarwinInitializationSettings();
    const InitializationSettings initializationSettings =
        InitializationSettings(
          android: initializationSettingsAndroid,
          iOS: initializationSettingsIOS,
        );
    await _notificationsPlugin.initialize(settings: initializationSettings);
  }

  /// 显示通知
  Future<void> showNotification({
    required int id,
    required String title,
    required String body,
    required String? payload,
  }) async {
    // Android 通知配置
    const AndroidNotificationDetails androidNotificationDetails =
        AndroidNotificationDetails(
          'your.channel.id',
          'your channel name',
          channelDescription: 'your channel description',
          importance: Importance.max,
          priority: Priority.high,
          ticker: 'ticker',
        );

    // iOS 通知配置
    const String darwinNotificationCategoryPlain = 'plainCategory';
    const DarwinNotificationDetails iosNotificationDetails =
        DarwinNotificationDetails(
          categoryIdentifier: darwinNotificationCategoryPlain,
        );

    // 跨平台通知配置
    const NotificationDetails platformChannelSpecifics = NotificationDetails(
      android: androidNotificationDetails,
      iOS: iosNotificationDetails,
    );

    // 显示通知
    await _notificationsPlugin.show(
      id: id,
      title: title,
      body: body,
      notificationDetails: platformChannelSpecifics,
      payload: payload,
    );
  }
}
