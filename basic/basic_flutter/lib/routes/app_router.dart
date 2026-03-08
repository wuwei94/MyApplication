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
    // 第三方包路由
    ...RouteConverter.toGoRoutes(packageRoutes),
    // 状态管理路由
    ...RouteConverter.toGoRoutes(stateManagementRoutes),
  ],
);
