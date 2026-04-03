import 'package:auto_route/auto_route.dart';
import 'package:basic_flutter/navigation/modules/index.dart';
import 'package:basic_flutter/navigation/utils/route_converter.dart';
import 'package:flutter/widgets.dart';

// 路由模块化组织
final RootStackRouter _appAutoRootRouter = RootStackRouter.build(
  routes: [
    // 首页路由
    ...RouteConverter.toAutoRoutes(homeRoutes),
    // 分组路由
    ...RouteConverter.toAutoRoutes(autoFeaturesRoutes),
    // 示例路由
    ...RouteConverter.toAutoRoutes(exampleRoutes),
    // 网络请求路由
    ...RouteConverter.toAutoRoutes(networkRoutes),
    // 本地存储路由
    ...RouteConverter.toAutoRoutes(storageRoutes),
    // 动画组件路由
    ...RouteConverter.toAutoRoutes(animRoutes),
    // 三方组件包路由
    ...RouteConverter.toAutoRoutes(packageRoutes),
    // 视频组件路由
    ...RouteConverter.toAutoRoutes(videoRoutes),
    // 状态管理路由
    ...RouteConverter.toAutoRoutes(stateManagerRoutes),
  ],
);

final RouterConfig<Object> appAutoRouter =
    _appAutoRootRouter.config() as RouterConfig<Object>;
