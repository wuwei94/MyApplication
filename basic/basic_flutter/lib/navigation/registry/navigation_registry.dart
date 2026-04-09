import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:basic_flutter/navigation/routes/demo_routes.dart';
import 'package:basic_flutter/navigation/routes/layout_routes.dart';
import 'package:basic_flutter/navigation/routes/navigation_routes.dart';

/// 路由注册中心
/// 用于首页展示分组入口
final List<RouteItem> navigationRegistry = [
  RouteItem(title: 'Example', subtitle: "示例", routeItems: exampleRoutes),
  RouteItem(title: 'Network', subtitle: "网络请求", routeItems: networkRoutes),
  RouteItem(title: 'Storage', subtitle: "本地存储", routeItems: storageRoutes),
  RouteItem(title: 'Animations', subtitle: "动画组件", routeItems: animRoutes),
  RouteItem(title: 'Packages', subtitle: "三方组件", routeItems: packageRoutes),
  RouteItem(title: 'Video', subtitle: "视频组件", routeItems: videoRoutes),
  RouteItem(
    title: 'StateManager',
    subtitle: "状态管理",
    routeItems: stateManagerRoutes,
  ),
  RouteItem(title: 'Layout', subtitle: "布局组件", routeItems: layoutRoutes),
  RouteItem(title: 'Demo', subtitle: "演示组件", routeItems: demoRoutes),
];
