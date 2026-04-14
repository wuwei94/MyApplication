import 'package:basic_flutter/features/packages/custom_google_font_example.dart';
import 'package:basic_flutter/features/packages/image_picker_example.dart';
import 'package:basic_flutter/features/packages/notification_example.dart';
import 'package:basic_flutter/features/packages/permission_example.dart';
import 'package:basic_flutter/features/packages/screen_util_example.dart';
import 'package:basic_flutter/features/packages/toast_example.dart';
import 'package:basic_flutter/features/packages/webview_example.dart';
import 'package:basic_flutter/features/packages/wechat_picker_example.dart';
import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:flutter/widgets.dart';

/// Packages 模块
/// 
/// 包含：Toast、Notification、Permission、ImagePicker、WechatPicker、
/// WebView、ScreenUtil、Google Fonts 等第三方包示例
class PackagesModule {
  const PackagesModule._();

  /// 首页目录入口
  RouteItem get catalog => RouteItem.section(
        path: '/packages',
        title: 'Packages',
        subtitle: '三方组件',
        routeItems: routes,
      );

  /// 所有路由列表
  List<RouteItem> get routes => _routes;

  static final List<RouteItem> _routes = [
    RouteItem.page(
      path: '/package/toast',
      title: 'Toast',
      subtitle: 'Toast示例',
      pageBuilder: (BuildContext context) => const ToastExample(title: 'Toast'),
    ),
    RouteItem.page(
      path: '/package/notification',
      title: 'Notification',
      subtitle: 'Notification示例',
      pageBuilder: (BuildContext context) =>
          const NotificationExample(title: 'Notification'),
    ),
    RouteItem.page(
      path: '/package/permission',
      title: 'Permission',
      subtitle: 'Permission示例',
      pageBuilder: (BuildContext context) =>
          const PermissionExample(title: 'Permission'),
    ),
    RouteItem.page(
      path: '/package/image-picker',
      title: 'ImagePicker',
      subtitle: 'ImagePicker示例',
      pageBuilder: (BuildContext context) =>
          const ImagePickerExample(title: 'ImagePicker'),
    ),
    RouteItem.page(
      path: '/package/wechat-picker',
      title: 'WechatPicker',
      subtitle: 'WechatPicker示例',
      pageBuilder: (BuildContext context) =>
          const WechatPickerExample(title: 'WechatPicker'),
    ),
    RouteItem.page(
      path: '/package/web-view',
      title: 'WebView',
      subtitle: 'WebView示例',
      pageBuilder: (BuildContext context) =>
          const WebViewExample(title: 'WebView'),
    ),
    RouteItem.page(
      path: '/package/screen-util',
      title: 'ScreenUtil',
      subtitle: 'ScreenUtil示例',
      pageBuilder: (BuildContext context) =>
          const ScreenUtilExample(title: 'ScreenUtil'),
    ),
    RouteItem.page(
      path: '/package/custom-google-font',
      title: 'Custom Google Font',
      subtitle: 'Google Fonts 第三方字体示例',
      pageBuilder: (BuildContext context) =>
          const CustomGoogleFontExample(title: 'Custom Google Font'),
    ),
  ];
}

/// 单例实例
const PackagesModule packagesModule = PackagesModule._();
