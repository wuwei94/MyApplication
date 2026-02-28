import 'package:basic_flutter/features/3_scroll/my_animated_list.dart';
import 'package:basic_flutter/features/3_scroll/my_custom_scroll_view.dart';
import 'package:basic_flutter/features/3_scroll/my_grid_view.dart';
import 'package:basic_flutter/features/3_scroll/my_list_view.dart';
import 'package:basic_flutter/features/3_scroll/my_nested_scroll_view.dart';
import 'package:basic_flutter/features/3_scroll/my_page_view.dart';
import 'package:basic_flutter/features/3_scroll/my_scroll_view.dart';
import 'package:basic_flutter/features/3_scroll/my_tab_bar_view.dart';
import 'package:basic_flutter/routes/models/route_item_model.dart';
import 'package:flutter/widgets.dart';

/// Scrolling 可滚动组件路由
final List<RouteItem> scrollingRoutes = [
  RouteItem(
    name: 'ListView',
    path: '/list-view',
    describe: 'ListView',
    builder: (BuildContext context, _) => const MyListView(),
  ),
  RouteItem(
    name: 'GridView',
    path: '/grid-view',
    describe: 'GridView',
    builder: (BuildContext context, _) => const MyGridView(),
  ),
  RouteItem(
    name: 'ScrollView',
    path: '/scroll-view',
    describe: 'ScrollView',
    builder: (BuildContext context, _) => const MyScrollView(),
  ),
  RouteItem(
    name: 'PageView',
    path: '/page-view',
    describe: 'PageView',
    builder: (BuildContext context, _) => const MyPageView(),
  ),
  RouteItem(
    name: 'TabBarView',
    path: '/tab-bar-view',
    describe: 'TabBarView',
    builder: (BuildContext context, _) => const MyTabBarView(),
  ),
  RouteItem(
    name: 'AnimatedList',
    path: '/animated-list',
    describe: 'AnimatedList',
    builder: (BuildContext context, _) => const MyAnimatedList(),
  ),
  RouteItem(
    name: 'CustomScrollView',
    path: '/custom-scroll-view',
    describe: 'CustomScrollView',
    builder: (BuildContext context, _) => const MyCustomScrollView(),
  ),
  RouteItem(
    name: 'NestedScrollView',
    path: '/nested-scroll-view',
    describe: 'NestedScrollView',
    builder: (BuildContext context, _) => const MyNestedScrollView(),
  ),
];
