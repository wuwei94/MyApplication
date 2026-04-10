import 'package:basic_flutter/features/layout/scroll_widgets/animatedlist_example.dart';
import 'package:basic_flutter/features/layout/scroll_widgets/custom_scrollview_example.dart';
import 'package:basic_flutter/features/layout/scroll_widgets/gridview_example.dart';
import 'package:basic_flutter/features/layout/scroll_widgets/listview_example.dart';
import 'package:basic_flutter/features/layout/scroll_widgets/nested_scrollview_example.dart';
import 'package:basic_flutter/features/layout/scroll_widgets/pageview_example.dart';
import 'package:basic_flutter/features/layout/scroll_widgets/single_child_scrollview_example.dart';
import 'package:basic_flutter/features/layout/scroll_widgets/tabbarview_example.dart';
import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:flutter/widgets.dart';

/// Scroll Widgets 路由
final List<RouteItem> scrollWidgetsRoutes = [
  RouteItem(
    path: 'listview',
    title: 'ListView',
    subtitle: '列表滚动组件',
    pageBuilder: (BuildContext context) =>
        const ListViewExample(title: 'ListView'),
  ),
  RouteItem(
    path: 'gridview',
    title: 'GridView',
    subtitle: '网格滚动组件',
    pageBuilder: (BuildContext context) =>
        const GridViewExample(title: 'GridView'),
  ),
  RouteItem(
    path: 'tabbar-view',
    title: 'TabBarView',
    subtitle: '标签页组件',
    pageBuilder: (BuildContext context) =>
        const TabBarViewExample(title: 'TabBarView'),
  ),
  RouteItem(
    path: 'nested-scroll-view',
    title: 'NestedScrollView',
    subtitle: '嵌套滚动组件',
    pageBuilder: (BuildContext context) =>
        const NestedScrollViewExample(title: 'NestedScrollView'),
  ),
  RouteItem(
    path: 'animated-list',
    title: 'AnimatedList',
    subtitle: '动画列表',
    pageBuilder: (BuildContext context) =>
        const AnimatedListExample(title: 'AnimatedList'),
  ),
  RouteItem(
    path: 'page-view',
    title: 'PageView',
    subtitle: '页面滑动',
    pageBuilder: (BuildContext context) =>
        const PageViewExample(title: 'PageView'),
  ),
  RouteItem(
    path: 'single-child-scroll-view',
    title: 'SingleChildScrollView',
    subtitle: '单孩子滚动',
    pageBuilder: (BuildContext context) =>
        const SingleChildScrollViewExample(title: 'SingleChildScrollView'),
  ),
  RouteItem(
    path: 'custom-scroll-view',
    title: 'CustomScrollView',
    subtitle: '自定义滚动',
    pageBuilder: (BuildContext context) =>
        const CustomScrollViewExample(title: 'CustomScrollView'),
  ),
];
