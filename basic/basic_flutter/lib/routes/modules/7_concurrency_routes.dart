import 'package:basic_flutter/features/7_concurrency/my_isolate.dart';
import 'package:basic_flutter/routes/models/route_item_model.dart';
import 'package:flutter/widgets.dart';

/// Concurrency 并发路由
final List<RouteItem> concurrencyRoutes = [
  RouteItem(
    name: 'Isolate',
    path: '/isolate',
    describe: 'Isolate',
    builder: (BuildContext context, _) => const MyIsolate(),
  ),
];
