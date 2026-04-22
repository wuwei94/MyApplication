import 'package:basic_flutter/catalog/models/catalog_entry.dart';
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
  subtitle: '权限、网络、系统唤起与嵌入',
  children: <CatalogEntry>[
    CatalogEntry.page(
      path: 'permission',
      title: 'Permission',
      subtitle: '运行时权限申请与状态读取',
      pageBuilder: (BuildContext context) =>
          const PermissionDemoPage(title: 'Permission'),
    ),
    CatalogEntry.page(
      path: 'connectivity-plus',
      title: 'ConnectivityPlus',
      subtitle: '网络连接状态监听',
      pageBuilder: (BuildContext context) =>
          const ConnectivityPlusDemoPage(title: 'ConnectivityPlus'),
    ),
    CatalogEntry.page(
      path: 'device-info-plus',
      title: 'DeviceInfoPlus',
      subtitle: '读取设备与系统信息',
      pageBuilder: (BuildContext context) =>
          const DeviceInfoPlusDemoPage(title: 'DeviceInfoPlus'),
    ),
    CatalogEntry.page(
      path: 'notification',
      title: 'Notification',
      subtitle: '本地系统通知',
      pageBuilder: (BuildContext context) =>
          const NotificationDemoPage(title: 'Notification'),
    ),
    CatalogEntry.page(
      path: 'url-launcher',
      title: 'UrlLauncher',
      subtitle: '链接跳转与系统能力唤起',
      pageBuilder: (BuildContext context) =>
          const UrlLauncherDemoPage(title: 'UrlLauncher'),
    ),
    CatalogEntry.page(
      path: 'web-view',
      title: 'WebView',
      subtitle: '嵌入网页内容',
      pageBuilder: (BuildContext context) =>
          const WebViewDemoPage(title: 'WebView'),
    ),
  ],
);
