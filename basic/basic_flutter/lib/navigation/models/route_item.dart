import 'package:auto_route/auto_route.dart' as auto_route;
import 'package:basic_flutter/navigation/constants/navigation_constants.dart';
import 'package:flutter/widgets.dart';
import 'package:go_router/go_router.dart' as go_router;

class RouteItem {
  final String path;
  final String title;
  final String subtitle;
  final List<RouteItem> routeItems;
  final WidgetBuilder pageBuilder;

  RouteItem({
    this.path = "",
    this.title = "",
    this.subtitle = "",
    this.routeItems = const [],
    WidgetBuilder? pageBuilder,
  }) : pageBuilder = pageBuilder ?? ((_) => const SizedBox.shrink());

  /// 使用 go_router 跳转（自动判断分组或叶子节点）
  Future<T?> pushByGo<T extends Object?>(
    BuildContext context, {
    Object? extra,
  }) {
    final bool isGroup = routeItems.isNotEmpty;
    if (isGroup) {
      return go_router.GoRouter.of(context).push<T>(
        groupRoutePath,
        extra: this,
      );
    }
    return go_router.GoRouter.of(context).push<T>(path, extra: extra);
  }

  /// 使用 auto_route 跳转（自动判断分组或叶子节点）
  Future<T?> pushByAuto<T extends Object?>(
    BuildContext context, {
    Object? args,
  }) {
    final bool isGroup = routeItems.isNotEmpty;
    if (isGroup) {
      return auto_route.AutoRouter.of(
        context,
      ).push<T>(auto_route.PageRouteInfo(groupRouteTitle, args: this));
    }
    return auto_route.AutoRouter.of(
      context,
    ).push<T>(auto_route.PageRouteInfo(title, args: args));
  }

  // 转换为 go_router 的 GoRoute（支持子路由）
  go_router.GoRoute toGoRoute() {
    return go_router.GoRoute(
      path: path,
      name: title,
      builder: (BuildContext context, go_router.GoRouterState state) =>
          pageBuilder(context),
      routes: routeItems
          .where((route) => route.path.isNotEmpty)
          .map((route) => route.toGoRoute())
          .toList(),
    );
  }

  // 转换为 auto_route 的 NamedRouteDef（支持子路由）
  auto_route.AutoRoute toAutoRoute() => auto_route.NamedRouteDef(
    path: path,
    name: title,
    builder: (BuildContext context, auto_route.RouteData<dynamic> data) =>
        pageBuilder(context),
    children: routeItems
        .where((route) => route.path.isNotEmpty)
        .map((route) => route.toAutoRoute())
        .toList(),
  );

  @override
  String toString() {
    return 'RouteItem('
        'path: $path, '
        'title: $title, '
        'subtitle: $subtitle, '
        'routeItems: $routeItems'
        ')';
  }
}
