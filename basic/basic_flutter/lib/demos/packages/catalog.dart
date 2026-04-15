import 'package:basic_flutter/demos/packages/custom_google_font_example.dart';
import 'package:basic_flutter/demos/packages/image_picker_example.dart';
import 'package:basic_flutter/demos/packages/notification_example.dart';
import 'package:basic_flutter/demos/packages/permission_example.dart';
import 'package:basic_flutter/demos/packages/screen_util_example.dart';
import 'package:basic_flutter/demos/packages/toast_example.dart';
import 'package:basic_flutter/demos/packages/webview_example.dart';
import 'package:basic_flutter/demos/packages/wechat_picker_example.dart';
import 'package:basic_flutter/app/catalog/catalog_item.dart';
import 'package:basic_flutter/app/catalog/catalog_section.dart';
import 'package:flutter/widgets.dart';

/// Packages 模块
/// 
/// 包含：Toast、Notification、Permission、ImagePicker、WechatPicker、
/// WebView、ScreenUtil、Google Fonts 等第三方包示例
class PackagesCatalog extends CatalogSection {
  const PackagesCatalog._();

  @override
  String get path => '/packages';

  @override
  String get title => 'Packages';

  @override
  String get subtitle => '三方组件';

  @override
  List<CatalogItem> get items => _items;

  static final List<CatalogItem> _items = [
    CatalogItem.page(
      path: '/packages/toast',
      title: 'Toast',
      subtitle: 'Toast示例',
      pageBuilder: (BuildContext context) => const ToastExample(title: 'Toast'),
    ),
    CatalogItem.page(
      path: '/packages/notification',
      title: 'Notification',
      subtitle: 'Notification示例',
      pageBuilder: (BuildContext context) =>
          const NotificationExample(title: 'Notification'),
    ),
    CatalogItem.page(
      path: '/packages/permission',
      title: 'Permission',
      subtitle: 'Permission示例',
      pageBuilder: (BuildContext context) =>
          const PermissionExample(title: 'Permission'),
    ),
    CatalogItem.page(
      path: '/packages/image-picker',
      title: 'ImagePicker',
      subtitle: 'ImagePicker示例',
      pageBuilder: (BuildContext context) =>
          const ImagePickerExample(title: 'ImagePicker'),
    ),
    CatalogItem.page(
      path: '/packages/wechat-picker',
      title: 'WechatPicker',
      subtitle: 'WechatPicker示例',
      pageBuilder: (BuildContext context) =>
          const WechatPickerExample(title: 'WechatPicker'),
    ),
    CatalogItem.page(
      path: '/packages/web-view',
      title: 'WebView',
      subtitle: 'WebView示例',
      pageBuilder: (BuildContext context) =>
          const WebViewExample(title: 'WebView'),
    ),
    CatalogItem.page(
      path: '/packages/screen-util',
      title: 'ScreenUtil',
      subtitle: 'ScreenUtil示例',
      pageBuilder: (BuildContext context) =>
          const ScreenUtilExample(title: 'ScreenUtil'),
    ),
    CatalogItem.page(
      path: '/packages/custom-google-font',
      title: 'Custom Google Font',
      subtitle: 'Google Fonts 第三方字体示例',
      pageBuilder: (BuildContext context) =>
          const CustomGoogleFontExample(title: 'Custom Google Font'),
    ),
  ];
}

/// 单例实例
const PackagesCatalog packagesCatalog = PackagesCatalog._();
