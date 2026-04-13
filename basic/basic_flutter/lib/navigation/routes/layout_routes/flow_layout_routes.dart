import 'package:basic_flutter/features/layout/flow_layout/flow_example.dart';
import 'package:basic_flutter/features/layout/flow_layout/wrap_example.dart';
import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:flutter/widgets.dart';

/// Flow Layout 路由
final List<RouteItem> flowLayoutRoutes = [
  RouteItem.page(
    path: 'wrap',
    title: 'Wrap',
    subtitle: '流式布局组件',
    pageBuilder: (BuildContext context) => const WrapExample(title: 'Wrap'),
  ),
  RouteItem.page(
    path: 'flow-widget',
    title: 'Flow',
    subtitle: '自定义流式布局',
    pageBuilder: (BuildContext context) => const FlowExample(title: 'Flow'),
  ),
];
