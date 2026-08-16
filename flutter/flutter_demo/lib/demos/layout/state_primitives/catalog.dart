import 'package:flutter/widgets.dart';
import 'package:flutter_demo/catalog/models/catalog_entry.dart';
import 'package:flutter_demo/demos/layout/state_primitives/inherited_widget_demo.dart';
import 'package:flutter_demo/demos/layout/state_primitives/listenable_builder_demo.dart';
import 'package:flutter_demo/demos/layout/state_primitives/value_listenable_builder_demo.dart';

final CatalogEntry stateSharingCatalog = CatalogEntry.catalog(
  path: 'state-primitives',
  title: '状态监听与共享组件',
  subtitle: 'InheritedWidget、ValueListenableBuilder、ListenableBuilder',
  children: <CatalogEntry>[
    CatalogEntry.page(
      path: 'inherited-widget',
      title: 'InheritedWidget',
      subtitle: '数据共享',
      pageBuilder: (BuildContext context) =>
          const InheritedWidgetDemoPage(title: 'InheritedWidget'),
    ),
    CatalogEntry.page(
      path: 'value-listenable-builder',
      title: 'ValueListenableBuilder',
      subtitle: '值监听构建器',
      pageBuilder: (BuildContext context) => const ValueListenableBuilderDemoPage(
        title: 'ValueListenableBuilder',
      ),
    ),
    CatalogEntry.page(
      path: 'listenable-builder',
      title: 'ListenableBuilder',
      subtitle: '可监听构建器',
      pageBuilder: (BuildContext context) =>
          const ListenableBuilderDemoPage(title: 'ListenableBuilder'),
    ),
  ],
);
