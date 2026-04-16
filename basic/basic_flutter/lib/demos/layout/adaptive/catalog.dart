import 'package:basic_flutter/catalog/models/catalog_entry.dart';
import 'package:basic_flutter/demos/layout/adaptive/layout_builder_example.dart';
import 'package:flutter/widgets.dart';

final CatalogEntry adaptiveCatalog = CatalogEntry.catalog(
  path: 'adaptive',
  title: '布局构建器',
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
