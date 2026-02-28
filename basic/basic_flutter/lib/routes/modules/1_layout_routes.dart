import 'package:basic_flutter/features/1_layout/my_column.dart';
import 'package:basic_flutter/features/1_layout/my_flex.dart';
import 'package:basic_flutter/features/1_layout/my_flow.dart';
import 'package:basic_flutter/features/1_layout/my_row.dart';
import 'package:basic_flutter/features/1_layout/my_stack.dart';
import 'package:basic_flutter/features/1_layout/my_wrap.dart';
import 'package:basic_flutter/routes/models/route_item_model.dart';
import 'package:flutter/widgets.dart';

/// Layout 布局路由
final List<RouteItem> layoutRoutes = [
  RouteItem(
    name: 'Row',
    path: '/row',
    describe: '水平线性布局',
    builder: (BuildContext context, _) => const MyRow(),
  ),
  RouteItem(
    name: 'Column',
    path: '/column',
    describe: '垂直线性布局',
    builder: (BuildContext context, _) => const MyColumn(),
  ),
  RouteItem(
    name: 'Flex',
    path: '/flex',
    describe: '弹性布局，按照一定比例来分配父容器空间',
    builder: (BuildContext context, _) => const MyFlex(),
  ),
  RouteItem(
    name: 'Wrap',
    path: '/wrap',
    describe: '流式布局，根据子组件大小自动换行的布局',
    builder: (BuildContext context, _) => const MyWrap(),
  ),
  RouteItem(
    name: 'Flow',
    path: '/flow',
    describe: '流式布局，根据子组件大小自动换行的布局',
    builder: (BuildContext context, _) => const MyFlow(),
  ),
  RouteItem(
    name: 'Stack',
    path: '/stack',
    describe: '堆叠布局，根据距父容器四个角的位置来确定自身的位置',
    builder: (BuildContext context, _) => const MyStack(),
  ),
];
