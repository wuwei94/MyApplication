import 'package:flutter/widgets.dart';
import 'package:flutter_demo/catalog/models/catalog_entry.dart';
import 'package:flutter_demo/demos/packages/platform/android_id_demo.dart';
import 'package:flutter_demo/demos/packages/platform/connectivity_plus_demo.dart';
import 'package:flutter_demo/demos/packages/platform/device_info_plus_demo.dart';
import 'package:flutter_demo/demos/packages/platform/flutter_udid_demo.dart';
import 'package:flutter_demo/demos/packages/platform/geolocator_demo.dart';
import 'package:flutter_demo/demos/packages/platform/notification_demo.dart';
import 'package:flutter_demo/demos/packages/platform/package_info_plus_demo.dart';
import 'package:flutter_demo/demos/packages/platform/permission_demo.dart';
import 'package:flutter_demo/demos/packages/platform/share_plus_demo.dart';
import 'package:flutter_demo/demos/packages/platform/url_launcher_demo.dart';
import 'package:flutter_demo/demos/packages/platform/webview_demo.dart';

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
      path: 'geolocator',
      title: 'Geolocator',
      subtitle: '定位权限、当前位置读取与定位设置跳转',
      pageBuilder: (BuildContext context) =>
          const GeolocatorDemoPage(title: 'Geolocator'),
    ),
    CatalogEntry.page(
      path: 'device-info-plus',
      title: 'DeviceInfoPlus',
      subtitle: '跨平台设备信息读取与字段展示',
      pageBuilder: (BuildContext context) =>
          const DeviceInfoPlusDemoPage(title: 'DeviceInfoPlus'),
    ),
    CatalogEntry.page(
      path: 'package-info-plus',
      title: 'PackageInfoPlus',
      subtitle: '应用名、包名、版本号与安装来源读取',
      pageBuilder: (BuildContext context) =>
          const PackageInfoPlusDemoPage(title: 'PackageInfoPlus'),
    ),
    CatalogEntry.page(
      path: 'share-plus',
      title: 'SharePlus',
      subtitle: '系统分享面板、文本链接分享与结果状态',
      pageBuilder: (BuildContext context) =>
          const SharePlusDemoPage(title: 'SharePlus'),
    ),
    CatalogEntry.page(
      path: 'android-id',
      title: 'AndroidId',
      subtitle: 'Android 设备标识读取与平台兼容提示',
      pageBuilder: (BuildContext context) =>
          const AndroidIdDemoPage(title: 'AndroidId'),
    ),
    CatalogEntry.page(
      path: 'flutter-udid',
      title: 'FlutterUdid',
      subtitle: '跨平台 UDID 与统一格式标识读取',
      pageBuilder: (BuildContext context) =>
          const FlutterUdidDemoPage(title: 'FlutterUdid'),
    ),
  ],
);
