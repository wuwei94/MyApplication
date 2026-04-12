import 'package:basic_flutter/features/layout/state_driven/futurebuilder_example.dart';
import 'package:basic_flutter/features/layout/state_driven/streambuilder_example.dart';
import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:flutter/widgets.dart';

/// State Driven 路由
final List<RouteItem> stateDrivenRoutes = [
  RouteItem(
    path: 'future-builder',
    title: 'FutureBuilder',
    subtitle: '异步数据组件',
    pageBuilder: (BuildContext context) =>
        const FutureBuilderExample(title: 'FutureBuilder'),
  ),
  RouteItem(
    path: 'stream-builder',
    title: 'StreamBuilder',
    subtitle: '流数据组件',
    pageBuilder: (BuildContext context) =>
        const StreamBuilderExample(title: 'StreamBuilder'),
  ),
];
