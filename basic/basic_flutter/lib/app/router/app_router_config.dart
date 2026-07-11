import 'package:auto_route/auto_route.dart' as auto_route;
import 'package:basic_flutter/app/home/app_home.dart';
import 'package:basic_flutter/app/router/app_router_type.dart';
import 'package:basic_flutter/catalog/registry/catalog_registry.dart';
import 'package:basic_flutter/catalog/routing/catalog_route_factory.dart';
import 'package:basic_flutter/core/utils/ui/smart_dialog.dart';
import 'package:flutter/widgets.dart';
import 'package:go_router/go_router.dart' as go_router;

export 'package:basic_flutter/app/router/app_router_type.dart';

final go_router.GoRoute _homeGoRoute = go_router.GoRoute(
  path: '/',
  name: 'Home',
  builder: (context, state) => const AppHome(),
);

final auto_route.AutoRoute _homeAutoRoute = auto_route.NamedRouteDef(
  path: '/',
  name: 'Home',
  builder: (context, data) => const AppHome(),
);

List<NavigatorObserver> _createNavigatorObservers() {
  return AppSmartDialog.createNavigatorObservers();
}

/// GoRouter instance (using CatalogRouteFactory)
final go_router.GoRouter goAppRouter = go_router.GoRouter(
  initialLocation: '/',
  observers: _createNavigatorObservers(),
  routes: <go_router.GoRoute>[
    _homeGoRoute,
    ...CatalogRouteFactory.toGoRoutes(catalogRegistry),
  ],
);

/// AutoRoute instance (using CatalogRouteFactory)
final RouterConfig<Object> autoAppRouter =
    auto_route.RootStackRouter.build(
          routes: <auto_route.AutoRoute>[
            _homeAutoRoute,
            ...CatalogRouteFactory.toAutoRoutes(catalogRegistry),
          ],
        ).config(navigatorObservers: _createNavigatorObservers)
        as RouterConfig<Object>;

/// App main router
final RouterConfig<Object> appRouterConfig = switch (currentAppRouterType) {
  AppRouterType.goRouter => goAppRouter,
  AppRouterType.autoRoute => autoAppRouter,
};
