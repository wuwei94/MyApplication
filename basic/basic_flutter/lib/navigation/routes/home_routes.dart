import 'package:basic_flutter/features/feature_list_page.dart';
import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:basic_flutter/navigation/registry/navigation_registry.dart';
import 'package:flutter/widgets.dart';

final List<RouteItem> homeRoutes = [
  RouteItem.page(
    path: '/home',
    title: 'Home',
    pageBuilder: (BuildContext context) {
      return FeatureListPage(title: 'Feature', routes: navigationRegistry);
    },
  ),
];
