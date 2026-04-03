import 'package:auto_route/auto_route.dart' as auto_route;
import 'package:basic_flutter/features/features_list_page.dart';
import 'package:basic_flutter/features/home/home_page.dart';
import 'package:basic_flutter/navigation/models/route_item_model.dart';
import 'package:flutter/widgets.dart';
import 'package:go_router/go_router.dart' as go_router;

export 'example_routes.dart';
export 'network_routes.dart';
export 'anim_routes.dart';
export 'package_routes.dart';
export 'state_manager_routes.dart';
export 'storage_routes.dart';
export 'video_routes.dart';

final List<RouteItem> homeRoutes = [
  RouteItem(
    path: '/',
    title: 'Home',
    pageBuilder: (BuildContext context) {
      return const HomePage();
    },
  ),
];

final List<RouteItem> goFeaturesRoutes = [
  RouteItem(
    path: '/group',
    title: 'FeaturesGroup',
    pageBuilder: (BuildContext context) {
      final RouteItem goRouterGroup =
          go_router.GoRouterState.of(context).extra! as RouteItem;
      return FeaturesListPage(title: goRouterGroup.title, routes: goRouterGroup.routeItems);
    },
  ),
];

final List<RouteItem> autoFeaturesRoutes = [
  RouteItem(
    path: '/group',
    title: 'FeaturesGroup',
    pageBuilder: (BuildContext context) {
      final RouteItem autoRouteGroup =
          auto_route.RouteData.of(context).args! as RouteItem;
      return FeaturesListPage(title: autoRouteGroup.title, routes: autoRouteGroup.routeItems);
    },
  ),
];
