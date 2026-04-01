import 'package:basic_flutter/features/features_list_page.dart';
import 'package:basic_flutter/features/home/home_page.dart';
import 'package:basic_flutter/navigation/models/route_item_model.dart';
import 'package:go_router/go_router.dart';

/// 路由转换工具类
/// 用于将 List&lt;RouteInfo&gt; 转换为 List&lt;GoRoute&gt;
class RouteConverter {
  RouteConverter._();

  static const String homePath = '/';
  static const String groupPath = '/group';

  /// 获取首页路由
  static GoRoute getHomeRoute() {
    return GoRoute(
      path: homePath,
      builder: (context, state) => const HomePage(),
    );
  }

  /// 获取分组路由
  static GoRoute getGroupRoute() {
    return GoRoute(
      path: groupPath,
      builder: (context, state) {
        final RouteItem group = state.extra! as RouteItem;
        return FeaturesListPage(title: group.title, routes: group.routeItems);
      },
    );
  }

  /// 将 List&lt;RouteInfo&gt; 转换为 List&lt;GoRoute&gt;
  /// 自动过滤掉 path 为空的分组标题项
  static List<GoRoute> toGoRoutes(List<RouteItem> routeInfos) {
    return routeInfos
        .where((route) => route.path.isNotEmpty)
        .map((route) => route.toGoRoute())
        .toList();
  }
}
