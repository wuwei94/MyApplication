import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:basic_flutter/navigation/routes/demo_routes.dart';
import 'package:basic_flutter/navigation/routes/layout_routes.dart';
import 'package:basic_flutter/navigation/routes/navigation_routes.dart';

final List<RouteItem> navigationRegistry = <RouteItem>[
  exampleModule.entry,
  networkModule.entry,
  storageModule.entry,
  animModule.entry,
  packageModule.entry,
  videoModule.entry,
  stateManagerModule.entry,
  layoutModule.entry,
  demoModule.entry,
];
