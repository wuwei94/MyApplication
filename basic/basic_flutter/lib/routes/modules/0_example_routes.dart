import 'package:basic_flutter/features/0_examples/counter/counter_page.dart';
import 'package:basic_flutter/features/0_examples/getX/my_get_app.dart';
import 'package:basic_flutter/routes/models/route_item_model.dart';
import 'package:flutter/widgets.dart';

/// 计数器示例路由
final List<RouteItem> exampleRoutes = [
  RouteItem(
    path: '/example/counter',
    name: '计数器示例',
    describe: '计数器示例',
    routeBuilder: (BuildContext context, _) => const Counter(),
  ),
  RouteItem(
    path: '/example/getX',
    name: 'GetX示例',
    describe: 'GetX示例',
    routeBuilder: (BuildContext context, _) => const MyGetX(),
  ),
];
