import 'package:basic_flutter/features/packages/dio_example.dart';
import 'package:basic_flutter/features/packages/http_example.dart';
import 'package:basic_flutter/features/packages/notification_example.dart';
import 'package:basic_flutter/features/packages/permission_example.dart';
import 'package:basic_flutter/features/packages/screen_util_example.dart';
import 'package:basic_flutter/features/packages/shared_preferences_example.dart';
import 'package:basic_flutter/features/packages/toast_example.dart';
import 'package:basic_flutter/navigation/models/route_item_model.dart';
import 'package:flutter/widgets.dart';

/// Package 三方框架路由
final List<RouteItem> packageRoutes = [
  RouteItem(
    path: '/package/dio',
    title: 'Dio',
    subtitle: 'Dio',
    routeBuilder: (BuildContext context, _) => const DioExample(),
  ),
  RouteItem(
    path: '/package/http',
    title: 'Http',
    subtitle: 'Http',
    routeBuilder: (BuildContext context, _) => const HttpExample(),
  ),

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
    path: '/package/shared-preferences',
    title: 'SharedPreferences',
    subtitle: 'SharedPreferences',
    routeBuilder: (BuildContext context, _) => const SharedPreferencesExample(),
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
];
