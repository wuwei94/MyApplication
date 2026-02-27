import 'package:flutter/widgets.dart';
import 'package:go_router/go_router.dart';

class AppRouteInfo {
  String path;
  final Widget Function(BuildContext, GoRouterState)? builder;
  String name;
  String describe;

  AppRouteInfo({
    required this.path,
    this.builder = null,
    this.name = "",
    this.describe = "",
  });

  // 转换为 GoRoute
  GoRoute toGoRoute() => GoRoute(path: path, name: name, builder: builder);
}
