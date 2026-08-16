import 'package:flutter_demo/demos/basics/getx_app/navigation/models/getx_route_entry.dart';
import 'package:get/get_navigation/src/routes/get_route.dart';

/// 路由转换工具类
/// 用于将 `List<RouteInfo>` 转换为 `List<GetPage>`
class GetXRouteConverter {
  GetXRouteConverter._();

  /// 将 `List<RouteInfo>` 转换为 `List<GetPage>`
  /// 自动过滤掉 path 为空的分组标题项
  static List<GetPage<void>> toGetPage(List<GetXRouteEntry> routeInfos) {
    return routeInfos
        .where((route) => route.name.isNotEmpty)
        .map((route) => route.toGetPage())
        .toList();
  }
}
