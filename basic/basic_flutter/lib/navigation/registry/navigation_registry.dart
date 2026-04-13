import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:basic_flutter/navigation/routes/demo_routes.dart';
import 'package:basic_flutter/navigation/routes/layout_routes.dart';
import 'package:basic_flutter/navigation/routes/navigation_routes.dart';

final List<RouteItem> navigationRegistry = <RouteItem>[
  RouteItem.section(
    path: '/examples',
    title: 'Example',
    subtitle: '示例',
    routeItems: exampleRoutes,
  ),
  RouteItem.section(
    path: '/network',
    title: 'Network',
    subtitle: '网络请求',
    routeItems: networkRoutes,
  ),
  RouteItem.section(
    path: '/storage',
    title: 'Storage',
    subtitle: '本地存储',
    routeItems: storageRoutes,
  ),
  RouteItem.section(
    path: '/animations',
    title: 'Animations',
    subtitle: '动画组件',
    routeItems: animRoutes,
  ),
  RouteItem.section(
    path: '/packages',
    title: 'Packages',
    subtitle: '三方组件',
    routeItems: packageRoutes,
  ),
  RouteItem.section(
    path: '/video',
    title: 'Video',
    subtitle: '视频组件',
    routeItems: videoRoutes,
  ),
  RouteItem.section(
    path: '/state-manager',
    title: 'StateManager',
    subtitle: '状态管理',
    routeItems: stateManagerRoutes,
  ),
  RouteItem.section(
    path: '/layout',
    title: 'Layout',
    subtitle: '布局组件',
    routeItems: layoutRoutes,
  ),
  RouteItem.section(
    path: '/demo',
    title: 'Demo',
    subtitle: '演示组件',
    routeItems: demoRoutes,
  ),
];
