import 'package:basic_flutter/demos/packages/custom_google_font_example.dart';
import 'package:basic_flutter/demos/packages/event_bus_example.dart';
import 'package:basic_flutter/demos/packages/image_picker_example.dart';
import 'package:basic_flutter/demos/packages/notification_example.dart';
import 'package:basic_flutter/demos/packages/permission_example.dart';
import 'package:basic_flutter/demos/packages/screen_util_example.dart';
import 'package:basic_flutter/demos/packages/toast_example.dart';
import 'package:basic_flutter/demos/packages/webview_example.dart';
import 'package:basic_flutter/demos/packages/wechat_picker_example.dart';
import 'package:basic_flutter/catalog/models/catalog_entry.dart';
import 'package:basic_flutter/catalog/models/catalog_section.dart';
import 'package:flutter/widgets.dart';

/// Packages 模块
///
/// 包含：Toast、Notification、Permission、ImagePicker、WechatPicker、
/// WebView、ScreenUtil、Google Fonts 等第三方包示例
class PackagesCatalog extends CatalogSection {
  const PackagesCatalog._();

  @override
  String get path => 'packages';

  @override
  String get title => 'Packages Example';

  @override
  String get subtitle => '三方组件';

  @override
  List<CatalogEntry> get items => _items;

  static final List<CatalogEntry> _items = [
    CatalogEntry.page(
      path: 'toast',
      title: 'Toast',
      subtitle: 'Toast示例',
      pageBuilder: (BuildContext context) =>
          const ToastDemoPage(title: 'Toast'),
    ),
    CatalogEntry.page(
      path: 'notification',
      title: 'Notification',
      subtitle: 'Notification示例',
      pageBuilder: (BuildContext context) =>
          const NotificationDemoPage(title: 'Notification'),
    ),
    CatalogEntry.page(
      path: 'event-bus',
      title: 'EventBus',
      subtitle: 'EventBus事件总线示例',
      pageBuilder: (BuildContext context) =>
          const EventBusDemoPage(title: 'EventBus'),
    ),
    CatalogEntry.page(
      path: 'permission',
      title: 'Permission',
      subtitle: 'Permission示例',
      pageBuilder: (BuildContext context) =>
          const PermissionDemoPage(title: 'Permission'),
    ),
    CatalogEntry.page(
      path: 'image-picker',
      title: 'ImagePicker',
      subtitle: 'ImagePicker示例',
      pageBuilder: (BuildContext context) =>
          const ImagePickerDemoPage(title: 'ImagePicker'),
    ),
    CatalogEntry.page(
      path: 'wechat-picker',
      title: 'WechatPicker',
      subtitle: 'WechatPicker示例',
      pageBuilder: (BuildContext context) =>
          const WechatPickerDemoPage(title: 'WechatPicker'),
    ),
    CatalogEntry.page(
      path: 'web-view',
      title: 'WebView',
      subtitle: 'WebView示例',
      pageBuilder: (BuildContext context) =>
          const WebViewDemoPage(title: 'WebView'),
    ),
    CatalogEntry.page(
      path: 'screen-util',
      title: 'ScreenUtil',
      subtitle: 'ScreenUtil示例',
      pageBuilder: (BuildContext context) =>
          const ScreenUtilDemoPage(title: 'ScreenUtil'),
    ),
    CatalogEntry.page(
      path: 'custom-google-font',
      title: 'Custom Google Font',
      subtitle: 'Google Fonts 第三方字体示例',
      pageBuilder: (BuildContext context) =>
          const CustomGoogleFontDemoPage(title: 'Custom Google Font'),
    ),
  ];
}

/// 单例实例
const PackagesCatalog packagesCatalog = PackagesCatalog._();
