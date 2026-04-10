import 'package:basic_flutter/features/layout/stack_positioning/positioned_example.dart';
import 'package:basic_flutter/features/layout/stack_positioning/stack_example.dart';
import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:flutter/widgets.dart';

/// Stack & Positioning 路由
final List<RouteItem> stackPositioningRoutes = [
  RouteItem(
    path: 'stack',
    title: 'Stack',
    subtitle: '堆叠布局组件',
    pageBuilder: (BuildContext context) => const StackExample(title: 'Stack'),
  ),
  RouteItem(
    path: 'positioned',
    title: 'Positioned',
    subtitle: '绝对定位组件',
    pageBuilder: (BuildContext context) =>
        const PositionedExample(title: 'Positioned'),
  ),
];
