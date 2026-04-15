import 'package:basic_flutter/app/catalog/catalog_item.dart';
import 'package:basic_flutter/demos/layout/scroll_widgets/animatedlist_example.dart';
import 'package:basic_flutter/demos/layout/scroll_widgets/custom_scrollview_example.dart';
import 'package:basic_flutter/demos/layout/scroll_widgets/gridview_example.dart';
import 'package:basic_flutter/demos/layout/scroll_widgets/listview_example.dart';
import 'package:basic_flutter/demos/layout/scroll_widgets/nested_scrollview_example.dart';
import 'package:basic_flutter/demos/layout/scroll_widgets/pageview_example.dart';
import 'package:basic_flutter/demos/layout/scroll_widgets/single_child_scrollview_example.dart';
import 'package:basic_flutter/demos/layout/scroll_widgets/tabbarview_example.dart';
import 'package:basic_flutter/demos/layout/sliver_widgets/sliver_appbar_example.dart';
import 'package:basic_flutter/demos/layout/sliver_widgets/sliver_grid_example.dart';
import 'package:basic_flutter/demos/layout/sliver_widgets/sliver_list_example.dart';
import 'package:flutter/widgets.dart';

final List<CatalogItem> layoutScrollingCatalogItems = <CatalogItem>[
  CatalogItem.catalog(
    path: '/layout/scroll',
    title: '滚动组件',
    subtitle: 'ListView、GridView、PageView、TabBarView、NestedScrollView...',
    children: <CatalogItem>[
      CatalogItem.page(
        path: '/layout/scroll/list-view',
        title: 'ListView',
        subtitle: '列表滚动组件',
        pageBuilder: (BuildContext context) =>
            const ListViewExample(title: 'ListView'),
      ),
      CatalogItem.page(
        path: '/layout/scroll/grid-view',
        title: 'GridView',
        subtitle: '网格滚动组件',
        pageBuilder: (BuildContext context) =>
            const GridViewExample(title: 'GridView'),
      ),
      CatalogItem.page(
        path: '/layout/scroll/tab-bar-view',
        title: 'TabBarView',
        subtitle: '标签页组件',
        pageBuilder: (BuildContext context) =>
            const TabBarViewExample(title: 'TabBarView'),
      ),
      CatalogItem.page(
        path: '/layout/scroll/nested-scroll-view',
        title: 'NestedScrollView',
        subtitle: '嵌套滚动组件',
        pageBuilder: (BuildContext context) =>
            const NestedScrollViewExample(title: 'NestedScrollView'),
      ),
      CatalogItem.page(
        path: '/layout/scroll/animated-list',
        title: 'AnimatedList',
        subtitle: '动画列表',
        pageBuilder: (BuildContext context) =>
            const AnimatedListExample(title: 'AnimatedList'),
      ),
      CatalogItem.page(
        path: '/layout/scroll/page-view',
        title: 'PageView',
        subtitle: '页面滑动',
        pageBuilder: (BuildContext context) =>
            const PageViewExample(title: 'PageView'),
      ),
      CatalogItem.page(
        path: '/layout/scroll/single-child-scroll-view',
        title: 'SingleChildScrollView',
        subtitle: '单孩子滚动',
        pageBuilder: (BuildContext context) =>
            const SingleChildScrollViewExample(title: 'SingleChildScrollView'),
      ),
      CatalogItem.page(
        path: '/layout/scroll/custom-scroll-view',
        title: 'CustomScrollView',
        subtitle: '自定义滚动',
        pageBuilder: (BuildContext context) =>
            const CustomScrollViewExample(title: 'CustomScrollView'),
      ),
    ],
  ),
  CatalogItem.catalog(
    path: '/layout/slivers',
    title: 'Sliver 组件',
    subtitle: 'SliverList、SliverGrid、SliverAppBar',
    children: <CatalogItem>[
      CatalogItem.page(
        path: '/layout/slivers/sliver-list',
        title: 'SliverList',
        subtitle: 'Sliver列表',
        pageBuilder: (BuildContext context) =>
            const SliverListExample(title: 'SliverList'),
      ),
      CatalogItem.page(
        path: '/layout/slivers/sliver-grid',
        title: 'SliverGrid',
        subtitle: 'Sliver网格',
        pageBuilder: (BuildContext context) =>
            const SliverGridExample(title: 'SliverGrid'),
      ),
      CatalogItem.page(
        path: '/layout/slivers/sliver-app-bar',
        title: 'SliverAppBar',
        subtitle: 'Sliver应用栏',
        pageBuilder: (BuildContext context) =>
            const SliverAppBarExample(title: 'SliverAppBar'),
      ),
    ],
  ),
];
