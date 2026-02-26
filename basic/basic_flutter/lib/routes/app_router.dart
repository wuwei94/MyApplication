import 'package:basic_flutter/routes/constants/route_groups.dart';
import 'package:go_router/go_router.dart';

import 'package:basic_flutter/routes/route_modules/route_modules.dart';

// 路由模块化组织
final GoRouter appRouter = GoRouter(
  initialLocation: HomeRoutes.home,
  routes: [
    ...homeRoutes,           // 首页路由
    ...exampleRoutes,        // 示例路由
    ...layoutRoutes,         // 布局路由
    ...containerRoutes,      // 容器路由
    ...scrollingRoutes,      // 滚动路由
    ...dialogRoutes,         // 对话框路由
    ...functionalRoutes,     // 功能路由
    ...animationRoutes,      // 动画路由
    ...concurrencyRoutes,    // 并发路由
    ...networkRoutes,        // 网络路由
    ...stateManagementRoutes,// 状态管理路由
    ...packageRoutes,        // 第三方包路由
  ],
);