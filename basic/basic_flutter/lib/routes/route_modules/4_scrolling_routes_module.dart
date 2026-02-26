import 'package:basic_flutter/routes/constants/route_groups.dart';
import 'package:basic_flutter/features/scroll/my_animated_list.dart';
import 'package:basic_flutter/features/scroll/my_custom_scroll_view.dart';
import 'package:basic_flutter/features/scroll/my_grid_view.dart';
import 'package:basic_flutter/features/scroll/my_list_view.dart';
import 'package:basic_flutter/features/scroll/my_nested_scroll_view.dart';
import 'package:basic_flutter/features/scroll/my_page_view.dart';
import 'package:basic_flutter/features/scroll/my_scroll_view.dart';
import 'package:basic_flutter/features/scroll/my_tab_bar_view.dart';
import 'package:go_router/go_router.dart';

/// Scrolling 可滚动组件路由
final List<GoRoute> scrollingRoutes = [
  GoRoute(
    path: ScrollingRoutes.listView,
    builder: (context, state) => const MyListView(),
  ),
  GoRoute(
    path: ScrollingRoutes.gridView,
    builder: (context, state) => const MyGridView(),
  ),
  GoRoute(
    path: ScrollingRoutes.scrollView,
    builder: (context, state) => const MyScrollView(),
  ),
  GoRoute(
    path: ScrollingRoutes.pageView,
    builder: (context, state) => const MyPageView(),
  ),
  GoRoute(
    path: ScrollingRoutes.tabBarView,
    builder: (context, state) => const MyTabBarView(),
  ),
  GoRoute(
    path: ScrollingRoutes.animatedList,
    builder: (context, state) => const MyAnimatedList(),
  ),
  GoRoute(
    path: ScrollingRoutes.customScrollView,
    builder: (context, state) => const MyCustomScrollView(),
  ),
  GoRoute(
    path: ScrollingRoutes.nestedScrollView,
    builder: (context, state) => const MyNestedScrollView(),
  ),
];
