import 'package:auto_route/auto_route.dart' as auto_route;
import 'package:basic_flutter/features/feature_list_page.dart';
import 'package:basic_flutter/navigation/constants/navigation_constants.dart';
import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:basic_flutter/navigation/routes/demo_routes.dart';
import 'package:basic_flutter/navigation/routes/navigation_routes.dart';
import 'package:basic_flutter/navigation/routes/home_routes.dart';
import 'package:basic_flutter/navigation/utils/route_converter.dart';
import 'package:flutter/widgets.dart';

final List<RouteItem> autoGroupRoutes = [
  RouteItem(
    path: groupRoutePath,
    title: groupRouteTitle,
    pageBuilder: (BuildContext context) {
      final RouteItem autoRouteGroup =
          auto_route.RouteData.of(context).args! as RouteItem;
      return FeatureListPage(
        title: autoRouteGroup.title,
        routes: autoRouteGroup.routeItems,
      );
    },
  ),
];

final auto_route.RootStackRouter _autoAppRootRouter =
    auto_route.RootStackRouter.build(
      routes: [
        ...RouteConverter.toAutoRoutes(homeRoutes),
        ...RouteConverter.toAutoRoutes(autoGroupRoutes),
        ...RouteConverter.toAutoRoutes(exampleRoutes),
        ...RouteConverter.toAutoRoutes(networkRoutes),
        ...RouteConverter.toAutoRoutes(storageRoutes),
        ...RouteConverter.toAutoRoutes(animRoutes),
        ...RouteConverter.toAutoRoutes(packageRoutes),
        ...RouteConverter.toAutoRoutes(videoRoutes),
        ...RouteConverter.toAutoRoutes(stateManagerRoutes),
        ...RouteConverter.toAutoRoutes(demoRoutes),
      ],
    );

final RouterConfig<Object> autoAppRouter =
    _autoAppRootRouter.config() as RouterConfig<Object>;
