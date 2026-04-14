import 'package:auto_route/auto_route.dart' as auto_route;
import 'package:basic_flutter/features/feature_list_page.dart';
import 'package:basic_flutter/navigation/constants/app_router_type.dart';
import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:flutter/cupertino.dart';
import 'package:go_router/go_router.dart' as go_router;

final List<RouteItem> featuresRoutes = [
  // RouteItem(
  //   path: 'groupRoutePath',
  //   title: 'groupRouteTitle',
  //   pageBuilder: (BuildContext context) {
  //     final RouteItem group = switch (currentAppRouterType) {
  //       AppRouterType.goRouter =>
  //         go_router.GoRouterState.of(context).extra! as RouteItem,
  //       AppRouterType.autoRoute =>
  //         auto_route.RouteData.of(context).args! as RouteItem,
  //     };
  //
  //     return FeatureListPage(title: group.title, routes: group.routeItems);
  //   },
  // ),
];
