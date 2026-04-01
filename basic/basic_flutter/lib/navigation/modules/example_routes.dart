import 'package:basic_flutter/features/examples/counter/counter_page.dart';
import 'package:basic_flutter/features/examples/getx/getx_example_app.dart';
import 'package:basic_flutter/navigation/models/route_item_model.dart';
import 'package:flutter/widgets.dart';

/// 计数器示例路由
final List<RouteItem> exampleRoutes = [
  RouteItem(
    path: '/example/counter',
    title: 'Counter Example',
    subtitle: '计数器示例',
    routeBuilder: (BuildContext context, _) =>
        const Counter(title: 'Counter Example'),
  ),
  RouteItem(
    path: '/example/getx',
    title: 'GetX Example',
    subtitle: 'GetX示例',
    routeBuilder: (BuildContext context, _) =>
        const GetXApp(title: 'GetX Example'),
  ),
];
