import 'package:basic_flutter/catalog/models/catalog_entry.dart';
import 'package:basic_flutter/demos/packages/layout/auto_size_text_example.dart';
import 'package:basic_flutter/demos/packages/layout/constraint_layout_example.dart';
import 'package:basic_flutter/demos/packages/layout/easy_paging_example.dart';
import 'package:basic_flutter/demos/packages/layout/easy_refresh_example.dart';
import 'package:basic_flutter/demos/packages/layout/infinite_scroll_pagination_example.dart';
import 'package:basic_flutter/demos/packages/layout/keyboard_visibility_example.dart';
import 'package:basic_flutter/demos/packages/layout/screen_util_example.dart';
import 'package:basic_flutter/demos/packages/layout/scroll_to_index_example.dart';
import 'package:basic_flutter/demos/packages/layout/slidable_example.dart';
import 'package:basic_flutter/demos/packages/layout/slider_captcha_example.dart';
import 'package:basic_flutter/demos/packages/layout/staggered_grid_view_example.dart';
import 'package:flutter/widgets.dart';

final CatalogEntry packagesLayoutCatalog = CatalogEntry.catalog(
  path: 'layout',
  title: 'Layout',
  subtitle: '适配、交互、滚动与视觉布局',
  children: <CatalogEntry>[
    CatalogEntry.page(
      path: 'screen-util',
      title: 'ScreenUtil',
      subtitle: '屏幕适配与尺寸换算',
      pageBuilder: (BuildContext context) =>
          const ScreenUtilDemoPage(title: 'ScreenUtil'),
    ),
    CatalogEntry.page(
      path: 'constraint-layout',
      title: 'ConstraintLayout',
      subtitle: '复杂约束布局编排',
      pageBuilder: (BuildContext context) =>
          const ConstraintLayoutDemoPage(title: 'ConstraintLayout'),
    ),
    CatalogEntry.page(
      path: 'easy-refresh',
      title: 'EasyRefresh',
      subtitle: '下拉刷新与上拉加载',
      pageBuilder: (BuildContext context) =>
          const EasyRefreshDemoPage(title: 'EasyRefresh'),
    ),
    CatalogEntry.page(
      path: 'easy-paging',
      title: 'EasyPaging',
      subtitle: '基于 easy_refresh 的分页列表',
      pageBuilder: (BuildContext context) =>
          const EasyPagingDemoPage(title: 'EasyPaging'),
    ),
    CatalogEntry.page(
      path: 'scroll-to-index',
      title: 'ScrollToIndex',
      subtitle: '按索引定位并滚动到指定项',
      pageBuilder: (BuildContext context) =>
          const ScrollToIndexDemoPage(title: 'ScrollToIndex'),
    ),
    CatalogEntry.page(
      path: 'slidable',
      title: 'Slidable',
      subtitle: '侧滑操作列表项',
      pageBuilder: (BuildContext context) =>
          const SlidableDemoPage(title: 'Slidable'),
    ),
    CatalogEntry.page(
      path: 'staggered-grid-view',
      title: 'StaggeredGridView',
      subtitle: '瀑布流、错落与拼贴网格',
      pageBuilder: (BuildContext context) =>
          const StaggeredGridViewDemoPage(title: 'StaggeredGridView'),
    ),
    CatalogEntry.page(
      path: 'auto-size-text',
      title: 'AutoSizeText',
      subtitle: '文本自适应缩放',
      pageBuilder: (BuildContext context) =>
          const AutoSizeTextDemoPage(title: 'AutoSizeText'),
    ),
    CatalogEntry.page(
      path: 'keyboard-visibility',
      title: 'KeyboardVisibility',
      subtitle: '监听键盘显隐与收起',
      pageBuilder: (BuildContext context) =>
          const KeyboardVisibilityDemoPage(title: 'KeyboardVisibility'),
    ),
    CatalogEntry.page(
      path: 'slider-captcha',
      title: 'SliderCaptcha',
      subtitle: '拼图滑块验证交互',
      pageBuilder: (BuildContext context) =>
          const SliderCaptchaDemoPage(title: 'SliderCaptcha'),
    ),
    CatalogEntry.page(
      path: 'infinite-scroll-pagination',
      title: 'InfiniteScrollPagination',
      subtitle: '无限滚动分页列表',
      pageBuilder: (BuildContext context) =>
          const InfiniteScrollPaginationDemoPage(
            title: 'InfiniteScrollPagination',
          ),
    ),
  ],
);
