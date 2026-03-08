import 'package:basic_flutter/features/8_network/my_dio.dart';
import 'package:basic_flutter/features/8_network/my_http.dart';
import 'package:basic_flutter/routes/models/route_item_model.dart';
import 'package:flutter/widgets.dart';

/// Network 网络请求路由
final List<RouteItem> networkRoutes = [
  RouteItem(
    name: 'Dio',
    path: '/dio',
    describe: 'Dio',
    builder: (BuildContext context, _) => const MyDio(),
  ),
  RouteItem(
    name: 'Http',
    path: '/http',
    describe: 'Http',
    builder: (BuildContext context, _) => const MyHttp(),
  ),
];
