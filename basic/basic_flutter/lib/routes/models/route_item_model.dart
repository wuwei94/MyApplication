import 'package:flutter/widgets.dart';
import 'package:go_router/go_router.dart';

class RouteItem {
  final String path;
  final Widget Function(BuildContext, GoRouterState) builder;
  final String name;
  final String describe;

  RouteItem({
    required this.path,
    required this.builder,
    this.name = "",
    this.describe = "",
  });

  // 转换为 GoRoute
  GoRoute toGoRoute() => GoRoute(path: path, name: name, builder: builder);
}
