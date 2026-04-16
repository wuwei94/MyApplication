import 'package:basic_flutter/catalog/models/catalog_entry.dart';
import 'package:basic_flutter/demos/layout/scroll/animatedlist_example.dart';
import 'package:basic_flutter/demos/layout/scroll/custom_scrollview_example.dart';
import 'package:basic_flutter/demos/layout/scroll/gridview_example.dart';
import 'package:basic_flutter/demos/layout/scroll/listview_example.dart';
import 'package:basic_flutter/demos/layout/scroll/nested_scrollview_example.dart';
import 'package:basic_flutter/demos/layout/scroll/pageview_example.dart';
import 'package:basic_flutter/demos/layout/scroll/single_child_scrollview_example.dart';
import 'package:basic_flutter/demos/layout/scroll/tabbarview_example.dart';
import 'package:flutter/widgets.dart';

final CatalogEntry scrollCatalog = CatalogEntry.catalog(
  path: 'scroll',
  title: '滚动组件',
  subtitle: 'ListView、GridView、PageView、TabBarView、NestedScrollView...',
  children: <CatalogEntry>[
    CatalogEntry.page(
      path: 'list-view',
      title: 'ListView',
      subtitle: '列表滚动组件',
      pageBuilder: (BuildContext context) =>
          const ListViewDemoPage(title: 'ListView'),
    ),
    CatalogEntry.page(
      path: 'grid-view',
      title: 'GridView',
      subtitle: '网格滚动组件',
      pageBuilder: (BuildContext context) =>
          const GridViewDemoPage(title: 'GridView'),
    ),
    CatalogEntry.page(
      path: 'tab-bar-view',
      title: 'TabBarView',
      subtitle: '标签页组件',
      pageBuilder: (BuildContext context) =>
          const TabBarViewDemoPage(title: 'TabBarView'),
    ),
    CatalogEntry.page(
      path: 'nested-scroll-view',
      title: 'NestedScrollView',
      subtitle: '嵌套滚动组件',
      pageBuilder: (BuildContext context) =>
          const NestedScrollViewDemoPage(title: 'NestedScrollView'),
    ),
    CatalogEntry.page(
      path: 'animated-list',
      title: 'AnimatedList',
      subtitle: '动画列表',
      pageBuilder: (BuildContext context) =>
          const AnimatedListDemoPage(title: 'AnimatedList'),
    ),
    CatalogEntry.page(
      path: 'page-view',
      title: 'PageView',
      subtitle: '页面滑动',
      pageBuilder: (BuildContext context) =>
          const PageViewDemoPage(title: 'PageView'),
    ),
    CatalogEntry.page(
      path: 'single-child-scroll-view',
      title: 'SingleChildScrollView',
      subtitle: '单孩子滚动',
      pageBuilder: (BuildContext context) =>
          const SingleChildScrollViewDemoPage(title: 'SingleChildScrollView'),
    ),
    CatalogEntry.page(
      path: 'custom-scroll-view',
      title: 'CustomScrollView',
      subtitle: '自定义滚动',
      pageBuilder: (BuildContext context) =>
          const CustomScrollViewDemoPage(title: 'CustomScrollView'),
    ),
  ],
);
