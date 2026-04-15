import 'package:auto_route/auto_route.dart' as auto_route;
import 'package:basic_flutter/app/catalog/demo_catalog.dart';
import 'package:basic_flutter/app/home/home_page.dart';
import 'package:basic_flutter/app/router/app_router_type.dart';
import 'package:basic_flutter/app/router/catalog_route_converter.dart';
import 'package:flutter/widgets.dart';
import 'package:go_router/go_router.dart' as go_router;

export 'package:basic_flutter/app/router/app_router_type.dart';

final go_router.GoRoute _homeGoRoute = go_router.GoRoute(
  path: '/home',
  name: 'Home',
  builder: (context, state) => const HomePage(),
);

final auto_route.AutoRoute _homeAutoRoute = auto_route.NamedRouteDef(
  path: '/home',
  name: 'Home',
  builder: (context, data) => const HomePage(),
);

/// GoRouter instance
final go_router.GoRouter goAppRouter = go_router.GoRouter(
  initialLocation: '/home',
  routes: <go_router.GoRoute>[
    _homeGoRoute,
    ...CatalogRouteConverter.toGoRoutes(demoCatalog),
  ],
);

/// AutoRoute instance (using CatalogRouteConverter)
final RouterConfig<Object> autoAppRouter =
    auto_route.RootStackRouter.build(
          routes: <auto_route.AutoRoute>[
            _homeAutoRoute,
            ...CatalogRouteConverter.toAutoRoutes(demoCatalog),
          ],
        ).config()
        as RouterConfig<Object>;

/// App main router
final RouterConfig<Object> appRouter = switch (currentAppRouterType) {
  AppRouterType.goRouter => goAppRouter,
  AppRouterType.autoRoute => autoAppRouter,
};
