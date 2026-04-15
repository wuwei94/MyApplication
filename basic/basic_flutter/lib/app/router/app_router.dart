import 'package:auto_route/auto_route.dart' as auto_route;
import 'package:basic_flutter/app/catalog/demo_catalog.dart';
import 'package:basic_flutter/demos/demo_catalog_page.dart';
import 'package:basic_flutter/demos/home/home_module.dart';
import 'package:basic_flutter/app/catalog/catalog_item.dart';
import 'package:basic_flutter/app/router/catalog_route_converter.dart';
import 'package:basic_flutter/navigation/constants/app_router_type.dart';
import 'package:flutter/widgets.dart';
import 'package:go_router/go_router.dart' as go_router;

export 'package:basic_flutter/navigation/constants/app_router_type.dart';

/// Build GoRouter routes list
///
/// Structure:
/// - Home route
/// - Each module's group route (with sub-items list)
/// - All page routes (independent top-level routes)
List<go_router.GoRoute> _buildGoRoutes() {
  final List<go_router.GoRoute> routes = <go_router.GoRoute>[];

  // Home route
  routes.add(
    go_router.GoRoute(
      path: '/home',
      name: '/home',
      builder: (BuildContext context, go_router.GoRouterState state) =>
          homeModule.homeRoute.pageBuilder(context),
    ),
  );

  // Iterate each module
  for (final CatalogItem catalogItem in demoCatalog) {
    // Add group route (displays sub-items list)
    routes.add(
      go_router.GoRoute(
        path: catalogItem.path,
        name: catalogItem.path,
        builder: (BuildContext context, go_router.GoRouterState state) =>
            DemoCatalogPage(
          title: catalogItem.title,
          routes: catalogItem.children,
        ),
      ),
    );

    // Add all sub-routes (flattened as independent top-level routes)
    routes.addAll(_buildSubGoRoutes(catalogItem.children));
  }

  return routes;
}

/// Build sub-routes list (recursive)
/// Includes both group routes and page routes
List<go_router.GoRoute> _buildSubGoRoutes(List<CatalogItem> items) {
  final List<go_router.GoRoute> result = <go_router.GoRoute>[];
  for (final CatalogItem item in items) {
    if (item.children.isEmpty) {
      // Leaf node (page)
      result.add(
        go_router.GoRoute(
          path: item.path,
          name: item.path,
          builder: (BuildContext context, go_router.GoRouterState state) =>
              item.pageBuilder(context),
        ),
      );
    } else {
      // Group node, add group route and recursively process sub-routes
      result.add(
        go_router.GoRoute(
          path: item.path,
          name: item.path,
          builder: (BuildContext context, go_router.GoRouterState state) =>
              DemoCatalogPage(
            title: item.title,
            routes: item.children,
          ),
        ),
      );
      // Recursively add sub-routes
      result.addAll(_buildSubGoRoutes(item.children));
    }
  }
  return result;
}

/// GoRouter instance
final go_router.GoRouter goAppRouter = go_router.GoRouter(
  initialLocation: '/home',
  routes: _buildGoRoutes(),
);

/// AutoRoute instance (using CatalogRouteConverter)
final RouterConfig<Object> autoAppRouter =
    auto_route.RootStackRouter.build(
          routes: CatalogRouteConverter.toAutoRoutes(<CatalogItem>[
            homeModule.homeRoute,
            ..._collectAllRouteItems(),
          ]),
        ).config()
        as RouterConfig<Object>;

/// Collect all catalog items (for AutoRoute)
List<CatalogItem> _collectAllRouteItems() {
  final List<CatalogItem> routes = <CatalogItem>[];
  for (final CatalogItem catalogItem in demoCatalog) {
    routes.add(catalogItem);
  }
  return routes;
}

/// App main router
final RouterConfig<Object> appRouter = switch (currentAppRouterType) {
  AppRouterType.goRouter => goAppRouter,
  AppRouterType.autoRoute => autoAppRouter,
};
