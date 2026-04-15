import 'package:auto_route/auto_route.dart' as auto_route;
import 'package:basic_flutter/app/catalog/catalog_item.dart';
import 'package:basic_flutter/app/catalog/demo_catalog_page.dart';
import 'package:flutter/widgets.dart';
import 'package:go_router/go_router.dart' as go_router;

/// 目录树路由转换工具类
/// 用于将 `List<CatalogItem>` 转换为 `GoRoute` / `AutoRoute`
class CatalogRouteConverter {
  CatalogRouteConverter._();

  /// 将 `List<CatalogItem>` 转换为 `List<GoRoute>`
  static List<go_router.GoRoute> toGoRoutes(List<CatalogItem> routeInfos) {
    return _flattenItems(routeInfos).map(_toGoRoute).toList(growable: false);
  }

  /// 将 `List<CatalogItem>` 转换为 `List<AutoRoute>`
  static List<auto_route.AutoRoute> toAutoRoutes(List<CatalogItem> routeInfos) {
    return _flattenItems(routeInfos).map(_toAutoRoute).toList(growable: false);
  }

  static go_router.GoRoute _toGoRoute(CatalogItem routeItem) {
    return go_router.GoRoute(
      path: routeItem.path,
      name: routeItem.path,
      builder: (BuildContext context, go_router.GoRouterState state) =>
          _buildPage(context, routeItem),
    );
  }

  static auto_route.AutoRoute _toAutoRoute(CatalogItem routeItem) {
    return auto_route.NamedRouteDef(
      path: routeItem.path,
      name: routeItem.path,
      builder: (BuildContext context, auto_route.RouteData<dynamic> data) =>
          _buildPage(context, routeItem),
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

  static List<CatalogItem> _flattenItems(List<CatalogItem> items) {
    final List<CatalogItem> flattened = <CatalogItem>[];

    void visit(List<CatalogItem> currentItems) {
      for (final CatalogItem item in currentItems) {
        if (item.path.isEmpty) {
          continue;
        }

        flattened.add(item);
        if (item.children.isNotEmpty) {
          visit(item.children);
        }
      }
    }

    visit(items);
    return flattened;
  }
}
