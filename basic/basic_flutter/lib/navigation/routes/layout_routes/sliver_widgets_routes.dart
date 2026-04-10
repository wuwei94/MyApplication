import 'package:basic_flutter/features/layout/sliver_widgets/sliver_appbar_example.dart';
import 'package:basic_flutter/features/layout/sliver_widgets/sliver_grid_example.dart';
import 'package:basic_flutter/features/layout/sliver_widgets/sliver_list_example.dart';
import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:flutter/widgets.dart';

/// Sliver Widgets 路由
final List<RouteItem> sliverWidgetsRoutes = [
  RouteItem(
    path: 'sliver-list',
    title: 'SliverList',
    subtitle: 'Sliver列表组件',
    pageBuilder: (BuildContext context) =>
        const SliverListExample(title: 'SliverList'),
  ),
  RouteItem(
    path: 'sliver-grid',
    title: 'SliverGrid',
    subtitle: 'Sliver网格组件',
    pageBuilder: (BuildContext context) =>
        const SliverGridExample(title: 'SliverGrid'),
  ),
  RouteItem(
    path: 'sliver-appbar',
    title: 'SliverAppBar',
    subtitle: '折叠导航栏',
    pageBuilder: (BuildContext context) =>
        const SliverAppBarExample(title: 'SliverAppBar'),
  ),
];
