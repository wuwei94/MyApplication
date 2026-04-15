import 'package:basic_flutter/features/network/dio_example.dart';
import 'package:basic_flutter/features/network/http_example.dart';
import 'package:basic_flutter/features/network/image_example.dart';
import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:flutter/widgets.dart';

/// Network 模块
/// 
/// 包含：Dio、Http、图片加载等网络请求示例
class NetworkModule {
  const NetworkModule._();

  /// 首页目录入口
  RouteItem get catalog => RouteItem.section(
        path: '/network',
        title: 'Network',
        subtitle: '网络请求',
        routeItems: routes,
      );

  /// 所有路由列表
  List<RouteItem> get routes => _routes;

  static final List<RouteItem> _routes = [
    RouteItem.page(
      path: 'dio',
      title: 'Dio',
      subtitle: 'Dio网络请求示例',
      pageBuilder: (BuildContext context) => const DioExample(title: 'Dio'),
    ),
    RouteItem.page(
      path: 'http',
      title: 'Http',
      subtitle: 'Http网络请求示例',
      pageBuilder: (BuildContext context) => const HttpExample(title: 'Http'),
    ),
    RouteItem.page(
      path: 'image-loader',
      title: 'ImageLoader',
      subtitle: 'ImageLoader图片加载示例',
      pageBuilder: (BuildContext context) =>
          const ImageExample(title: 'ImageLoader'),
    ),
  ];
}

/// 单例实例
const NetworkModule networkModule = NetworkModule._();
