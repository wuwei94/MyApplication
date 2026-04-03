import 'package:basic_flutter/navigation/modules/index.dart';
import 'package:basic_flutter/navigation/utils/route_converter.dart';
import 'package:go_router/go_router.dart';

// 路由模块化组织
final GoRouter appGoRouter = GoRouter(
  initialLocation: '/',
  routes: [
    // 首页路由
    ...RouteConverter.toGoRoutes(homeRoutes),
    // 分组路由
    ...RouteConverter.toGoRoutes(goFeaturesRoutes),
    // 示例路由
    ...RouteConverter.toGoRoutes(exampleRoutes),
    // 网络请求路由
    ...RouteConverter.toGoRoutes(networkRoutes),
    // 本地存储路由
    ...RouteConverter.toGoRoutes(storageRoutes),
    // 动画组件路由
    ...RouteConverter.toGoRoutes(animRoutes),
    // 三方组件包路由
    ...RouteConverter.toGoRoutes(packageRoutes),
    // 视频组件路由
    ...RouteConverter.toGoRoutes(videoRoutes),
    // 状态管理路由
    ...RouteConverter.toGoRoutes(stateManagerRoutes),
  ],
);
