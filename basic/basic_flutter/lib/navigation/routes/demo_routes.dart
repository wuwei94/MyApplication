import 'package:basic_flutter/features/demo/custom_local_font_example.dart';
import 'package:basic_flutter/navigation/models/route_module.dart';
import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:flutter/widgets.dart';

/// Demo 演示路由
final RouteModule demoModule = RouteModule(
  entry: RouteItem.section(
    path: '/demo',
    title: 'Demo',
    subtitle: '演示组件',
    routeItems: demoRoutes,
  ),
  routes: demoRoutes,
);

final List<RouteItem> demoRoutes = [
  RouteItem.page(
    path: 'custom-local-font',
    title: 'Custom Local Font',
    subtitle: '本地自定义字体示例',
    pageBuilder: (BuildContext context) =>
        const CustomLocalFontExample(title: 'Custom Local Font'),
  ),
];
