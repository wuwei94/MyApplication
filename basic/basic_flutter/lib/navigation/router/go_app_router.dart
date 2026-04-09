import 'package:basic_flutter/features/feature_list_page.dart';
import 'package:basic_flutter/navigation/constants/navigation_constants.dart';
import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:basic_flutter/navigation/routes/demo_routes.dart';
import 'package:basic_flutter/navigation/routes/navigation_routes.dart';
import 'package:basic_flutter/navigation/routes/home_routes.dart';
import 'package:basic_flutter/navigation/utils/route_converter.dart';
import 'package:go_router/go_router.dart' as go_router;
import 'package:flutter/widgets.dart';

final List<RouteItem> goGroupRoutes = [
  RouteItem(
    path: groupRoutePath,
    title: groupRouteTitle,
    pageBuilder: (BuildContext context) {
      final RouteItem goRouterGroup =
          go_router.GoRouterState.of(context).extra! as RouteItem;
      return FeatureListPage(
        title: goRouterGroup.title,
        routes: goRouterGroup.routeItems,
      );
    },
  ),
];

final go_router.GoRouter goAppRouter = go_router.GoRouter(
  initialLocation: '/',
  routes: [
    ...RouteConverter.toGoRoutes(homeRoutes),
    ...RouteConverter.toGoRoutes(goGroupRoutes),
    ...RouteConverter.toGoRoutes(exampleRoutes),
    ...RouteConverter.toGoRoutes(networkRoutes),
    ...RouteConverter.toGoRoutes(storageRoutes),
    ...RouteConverter.toGoRoutes(animRoutes),
    ...RouteConverter.toGoRoutes(packageRoutes),
    ...RouteConverter.toGoRoutes(videoRoutes),
    ...RouteConverter.toGoRoutes(stateManagerRoutes),
    ...RouteConverter.toGoRoutes(demoRoutes),
  ],
);
