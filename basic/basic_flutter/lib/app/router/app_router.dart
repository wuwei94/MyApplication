import 'package:auto_route/auto_route.dart' as auto_route;
import 'package:basic_flutter/app/catalog/app_catalog.dart';
import 'package:basic_flutter/features/home/home_module.dart';
import 'package:basic_flutter/navigation/constants/app_router_type.dart';
import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:basic_flutter/navigation/utils/route_converter.dart';
import 'package:flutter/widgets.dart';
import 'package:go_router/go_router.dart' as go_router;

export 'package:basic_flutter/navigation/constants/app_router_type.dart';

/// 从所有模块聚合路由
///
/// 遍历 appCatalog 中每个模块的 entry，收集所有 routes
List<RouteItem> get _allRoutes {
  final List<RouteItem> routes = <RouteItem>[];
  for (final RouteItem item in appCatalog) {
    routes.addAll(item.routeItems);
  }
  return routes;
}

/// GoRouter 实例
final go_router.GoRouter goAppRouter = go_router.GoRouter(
  initialLocation: '/home',
  routes: RouteConverter.toGoRoutes(<RouteItem>[
    homeModule.homeRoute,
    ..._allRoutes,
  ]),
);

/// AutoRoute 实例
final RouterConfig<Object> autoAppRouter =
    auto_route.RootStackRouter.build(
          routes: RouteConverter.toAutoRoutes(<RouteItem>[
            homeModule.homeRoute,
            ..._allRoutes,
          ]),
        ).config()
        as RouterConfig<Object>;

/// 应用主路由器
final RouterConfig<Object> appRouter = switch (currentAppRouterType) {
  AppRouterType.goRouter => goAppRouter,
  AppRouterType.autoRoute => autoAppRouter,
};
