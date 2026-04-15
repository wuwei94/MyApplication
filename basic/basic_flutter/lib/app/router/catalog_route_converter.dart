import 'package:auto_route/auto_route.dart' as auto_route;
import 'package:basic_flutter/app/catalog/catalog_item.dart';
import 'package:basic_flutter/demos/demo_catalog_page.dart';
import 'package:flutter/widgets.dart';
import 'package:go_router/go_router.dart' as go_router;

/// 目录树路由转换工具类
/// 用于将 `List<CatalogItem>` 转换为 `GoRoute` / `AutoRoute`
class CatalogRouteConverter {
  CatalogRouteConverter._();

  /// 将 `List<CatalogItem>` 转换为 `List<GoRoute>`
  /// 自动过滤掉 path 为空的分组标题项
  static List<go_router.GoRoute> toGoRoutes(List<CatalogItem> routeInfos) {
    return routeInfos
        .where((route) => route.path.isNotEmpty)
        .map(_toGoRoute)
        .toList();
  }

  /// 将 `List<CatalogItem>` 转换为 `List<AutoRoute>`
  /// 自动过滤掉 path 为空的分组标题项
  static List<auto_route.AutoRoute> toAutoRoutes(List<CatalogItem> routeInfos) {
    return routeInfos
        .where((route) => route.path.isNotEmpty)
        .map(_toAutoRoute)
        .toList();
  }

  static go_router.GoRoute _toGoRoute(CatalogItem routeItem) {
    return go_router.GoRoute(
      path: routeItem.path,
      name: routeItem.path,
      builder: (BuildContext context, go_router.GoRouterState state) =>
          _buildPage(context, routeItem),
      routes: routeItem.children
          .where((route) => route.path.isNotEmpty)
          .map(_toGoRoute)
          .toList(),
    );
  }

  static auto_route.AutoRoute _toAutoRoute(CatalogItem routeItem) {
    return auto_route.NamedRouteDef(
      path: routeItem.path,
      name: routeItem.path,
      builder: (BuildContext context, auto_route.RouteData<dynamic> data) =>
          _buildPage(context, routeItem),
      children: routeItem.children
          .where((route) => route.path.isNotEmpty)
          .map(_toAutoRoute)
          .toList(),
    );
  }

  static Widget _buildPage(BuildContext context, CatalogItem routeItem) {
    if (routeItem.children.isNotEmpty) {
      return DemoCatalogPage(
        title: routeItem.title,
        routes: routeItem.children,
      );
    }

    return routeItem.pageBuilder(context);
  }
}
