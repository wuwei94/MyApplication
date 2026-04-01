import 'package:basic_flutter/navigation/models/route_item_model.dart';
import 'package:basic_flutter/navigation/modules/index.dart';

/// 路由注册中心
/// 用于首页展示分组入口
final List<RouteItem> routeRegistry = [
  RouteItem(title: 'Example', subtitle: "示例", routeItems: exampleRoutes),
  RouteItem(title: 'Network', subtitle: "网络请求", routeItems: networkRoutes),
  RouteItem(title: 'Storage', subtitle: "本地存储", routeItems: storageRoutes),
  RouteItem(title: 'Animations', subtitle: "动画组件", routeItems: animRoutes),
  RouteItem(title: 'Packages', subtitle: "三方组件", routeItems: packageRoutes),
  RouteItem(
    title: 'StateManager',
    subtitle: "状态管理",
    routeItems: stateManagerRoutes,
  ),
];
