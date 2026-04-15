import 'package:basic_flutter/app/catalog/catalog_item.dart';
import 'package:basic_flutter/demos/layout/async_programming/completer_example.dart';
import 'package:basic_flutter/demos/layout/async_programming/compute_example.dart';
import 'package:basic_flutter/demos/layout/async_programming/future_example.dart';
import 'package:basic_flutter/demos/layout/async_programming/isolate_example.dart';
import 'package:basic_flutter/demos/layout/async_programming/stream_example.dart';
import 'package:basic_flutter/demos/layout/layout_builder/layout_builder_example.dart';
import 'package:basic_flutter/demos/layout/state_driven/futurebuilder_example.dart';
import 'package:basic_flutter/demos/layout/state_driven/streambuilder_example.dart';
import 'package:basic_flutter/demos/layout/state_management/inherited_widget_example.dart';
import 'package:basic_flutter/demos/layout/state_management/listenable_builder_example.dart';
import 'package:basic_flutter/demos/layout/state_management/value_listenable_builder_example.dart';
import 'package:flutter/widgets.dart';

final List<CatalogItem> layoutStateCatalogItems = <CatalogItem>[
  CatalogItem.catalog(
    path: 'builders',
    title: '布局构建器',
    subtitle: 'LayoutBuilder',
    children: <CatalogItem>[
      CatalogItem.page(
        path: 'layout-builder',
        title: 'LayoutBuilder',
        subtitle: '响应式布局',
        pageBuilder: (BuildContext context) =>
            const LayoutBuilderExample(title: 'LayoutBuilder'),
      ),
    ],
  ),
  CatalogItem.catalog(
    path: 'state-driven',
    title: '状态驱动组件',
    subtitle: 'FutureBuilder、StreamBuilder',
    children: <CatalogItem>[
      CatalogItem.page(
        path: 'future-builder',
        title: 'FutureBuilder',
        subtitle: 'Future构建器',
        pageBuilder: (BuildContext context) =>
            const FutureBuilderExample(title: 'FutureBuilder'),
      ),
      CatalogItem.page(
        path: 'stream-builder',
        title: 'StreamBuilder',
        subtitle: 'Stream构建器',
        pageBuilder: (BuildContext context) =>
            const StreamBuilderExample(title: 'StreamBuilder'),
      ),
    ],
  ),
  CatalogItem.catalog(
    path: 'state-management',
    title: '状态管理',
    subtitle: 'InheritedWidget、ValueListenableBuilder、ListenableBuilder',
    children: <CatalogItem>[
      CatalogItem.page(
        path: 'inherited-widget',
        title: 'InheritedWidget',
        subtitle: '数据共享',
        pageBuilder: (BuildContext context) =>
            const InheritedWidgetExample(title: 'InheritedWidget'),
      ),
      CatalogItem.page(
        path: 'value-listenable-builder',
        title: 'ValueListenableBuilder',
        subtitle: '值监听构建器',
        pageBuilder: (BuildContext context) => const ValueListenableBuilderExample(
          title: 'ValueListenableBuilder',
        ),
      ),
      CatalogItem.page(
        path: 'listenable-builder',
        title: 'ListenableBuilder',
        subtitle: '可监听构建器',
        pageBuilder: (BuildContext context) =>
            const ListenableBuilderExample(title: 'ListenableBuilder'),
      ),
    ],
  ),
  CatalogItem.catalog(
    path: 'async',
    title: '异步编程',
    subtitle: 'Future、Stream、Compute、Completer、Isolate',
    children: <CatalogItem>[
      CatalogItem.page(
        path: 'future',
        title: 'Future',
        subtitle: '异步任务',
        pageBuilder: (BuildContext context) =>
            const FutureExample(title: 'Future'),
      ),
      CatalogItem.page(
        path: 'stream',
        title: 'Stream',
        subtitle: '异步流',
        pageBuilder: (BuildContext context) =>
            const StreamExample(title: 'Stream'),
      ),
      CatalogItem.page(
        path: 'compute',
        title: 'Compute',
        subtitle: '计算隔离',
        pageBuilder: (BuildContext context) =>
            const ComputeExample(title: 'Compute'),
      ),
      CatalogItem.page(
        path: 'completer',
        title: 'Completer',
        subtitle: '异步完成器',
        pageBuilder: (BuildContext context) =>
            const CompleterExample(title: 'Completer'),
      ),
      CatalogItem.page(
        path: 'isolate',
        title: 'Isolate',
        subtitle: '多线程',
        pageBuilder: (BuildContext context) =>
            const IsolateExample(title: 'Isolate'),
      ),
    ],
  ),
];
