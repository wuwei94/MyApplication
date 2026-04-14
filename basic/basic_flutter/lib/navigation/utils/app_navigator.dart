import 'package:auto_route/auto_route.dart' as auto_route;
import 'package:basic_flutter/navigation/constants/app_router_type.dart';
import 'package:flutter/widgets.dart';
import 'package:go_router/go_router.dart' as go_router;

class AppNavigator {
  AppNavigator._();

  static Future<T?> pushPath<T extends Object?>(
    BuildContext context,
    String path,
  ) {
    return switch (currentAppRouterType) {
      AppRouterType.goRouter =>
        // GoRouter
        go_router.GoRouter.of(context).push<T>(path),
      AppRouterType.autoRoute =>
        // AutoRoute
        auto_route.AutoRouter.of(context).pushPath<T>(path),
    };
  }

  static Future<T?> pushPathWithArgs<T extends Object?>(
    BuildContext context,
    String path, {
    Object? args,
  }) {
    return switch (currentAppRouterType) {
      AppRouterType.goRouter =>
        // GoRouter
        go_router.GoRouter.of(context).push<T>(path, extra: args),
      AppRouterType.autoRoute =>
        // AutoRoute
        auto_route.AutoRouter.of(
          context,
        ).push<T>(auto_route.PageRouteInfo(path, args: args)),
    };
  }
}
