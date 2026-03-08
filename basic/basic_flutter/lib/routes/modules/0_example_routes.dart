import 'package:basic_flutter/features/0_examples/counter/counter_page.dart';
import 'package:basic_flutter/routes/models/route_item_model.dart';
import 'package:flutter/widgets.dart';

/// 计数器示例路由
final List<RouteItem> exampleRoutes = [
  RouteItem(
    name: '计数器',
    path: '/counter',
    describe: '基础计数器示例',
    routeBuilder: (BuildContext context, _) => const Counter(),
  ),
];
