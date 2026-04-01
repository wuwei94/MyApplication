import 'package:basic_flutter/features/packages/image_picker_example.dart';
import 'package:basic_flutter/features/packages/lottie_example.dart';
import 'package:basic_flutter/features/packages/notification_example.dart';
import 'package:basic_flutter/features/packages/pag_example.dart';
import 'package:basic_flutter/features/packages/permission_example.dart';
import 'package:basic_flutter/features/packages/screen_util_example.dart';
import 'package:basic_flutter/features/packages/svg_example.dart';
import 'package:basic_flutter/features/packages/svga_example.dart';
import 'package:basic_flutter/features/packages/toast_example.dart';
import 'package:basic_flutter/features/packages/webview_example.dart';
import 'package:basic_flutter/features/packages/wechat_picker_example.dart';
import 'package:basic_flutter/navigation/models/route_item_model.dart';
import 'package:flutter/widgets.dart';

/// Package 三方框架路由
final List<RouteItem> packageRoutes = [
  RouteItem(
    path: '/package/notification',
    title: 'Notification',
    subtitle: 'Notification',
    routeBuilder: (BuildContext context, _) => const NotificationExample(),
  ),
  RouteItem(
    path: '/package/permission',
    title: 'Permission',
    subtitle: 'Permission',
    routeBuilder: (BuildContext context, _) => const PermissionExample(),
  ),
  RouteItem(
    path: '/package/image-picker',
    title: 'ImagePicker',
    subtitle: 'ImagePicker',
    routeBuilder: (BuildContext context, _) => const ImagePickerExample(),
  ),
  RouteItem(
    path: '/package/wechat-picker',
    title: 'WechatPicker',
    subtitle: 'WechatPicker',
    routeBuilder: (BuildContext context, _) => const WechatPickerExample(),
  ),
  RouteItem(
    path: '/package/toast',
    title: 'Toast',
    subtitle: 'Toast',
    routeBuilder: (BuildContext context, _) => const ToastExample(),
  ),
  RouteItem(
    path: '/package/screen-util',
    title: 'ScreenUtil',
    subtitle: 'ScreenUtil',
    routeBuilder: (BuildContext context, _) => const ScreenUtilExample(),
  ),
  RouteItem(
    path: '/package/lottie',
    title: 'Lottie',
    subtitle: 'Lottie Animation',
    routeBuilder: (BuildContext context, _) => const LottieExample(),
  ),
  RouteItem(
    path: '/package/svg',
    title: 'SVG',
    subtitle: 'SVG Image',
    routeBuilder: (BuildContext context, _) => const SvgExample(),
  ),
  RouteItem(
    path: '/package/pag',
    title: 'PAG',
    subtitle: 'PAG Animation',
    routeBuilder: (BuildContext context, _) => const PagExample(),
  ),
  RouteItem(
    path: '/package/svga',
    title: 'SVGA',
    subtitle: 'SVGA Animation',
    routeBuilder: (BuildContext context, _) => const SvgaExample(),
  ),
  RouteItem(
    path: '/package/webview',
    title: 'WebViewFlutter',
    subtitle: 'WebViewFlutter',
    routeBuilder: (BuildContext context, _) => const WebViewExample(),
  ),
];
