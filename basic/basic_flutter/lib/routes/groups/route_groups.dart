import 'package:basic_flutter/routes/models/route_group_model.dart';
import 'package:basic_flutter/routes/modules/index.dart';

/// 路由分组配置
/// 用于首页展示分组入口
final List<RouteGroup> routeGroups = [
  RouteGroup(name: 'Example', routes: exampleRoutes),
  RouteGroup(name: 'Layout', routes: layoutRoutes),
  RouteGroup(name: 'Container', routes: containerRoutes),
  RouteGroup(name: 'Scrolling', routes: scrollingRoutes),
  RouteGroup(name: 'Functional', routes: functionalRoutes),
  RouteGroup(name: 'Animation', routes: animationRoutes),
  RouteGroup(name: 'Dialog', routes: dialogRoutes),
  RouteGroup(name: 'Concurrency', routes: concurrencyRoutes),
  RouteGroup(name: 'Network', routes: networkRoutes),
  RouteGroup(name: 'State Management', routes: stateManagementRoutes),
  RouteGroup(name: 'Packages', routes: packageRoutes),
];
