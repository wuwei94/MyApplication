import 'package:auto_route/auto_route.dart' as auto_route;
import 'package:basic_flutter/navigation/constants/app_router_type.dart';
import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:basic_flutter/navigation/routes/demo_routes.dart';
import 'package:basic_flutter/navigation/routes/home_routes.dart';
import 'package:basic_flutter/navigation/routes/layout_routes.dart';
import 'package:basic_flutter/navigation/routes/navigation_routes.dart';
import 'package:basic_flutter/navigation/utils/route_converter.dart';
import 'package:flutter/widgets.dart';
import 'package:go_router/go_router.dart' as go_router;

export 'package:basic_flutter/navigation/constants/app_router_type.dart';

/// 公共路由列表，用于所有路由器
final List<RouteItem> appRoutes = [
  ...homeRoutes,
  ...exampleModule.routes,
  ...demoModule.routes,
  ...networkModule.routes,
  ...storageModule.routes,
  ...animModule.routes,
  ...packageModule.routes,
  ...videoModule.routes,
  ...stateManagerModule.routes,
  ...layoutModule.routes,
];

/// GoRouter 实例
final go_router.GoRouter goAppRouter = go_router.GoRouter(
  initialLocation: '/home',
  routes: RouteConverter.toGoRoutes(appRoutes),
);

/// AutoRoute 实例
final RouterConfig<Object> autoAppRouter =
    auto_route.RootStackRouter.build(
          routes: RouteConverter.toAutoRoutes(appRoutes),
        ).config()
        as RouterConfig<Object>;

/// 应用主路由器
final RouterConfig<Object> appRouter = switch (currentAppRouterType) {
  AppRouterType.goRouter => goAppRouter,
  AppRouterType.autoRoute => autoAppRouter,
};
