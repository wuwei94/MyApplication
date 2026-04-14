import 'package:auto_route/auto_route.dart' as auto_route;
import 'package:basic_flutter/app/catalog/app_catalog.dart';
import 'package:basic_flutter/features/feature_list_page.dart';
import 'package:basic_flutter/features/home/home_module.dart';
import 'package:basic_flutter/navigation/constants/app_router_type.dart';
import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:basic_flutter/navigation/utils/route_converter.dart';
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
  for (final RouteItem catalogItem in appCatalog) {
    // Add group route (displays sub-items list)
    routes.add(
      go_router.GoRoute(
        path: catalogItem.path,
        name: catalogItem.path,
        builder: (BuildContext context, go_router.GoRouterState state) =>
            FeatureListPage(
          title: catalogItem.title,
          routes: catalogItem.routeItems,
        ),
      ),
    );

    // Add all sub-routes (flattened as independent top-level routes)
    routes.addAll(_buildSubGoRoutes(catalogItem.routeItems));
  }

  return routes;
}

/// Build sub-routes list (recursive)
/// Includes both group routes and page routes
List<go_router.GoRoute> _buildSubGoRoutes(List<RouteItem> items) {
  final List<go_router.GoRoute> result = <go_router.GoRoute>[];
  for (final RouteItem item in items) {
    if (item.routeItems.isEmpty) {
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
              FeatureListPage(
            title: item.title,
            routes: item.routeItems,
          ),
        ),
      );
      // Recursively add sub-routes
      result.addAll(_buildSubGoRoutes(item.routeItems));
    }
  }
  return result;
}

/// GoRouter instance
final go_router.GoRouter goAppRouter = go_router.GoRouter(
  initialLocation: '/home',
  routes: _buildGoRoutes(),
);

/// AutoRoute instance (using RouteConverter)
final RouterConfig<Object> autoAppRouter =
    auto_route.RootStackRouter.build(
          routes: RouteConverter.toAutoRoutes(<RouteItem>[
            homeModule.homeRoute,
            ..._collectAllRouteItems(),
          ]),
        ).config()
        as RouterConfig<Object>;

/// Collect all RouteItems (for AutoRoute)
List<RouteItem> _collectAllRouteItems() {
  final List<RouteItem> routes = <RouteItem>[];
  for (final RouteItem catalogItem in appCatalog) {
    routes.add(catalogItem);
  }
  return routes;
}

/// App main router
final RouterConfig<Object> appRouter = switch (currentAppRouterType) {
  AppRouterType.goRouter => goAppRouter,
  AppRouterType.autoRoute => autoAppRouter,
};
