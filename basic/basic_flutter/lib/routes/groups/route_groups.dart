import 'package:basic_flutter/routes/models/route_group_model.dart';
import 'package:basic_flutter/routes/modules/index.dart';

/// 路由分组配置
/// 用于首页展示分组入口
final List<RouteGroup> routeGroups = [
  RouteGroup(name: 'Example', describe: "示例", routeItems: exampleRoutes),
  RouteGroup(name: 'Layout', describe: "布局", routeItems: layoutRoutes),
  RouteGroup(name: 'Container', describe: "容器组件", routeItems: containerRoutes),
  RouteGroup(name: 'Scrolling', describe: "滑动组件", routeItems: scrollingRoutes),
  RouteGroup(
    name: 'Functional',
    describe: "功能组件",
    routeItems: functionalRoutes,
  ),
  RouteGroup(name: 'Animation', describe: "动画组件", routeItems: animationRoutes),
  RouteGroup(name: 'Dialog', describe: "弹窗组件", routeItems: dialogRoutes),
  RouteGroup(
    name: 'Concurrency',
    describe: "并发组件",
    routeItems: concurrencyRoutes,
  ),
  RouteGroup(name: 'Network', describe: "网络请求", routeItems: networkRoutes),
  RouteGroup(
    name: 'State Management',
    describe: "状态管理",
    routeItems: stateManagementRoutes,
  ),
  RouteGroup(name: 'Packages', describe: "第三方组件", routeItems: packageRoutes),
];
