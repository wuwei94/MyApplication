import 'package:basic_flutter/main.dart';
import 'package:basic_flutter/routes/modules/index.dart';
import 'package:basic_flutter/routes/utils/route_converter.dart';
import 'package:go_router/go_router.dart';

// 路由模块化组织
final GoRouter appRouter = GoRouter(
  initialLocation: '/',
  routes: [
    // 首页路由
    GoRoute(path: '/', builder: (context, state) => const HomePage()),
    // 示例路由
    ...RouteConverter.toGoRoutes(exampleRoutes),
    // 布局路由
    ...RouteConverter.toGoRoutes(layoutRoutes),
    // 容器路由
    ...RouteConverter.toGoRoutes(containerRoutes),
    // 滚动路由
    ...RouteConverter.toGoRoutes(scrollingRoutes),
    // 对话框路由
    ...RouteConverter.toGoRoutes(dialogRoutes),
    // 功能路由
    ...RouteConverter.toGoRoutes(functionalRoutes),
    // 动画路由
    ...RouteConverter.toGoRoutes(animationRoutes),
    // 并发路由
    ...RouteConverter.toGoRoutes(concurrencyRoutes),
    // 网络路由
    ...RouteConverter.toGoRoutes(networkRoutes),
    // 状态管理路由
    ...RouteConverter.toGoRoutes(stateManagementRoutes),
    // 第三方包路由
    ...RouteConverter.toGoRoutes(packageRoutes),
  ],
);
