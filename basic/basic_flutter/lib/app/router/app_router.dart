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
List<RouteItem> get _allPageRoutes {
  final List<RouteItem> routes = <RouteItem>[];
  for (final RouteItem catalogItem in appCatalog) {
    // 为每个 catalog 项创建分组路由
    routes.add(
      RouteItem.section(
        path: catalogItem.path,
        title: catalogItem.title,
        subtitle: catalogItem.subtitle,
        routeItems: _flattenRoutes(catalogItem.routeItems),
      ),
    );
  }
  return routes;
}

/// 扁平化路由列表，将所有嵌套路由转换为顶层路由
List<RouteItem> _flattenRoutes(List<RouteItem> items) {
  final List<RouteItem> result = <RouteItem>[];
  for (final RouteItem item in items) {
    if (item.routeItems.isEmpty) {
      // 叶子节点（页面）
      result.add(item);
    } else {
      // 分组节点，递归扁平化子路由
      result.addAll(_flattenRoutes(item.routeItems));
    }
  }
  return result;
}

/// GoRouter 实例
final go_router.GoRouter goAppRouter = go_router.GoRouter(
  initialLocation: '/home',
  routes: RouteConverter.toGoRoutes(<RouteItem>[
    homeModule.homeRoute,
    ..._allPageRoutes,
  ]),
);

/// AutoRoute 实例
final RouterConfig<Object> autoAppRouter =
    auto_route.RootStackRouter.build(
          routes: RouteConverter.toAutoRoutes(<RouteItem>[
            homeModule.homeRoute,
            ..._allPageRoutes,
          ]),
        ).config()
        as RouterConfig<Object>;

/// 应用主路由器
final RouterConfig<Object> appRouter = switch (currentAppRouterType) {
  AppRouterType.goRouter => goAppRouter,
  AppRouterType.autoRoute => autoAppRouter,
};
