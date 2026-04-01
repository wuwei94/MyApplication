import 'package:basic_flutter/features/packages/image_picker_example.dart';
import 'package:basic_flutter/features/packages/notification_example.dart';
import 'package:basic_flutter/features/packages/permission_example.dart';
import 'package:basic_flutter/features/packages/screen_util_example.dart';
import 'package:basic_flutter/features/packages/toast_example.dart';
import 'package:basic_flutter/features/packages/webview_example.dart';
import 'package:basic_flutter/features/packages/wechat_picker_example.dart';
import 'package:basic_flutter/navigation/models/route_item_model.dart';
import 'package:flutter/widgets.dart';

/// Package 三方框架路由
final List<RouteItem> packageRoutes = [
  RouteItem(
    path: '/package/toast',
    title: 'Toast',
    subtitle: 'Toast示例',
    routeBuilder: (BuildContext context, _) =>
        const ToastExample(title: 'Toast'),
  ),
  RouteItem(
    path: '/package/notification',
    title: 'Notification',
    subtitle: 'Notification示例',
    routeBuilder: (BuildContext context, _) =>
        const NotificationExample(title: 'Notification'),
  ),
  RouteItem(
    path: '/package/permission',
    title: 'Permission',
    subtitle: 'Permission示例',
    routeBuilder: (BuildContext context, _) =>
        const PermissionExample(title: 'Permission'),
  ),
  RouteItem(
    path: '/package/image-picker',
    title: 'ImagePicker',
    subtitle: 'ImagePicker示例',
    routeBuilder: (BuildContext context, _) =>
        const ImagePickerExample(title: 'ImagePicker'),
  ),
  RouteItem(
    path: '/package/wechat-picker',
    title: 'WechatPicker',
    subtitle: 'WechatPicker示例',
    routeBuilder: (BuildContext context, _) =>
        const WechatPickerExample(title: 'WechatPicker'),
  ),
  RouteItem(
    path: '/package/webview',
    title: 'WebView',
    subtitle: 'WebView示例',
    routeBuilder: (BuildContext context, _) =>
        const WebViewExample(title: 'WebView'),
  ),
  RouteItem(
    path: '/package/screen-util',
    title: 'ScreenUtil',
    subtitle: 'ScreenUtil示例',
    routeBuilder: (BuildContext context, _) =>
        const ScreenUtilExample(title: 'ScreenUtil'),
  ),
];
