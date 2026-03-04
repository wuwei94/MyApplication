import 'package:basic_flutter/features/1_layout/1_row_page.dart';
import 'package:basic_flutter/features/1_layout/2_column_page.dart';
import 'package:basic_flutter/features/1_layout/4_flexible_page.dart';
import 'package:basic_flutter/features/1_layout/5_expanded_page.dart';
import 'package:basic_flutter/features/1_layout/3_flex_page.dart';
import 'package:basic_flutter/features/1_layout/6_stack_page.dart';
import 'package:basic_flutter/features/1_layout/7_positioned_page.dart';
import 'package:basic_flutter/features/1_layout/8_wrap_page.dart';
import 'package:basic_flutter/features/1_layout/9_flow_page.dart';
import 'package:basic_flutter/routes/models/route_item_model.dart';
import 'package:flutter/widgets.dart';

/// Layout 布局路由
final List<RouteItem> layoutRoutes = [
  RouteItem(
    name: 'Row',
    path: '/row',
    describe: '水平线性布局',
    builder: (BuildContext context, _) => const RowPage(),
  ),
  RouteItem(
    name: 'Column',
    path: '/column',
    describe: '垂直线性布局',
    builder: (BuildContext context, _) => const ColumnPage(),
  ),
  RouteItem(
    name: 'Flex',
    path: '/flex',
    describe: '弹性布局，可控制方向的弹性容器',
    builder: (BuildContext context, _) => const FlexPage(),
  ),
  RouteItem(
    name: 'Flexible',
    path: '/flexible',
    describe: '弹性布局，控制子组件在弹性空间中的占比',
    builder: (BuildContext context, _) => const FlexiblePage(),
  ),
  RouteItem(
    name: 'Expanded',
    path: '/expanded',
    describe: '扩展布局，强制子组件填满剩余空间',
    builder: (BuildContext context, _) => const ExpandedPage(),
  ),
  RouteItem(
    name: 'Stack',
    path: '/stack',
    describe: '堆叠布局，子组件按顺序堆叠显示',
    builder: (BuildContext context, _) => const StackPage(),
  ),
  RouteItem(
    name: 'Positioned',
    path: '/positioned',
    describe: '定位布局，在 Stack 中精确定位子组件',
    builder: (BuildContext context, _) => const PositionedPage(),
  ),
  RouteItem(
    name: 'Wrap',
    path: '/wrap',
    describe: '流式布局，根据子组件大小自动换行',
    builder: (BuildContext context, _) => const WrapPage(),
  ),
  RouteItem(
    name: 'Flow',
    path: '/flow',
    describe: '流式布局，使用自定义 FlowDelegate 实现复杂布局',
    builder: (BuildContext context, _) => const FlowPage(),
  ),
];
