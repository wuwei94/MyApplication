import 'package:auto_route/auto_route.dart' as auto_route;
import 'package:flutter/widgets.dart';
import 'package:go_router/go_router.dart' as go_router;

class CatalogItem {
  final String path;
  final String title;
  final String subtitle;
  final List<CatalogItem> children;
  final WidgetBuilder pageBuilder;

  CatalogItem.page({
    required this.path,
    required this.title,
    this.subtitle = "",
    required this.pageBuilder,
  }) : children = const <CatalogItem>[];

  CatalogItem.catalog({
    required this.path,
    required this.title,
    this.subtitle = "",
    required this.children,
  }) : pageBuilder = ((_) => const SizedBox.shrink());

  /// 使用 go_router 跳转
  Future<T?> pushByGo<T extends Object?>(
    BuildContext context, {
    Object? extra,
  }) {
    return go_router.GoRouter.of(context).push<T>(path, extra: extra);
  }

  /// 使用 auto_route 跳转
  Future<T?> pushByAuto<T extends Object?>(
    BuildContext context, {
    Object? args,
  }) {
    return auto_route.AutoRouter.of(
      context,
    ).push<T>(auto_route.PageRouteInfo(path, args: args));
  }

  // 转换为 go_router 的 GoRoute（支持子路由）
  go_router.GoRoute toGoRoute() {
    return go_router.GoRoute(
      path: path,
      name: path,
      builder: (BuildContext context, go_router.GoRouterState state) =>
          pageBuilder(context),
      routes: children
          .where((route) => route.path.isNotEmpty)
          .map((route) => route.toGoRoute())
          .toList(),
    );
  }

  // 转换为 auto_route 的 NamedRouteDef（支持子路由）
  auto_route.AutoRoute toAutoRoute() {
    return auto_route.NamedRouteDef(
      path: path,
      name: path,
      builder: (BuildContext context, auto_route.RouteData<dynamic> data) =>
          pageBuilder(context),
      children: children
          .where((route) => route.path.isNotEmpty)
          .map((route) => route.toAutoRoute())
          .toList(),
    );
  }

  @override
  String toString() {
    return 'CatalogItem('
        'path: $path, '
        'title: $title, '
        'subtitle: $subtitle, '
        'children: $children'
        ')';
  }
}
