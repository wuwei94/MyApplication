import 'package:auto_route/auto_route.dart' as auto_route;
import 'package:basic_flutter/catalog/services/catalog_tree_resolver.dart';
import 'package:basic_flutter/catalog/models/catalog_entry.dart';
import 'package:basic_flutter/catalog/pages/catalog_page.dart';
import 'package:basic_flutter/catalog/models/resolved_catalog_entry.dart';
import 'package:flutter/widgets.dart';
import 'package:go_router/go_router.dart' as go_router;

/// 目录树路由构建工具类
/// 用于将 `List<CatalogEntry>` 转换为 `GoRoute` / `AutoRoute`
class CatalogRouteFactory {
  CatalogRouteFactory._();

  /// 将 `List<CatalogEntry>` 转换为 `List<GoRoute>`
  static List<go_router.GoRoute> toGoRoutes(List<CatalogEntry> routeInfos) {
    return _flattenItems(CatalogTreeResolver.resolve(routeInfos))
        .map(_toGoRoute)
        .toList(growable: false);
  }

  /// 将 `List<CatalogEntry>` 转换为 `List<AutoRoute>`
  static List<auto_route.AutoRoute> toAutoRoutes(List<CatalogEntry> routeInfos) {
    return _flattenItems(CatalogTreeResolver.resolve(routeInfos))
        .map(_toAutoRoute)
        .toList(growable: false);
  }

  static go_router.GoRoute _toGoRoute(ResolvedCatalogEntry routeItem) {
    return go_router.GoRoute(
      path: routeItem.path,
      name: routeItem.path,
      builder: (BuildContext context, go_router.GoRouterState state) =>
          _buildPage(context, routeItem),
    );
  }

  static auto_route.AutoRoute _toAutoRoute(ResolvedCatalogEntry routeItem) {
    return auto_route.NamedRouteDef(
      path: routeItem.path,
      name: routeItem.path,
      builder: (BuildContext context, auto_route.RouteData<dynamic> data) =>
          _buildPage(context, routeItem),
    );
  }

  static Widget _buildPage(BuildContext context, ResolvedCatalogEntry routeItem) {
    if (routeItem.children.isNotEmpty) {
      return CatalogPage(
        title: routeItem.title,
        routes: routeItem.children,
      );
    }

    return routeItem.pageBuilder(context);
  }

  static List<ResolvedCatalogEntry> _flattenItems(List<ResolvedCatalogEntry> items) {
    final List<ResolvedCatalogEntry> flattened = <ResolvedCatalogEntry>[];

    void visit(List<ResolvedCatalogEntry> currentItems) {
      for (final ResolvedCatalogEntry item in currentItems) {
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
