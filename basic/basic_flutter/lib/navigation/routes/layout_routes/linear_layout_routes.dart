import 'package:basic_flutter/features/layout/linear_layout/column_example.dart';
import 'package:basic_flutter/features/layout/linear_layout/flexible_expanded_example.dart';
import 'package:basic_flutter/features/layout/linear_layout/row_example.dart';
import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:flutter/widgets.dart';

/// Linear Layout 路由
final List<RouteItem> linearLayoutRoutes = [
  RouteItem(
    path: 'row',
    title: 'Row',
    subtitle: '水平布局组件',
    pageBuilder: (BuildContext context) => const RowExample(title: 'Row'),
  ),
  RouteItem(
    path: 'column',
    title: 'Column',
    subtitle: '垂直布局组件',
    pageBuilder: (BuildContext context) => const ColumnExample(title: 'Column'),
  ),
  RouteItem(
    path: 'flexible-expanded',
    title: 'Flexible & Expanded',
    subtitle: '弹性布局组件',
    pageBuilder: (BuildContext context) =>
        const FlexibleExpandedExample(title: 'Flexible & Expanded'),
  ),
];
