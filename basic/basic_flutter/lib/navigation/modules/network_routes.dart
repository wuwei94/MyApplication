import 'package:basic_flutter/features/network/dio_example.dart';
import 'package:basic_flutter/features/network/http_example.dart';
import 'package:basic_flutter/navigation/models/route_item_model.dart';
import 'package:flutter/widgets.dart';

/// Network 网络请求路由
final List<RouteItem> networkRoutes = [
  RouteItem(
    path: '/network/dio',
    title: 'Dio',
    subtitle: 'Dio',
    routeBuilder: (BuildContext context, _) => const DioExample(),
  ),
  RouteItem(
    path: '/network/http',
    title: 'Http',
    subtitle: 'Http',
    routeBuilder: (BuildContext context, _) => const HttpExample(),
  ),
];
