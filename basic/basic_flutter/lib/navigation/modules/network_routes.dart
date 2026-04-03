import 'package:basic_flutter/features/network/dio_example.dart';
import 'package:basic_flutter/features/network/http_example.dart';
import 'package:basic_flutter/features/network/image_example.dart';
import 'package:basic_flutter/navigation/models/route_item_model.dart';
import 'package:flutter/widgets.dart';

/// Network 网络请求路由
final List<RouteItem> networkRoutes = [
  RouteItem(
    path: '/network/dio',
    title: 'Dio',
    subtitle: 'Dio网络请求示例',
    pageBuilder: (BuildContext context) => const DioExample(title: 'Dio'),
  ),
  RouteItem(
    path: '/network/http',
    title: 'Http',
    subtitle: 'Http网络请求示例',
    pageBuilder: (BuildContext context) => const HttpExample(title: 'Http'),
  ),
  RouteItem(
    path: '/network/image',
    title: 'ImageLoader',
    subtitle: 'ImageLoader图片加载示例',
    pageBuilder: (BuildContext context) =>
        const ImageExample(title: 'ImageLoader'),
  ),
];
