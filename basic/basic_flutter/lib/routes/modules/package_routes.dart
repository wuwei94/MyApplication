import 'package:basic_flutter/features/packages/my_notification.dart';
import 'package:basic_flutter/features/packages/my_screen_util.dart';
import 'package:basic_flutter/features/packages/my_shared_preferences.dart';
import 'package:basic_flutter/features/packages/my_toast.dart';
import 'package:basic_flutter/features/packages/my_dio.dart';
import 'package:basic_flutter/features/packages/my_http.dart';
import 'package:basic_flutter/routes/models/route_item_model.dart';
import 'package:flutter/widgets.dart';

/// Package 三方框架路由
final List<RouteItem> packageRoutes = [
  RouteItem(
    name: 'Dio',
    path: '/dio',
    describe: 'Dio',
    routeBuilder: (BuildContext context, _) => const MyDio(),
  ),
  RouteItem(
    name: 'Http',
    path: '/http',
    describe: 'Http',
    routeBuilder: (BuildContext context, _) => const MyHttp(),
  ),

  RouteItem(
    name: 'Notification',
    path: '/notification',
    describe: 'Notification',
    routeBuilder: (BuildContext context, _) => const MyNotification(),
  ),
  RouteItem(
    name: 'SharedPreferences',
    path: '/shared-preferences',
    describe: 'SharedPreferences',
    routeBuilder: (BuildContext context, _) => const MySharedPreferences(),
  ),
  RouteItem(
    name: 'Toast',
    path: '/toast',
    describe: 'Toast',
    routeBuilder: (BuildContext context, _) => const MyToast(),
  ),
  RouteItem(
    name: 'ScreenUtil',
    path: '/screen-util',
    describe: 'ScreenUtil',
    routeBuilder: (BuildContext context, _) => const MyScreenUtil(),
  ),
];
