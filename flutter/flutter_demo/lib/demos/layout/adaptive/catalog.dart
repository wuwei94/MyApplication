import 'package:flutter/widgets.dart';
import 'package:flutter_demo/catalog/models/catalog_entry.dart';
import 'package:flutter_demo/demos/layout/adaptive/layout_builder_demo.dart';

final CatalogEntry layoutAwareCatalog = CatalogEntry.catalog(
  path: 'adaptive',
  title: '布局感知组件',
  subtitle: 'LayoutBuilder',
  children: <CatalogEntry>[
    CatalogEntry.page(
      path: 'layout-builder',
      title: 'LayoutBuilder',
      subtitle: '响应式布局',
      pageBuilder: (BuildContext context) =>
          const LayoutBuilderDemoPage(title: 'LayoutBuilder'),
    ),
  ],
);
