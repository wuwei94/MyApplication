import 'package:basic_flutter/routes/models/route_group_model.dart';
import 'package:basic_flutter/routes/modules/index.dart';

/// 路由分组配置
/// 用于首页展示分组入口
final List<RouteGroup> routeGroups = [
  RouteGroup(name: 'Example', describe: "示例", routeItems: exampleRoutes),
  RouteGroup(name: 'Packages', describe: "第三方组件", routeItems: packageRoutes),
  RouteGroup(
    name: 'State Management',
    describe: "状态管理",
    routeItems: stateManagementRoutes,
  ),
];
