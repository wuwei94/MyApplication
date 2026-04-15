import 'package:basic_flutter/features/packages/custom_google_font_example.dart';
import 'package:basic_flutter/features/packages/image_picker_example.dart';
import 'package:basic_flutter/features/packages/notification_example.dart';
import 'package:basic_flutter/features/packages/permission_example.dart';
import 'package:basic_flutter/features/packages/screen_util_example.dart';
import 'package:basic_flutter/features/packages/toast_example.dart';
import 'package:basic_flutter/features/packages/webview_example.dart';
import 'package:basic_flutter/features/packages/wechat_picker_example.dart';
import 'package:basic_flutter/navigation/models/route_module.dart';
import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:flutter/widgets.dart';

/// Package 三方框架路由
final RouteModule packageModule = RouteModule(
  entry: RouteItem.section(
    path: '/packages',
    title: 'Packages',
    subtitle: '三方组件',
    routeItems: packageRoutes,
  ),
  routes: packageRoutes,
);

final List<RouteItem> packageRoutes = [
  RouteItem.page(
    path: 'toast',
    title: 'Toast',
    subtitle: 'Toast示例',
    pageBuilder: (BuildContext context) => const ToastExample(title: 'Toast'),
  ),
  RouteItem.page(
    path: 'notification',
    title: 'Notification',
    subtitle: 'Notification示例',
    pageBuilder: (BuildContext context) =>
        const NotificationExample(title: 'Notification'),
  ),
  RouteItem.page(
    path: 'permission',
    title: 'Permission',
    subtitle: 'Permission示例',
    pageBuilder: (BuildContext context) =>
        const PermissionExample(title: 'Permission'),
  ),
  RouteItem.page(
    path: 'image-picker',
    title: 'ImagePicker',
    subtitle: 'ImagePicker示例',
    pageBuilder: (BuildContext context) =>
        const ImagePickerExample(title: 'ImagePicker'),
  ),
  RouteItem.page(
    path: 'wechat-picker',
    title: 'WechatPicker',
    subtitle: 'WechatPicker示例',
    pageBuilder: (BuildContext context) =>
        const WechatPickerExample(title: 'WechatPicker'),
  ),
  RouteItem.page(
    path: 'web-view',
    title: 'WebView',
    subtitle: 'WebView示例',
    pageBuilder: (BuildContext context) =>
        const WebViewExample(title: 'WebView'),
  ),
  RouteItem.page(
    path: 'screen-util',
    title: 'ScreenUtil',
    subtitle: 'ScreenUtil示例',
    pageBuilder: (BuildContext context) =>
        const ScreenUtilExample(title: 'ScreenUtil'),
  ),
  RouteItem.page(
    path: 'custom-google-font',
    title: 'Custom Google Font',
    subtitle: 'Google Fonts 第三方字体示例',
    pageBuilder: (BuildContext context) =>
        const CustomGoogleFontExample(title: 'Custom Google Font'),
  ),
];
