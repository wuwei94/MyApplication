import 'package:basic_flutter/features/5_functional/my_future_builder.dart';
import 'package:basic_flutter/features/5_functional/my_gesture_detector.dart';
import 'package:basic_flutter/features/5_functional/my_inherited_widget.dart';
import 'package:basic_flutter/features/5_functional/my_layout_builder.dart';
import 'package:basic_flutter/features/5_functional/my_pop_scope.dart';
import 'package:basic_flutter/features/5_functional/my_stream_builder.dart';
import 'package:basic_flutter/features/5_functional/my_value_listenable_builder.dart';
import 'package:basic_flutter/routes/models/route_item_model.dart';
import 'package:flutter/widgets.dart';

/// Functional 功能组件路由
final List<RouteItem> functionalRoutes = [
  RouteItem(
    name: 'LayoutBuilder',
    path: '/layout-builder',
    describe: '获取父组件大小并布局容器',
    builder: (BuildContext context, _) => const MyLayoutBuilder(),
  ),
  RouteItem(
    name: 'GestureDetector',
    path: '/gesture-detector',
    describe: '手势检测',
    builder: (BuildContext context, _) => const MyGestureDetector(),
  ),
  RouteItem(
    name: 'PopScope',
    path: '/pop-scope',
    describe: '返回拦截',
    builder: (BuildContext context, _) => const MyPopScope(),
  ),
  RouteItem(
    name: 'InheritedWidget',
    path: '/inherited-widget',
    describe: '数据共享',
    builder: (BuildContext context, _) => const MyInheritedWidget(),
  ),
  RouteItem(
    name: 'ValueListenableBuilder',
    path: '/value-listenable-builder',
    describe: '数据源监听',
    builder: (BuildContext context, _) => const MyValueListenableBuilder(),
  ),
  RouteItem(
    name: 'FutureBuilder',
    path: '/future-builder',
    describe: '异步UI更新',
    builder: (BuildContext context, _) => const MyFutureBuilder(),
  ),
  RouteItem(
    name: 'StreamBuilder',
    path: '/stream-builder',
    describe: '异步UI更新',
    builder: (BuildContext context, _) => const MyStreamBuilder(),
  ),
];
