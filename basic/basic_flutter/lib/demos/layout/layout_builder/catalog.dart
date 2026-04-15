import 'package:basic_flutter/app/catalog/catalog_item.dart';
import 'package:basic_flutter/demos/layout/layout_builder/layout_builder_example.dart';
import 'package:flutter/widgets.dart';

final CatalogItem layoutBuilderCatalog = CatalogItem.catalog(
  path: '/layout/builders',
  title: '布局构建器',
  subtitle: 'LayoutBuilder',
  children: <CatalogItem>[
    CatalogItem.page(
      path: '/layout/builders/layout-builder',
      title: 'LayoutBuilder',
      subtitle: '响应式布局',
      pageBuilder: (BuildContext context) =>
          const LayoutBuilderExample(title: 'LayoutBuilder'),
    ),
  ],
);
