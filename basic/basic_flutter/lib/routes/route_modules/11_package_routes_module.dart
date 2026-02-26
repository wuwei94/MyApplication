import 'package:basic_flutter/routes/constants/route_groups.dart';
import 'package:basic_flutter/packages/my_notification.dart';
import 'package:basic_flutter/packages/my_screen_util.dart';
import 'package:basic_flutter/packages/my_shared_preferences.dart';
import 'package:basic_flutter/packages/my_toast.dart';
import 'package:go_router/go_router.dart';

/// Package 三方框架路由
final List<GoRoute> packageRoutes = [
  GoRoute(
    path: PackageRoutes.toast,
    builder: (context, state) => const MyToast(),
  ),
  GoRoute(
    path: PackageRoutes.notification,
    builder: (context, state) => const MyNotification(),
  ),
  GoRoute(
    path: PackageRoutes.sharedPreferences,
    builder: (context, state) => const MySharedPreferences(),
  ),
  GoRoute(
    path: PackageRoutes.screenUtil,
    builder: (context, state) => const MyScreenUtil(),
  ),
];
