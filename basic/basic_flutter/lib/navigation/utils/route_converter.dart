import 'package:auto_route/auto_route.dart' as auto_route;
import 'package:basic_flutter/features/feature_list_page.dart';
import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:flutter/widgets.dart';
import 'package:go_router/go_router.dart' as go_router;

/// 路由转换工具类
/// 用于将 List&lt;RouteInfo&gt; 转换为 List&lt;GoRoute&gt;
class RouteConverter {
  RouteConverter._();

  /// 将 List&lt;RouteInfo&gt; 转换为 List&lt;GoRoute&gt;
  /// 自动过滤掉 path 为空的分组标题项
  static List<go_router.GoRoute> toGoRoutes(List<RouteItem> routeInfos) {
    return routeInfos
        .where((route) => route.path.isNotEmpty)
        .map(_toGoRoute)
        .toList();
  }

  /// 将 List&lt;RouteInfo&gt; 转换为 List&lt;AutoRoute&gt;
  /// 自动过滤掉 path 为空的分组标题项
  static List<auto_route.AutoRoute> toAutoRoutes(List<RouteItem> routeInfos) {
    return routeInfos
        .where((route) => route.path.isNotEmpty)
        .map(_toAutoRoute)
        .toList();
  }

  static go_router.GoRoute _toGoRoute(RouteItem routeItem) {
    return go_router.GoRoute(
      path: routeItem.path,
      name: routeItem.path,
      builder: (BuildContext context, go_router.GoRouterState state) =>
          _buildPage(context, routeItem),
      routes: routeItem.routeItems
          .where((route) => route.path.isNotEmpty)
          .map(_toGoRoute)
          .toList(),
    );
  }

  static auto_route.AutoRoute _toAutoRoute(RouteItem routeItem) {
    return auto_route.NamedRouteDef(
      path: routeItem.path,
      name: routeItem.path,
      builder: (BuildContext context, auto_route.RouteData<dynamic> data) =>
          _buildPage(context, routeItem),
      children: routeItem.routeItems
          .where((route) => route.path.isNotEmpty)
          .map(_toAutoRoute)
          .toList(),
    );
  }

  static Widget _buildPage(BuildContext context, RouteItem routeItem) {
    if (routeItem.routeItems.isNotEmpty) {
      return FeatureListPage(
        title: routeItem.title,
        routes: routeItem.routeItems,
      );
    }

    return routeItem.pageBuilder(context);
  }
}
