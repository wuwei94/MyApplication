import 'package:basic_flutter/features/network/dio_example.dart';
import 'package:basic_flutter/features/network/http_example.dart';
import 'package:basic_flutter/features/network/image_example.dart';
import 'package:basic_flutter/navigation/models/route_module.dart';
import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:flutter/widgets.dart';

/// Network 网络请求路由
final RouteModule networkModule = RouteModule(
  entry: RouteItem.section(
    path: '/network',
    title: 'Network',
    subtitle: '网络请求',
    routeItems: networkRoutes,
  ),
  routes: networkRoutes,
);

final List<RouteItem> networkRoutes = [
  RouteItem.page(
    path: '/network/dio',
    title: 'Dio',
    subtitle: 'Dio网络请求示例',
    pageBuilder: (BuildContext context) => const DioExample(title: 'Dio'),
  ),
  RouteItem.page(
    path: '/network/http',
    title: 'Http',
    subtitle: 'Http网络请求示例',
    pageBuilder: (BuildContext context) => const HttpExample(title: 'Http'),
  ),
  RouteItem.page(
    path: '/network/image-loader',
    title: 'ImageLoader',
    subtitle: 'ImageLoader图片加载示例',
    pageBuilder: (BuildContext context) =>
        const ImageExample(title: 'ImageLoader'),
  ),
];
