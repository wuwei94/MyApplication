import 'package:basic_flutter/routes/models/route_item_model.dart';
import 'package:basic_flutter/routes/modules/index.dart';

/// 路由分组配置
/// 用于首页展示分组入口
final List<RouteItem> routeGroups = [
  RouteItem(name: 'Example', describe: "示例", routeItems: exampleRoutes),
  RouteItem(name: 'Packages', describe: "三方组件", routeItems: packageRoutes),
  RouteItem(
    name: 'State Management',
    describe: "状态管理",
    routeItems: stateManagementRoutes,
  ),
];
