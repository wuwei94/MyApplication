import 'package:flutter/widgets.dart';
import 'package:go_router/go_router.dart';

class RouteItem {
  final String path;
  final String name;
  final String describe;
  final List<RouteItem> routeItems;
  final Widget Function(BuildContext, GoRouterState) routeBuilder;

  RouteItem({
    this.path = "",
    this.name = "",
    this.describe = "",
    this.routeItems = const [],
    Widget Function(BuildContext, GoRouterState)? routeBuilder,
  }) : routeBuilder = routeBuilder ?? ((_, _) => const SizedBox.shrink());

  // 转换为 GoRoute
  GoRoute toGoRoute() => GoRoute(path: path, name: name, builder: routeBuilder);
}
