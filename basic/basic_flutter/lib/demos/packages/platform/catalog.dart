import 'package:basic_flutter/catalog/models/catalog_entry.dart';
import 'package:basic_flutter/demos/packages/platform/android_id_example.dart';
import 'package:basic_flutter/demos/packages/platform/connectivity_plus_example.dart';
import 'package:basic_flutter/demos/packages/platform/device_info_plus_example.dart';
import 'package:basic_flutter/demos/packages/platform/notification_example.dart';
import 'package:basic_flutter/demos/packages/platform/permission_example.dart';
import 'package:basic_flutter/demos/packages/platform/url_launcher_example.dart';
import 'package:basic_flutter/demos/packages/platform/webview_example.dart';
import 'package:flutter/widgets.dart';

final CatalogEntry packagesPlatformCatalog = CatalogEntry.catalog(
  path: 'platform',
  title: 'Platform',
  subtitle: '权限、系统能力与设备信息',
  children: <CatalogEntry>[
    CatalogEntry.page(
      path: 'permission',
      title: 'Permission',
      subtitle: '运行时权限申请、状态读取与设置跳转',
      pageBuilder: (BuildContext context) =>
          const PermissionDemoPage(title: 'Permission'),
    ),
    CatalogEntry.page(
      path: 'notification',
      title: 'Notification',
      subtitle: '本地通知初始化与消息触发',
      pageBuilder: (BuildContext context) =>
          const NotificationDemoPage(title: 'Notification'),
    ),
    CatalogEntry.page(
      path: 'url-launcher',
      title: 'UrlLauncher',
      subtitle: '浏览器、电话、短信、邮件与地图拉起',
      pageBuilder: (BuildContext context) =>
          const UrlLauncherDemoPage(title: 'UrlLauncher'),
    ),
    CatalogEntry.page(
      path: 'web-view',
      title: 'WebView',
      subtitle: '内嵌网页加载、进度展示与刷新',
      pageBuilder: (BuildContext context) =>
          const WebViewDemoPage(title: 'WebView'),
    ),
    CatalogEntry.page(
      path: 'connectivity-plus',
      title: 'ConnectivityPlus',
      subtitle: '网络状态读取、监听与历史记录',
      pageBuilder: (BuildContext context) =>
          const ConnectivityPlusDemoPage(title: 'ConnectivityPlus'),
    ),
    CatalogEntry.page(
      path: 'device-info-plus',
      title: 'DeviceInfoPlus',
      subtitle: '跨平台设备信息读取与字段展示',
      pageBuilder: (BuildContext context) =>
          const DeviceInfoPlusDemoPage(title: 'DeviceInfoPlus'),
    ),
    CatalogEntry.page(
      path: 'android-id',
      title: 'AndroidId',
      subtitle: 'Android 设备标识读取与平台兼容提示',
      pageBuilder: (BuildContext context) =>
          const AndroidIdDemoPage(title: 'AndroidId'),
    ),
  ],
);
