import 'package:auto_route/auto_route.dart';
import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:go_router/go_router.dart';

/// 路由转换工具类
/// 用于将 List&lt;RouteInfo&gt; 转换为 List&lt;GoRoute&gt;
class RouteConverter {
  RouteConverter._();

  /// 将 List&lt;RouteInfo&gt; 转换为 List&lt;GoRoute&gt;
  /// 自动过滤掉 path 为空的分组标题项
  static List<GoRoute> toGoRoutes(List<RouteItem> routeInfos) {
    return routeInfos
        .where((route) => route.path.isNotEmpty)
        .map((route) => route.toGoRoute())
        .toList();
  }

  /// 将 List&lt;RouteInfo&gt; 转换为 List&lt;AutoRoute&gt;
  /// 自动过滤掉 path 为空的分组标题项
  static List<AutoRoute> toAutoRoutes(List<RouteItem> routeInfos) {
    return routeInfos
        .where((route) => route.path.isNotEmpty)
        .map((route) => route.toAutoRoute())
        .toList();
  }
}
