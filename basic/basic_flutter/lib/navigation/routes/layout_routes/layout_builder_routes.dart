import 'package:basic_flutter/features/layout/layout_builder/layout_builder_example.dart';
import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:flutter/widgets.dart';

/// Layout Builder 路由
final List<RouteItem> layoutBuilderRoutes = [
  RouteItem.page(
    path: 'layout-builder',
    title: 'LayoutBuilder',
    subtitle: '布局感知',
    pageBuilder: (BuildContext context) =>
        const LayoutBuilderExample(title: 'LayoutBuilder'),
  ),
];
