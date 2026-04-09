import 'package:basic_flutter/features/demo/custom_local_font_example.dart';
import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:flutter/widgets.dart';

/// Demo 演示路由
final List<RouteItem> demoRoutes = [
  RouteItem(
    path: '/demo/custom-local-font',
    title: 'Custom Local Font',
    subtitle: '本地自定义字体示例',
    pageBuilder: (BuildContext context) =>
        const CustomLocalFontExample(title: 'Custom Local Font'),
  ),
];
