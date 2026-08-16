import 'package:flutter/widgets.dart';
import 'package:flutter_demo/catalog/models/catalog_entry.dart';
import 'package:flutter_demo/demos/layout/linear/column_demo.dart';
import 'package:flutter_demo/demos/layout/linear/flexible_expanded_demo.dart';
import 'package:flutter_demo/demos/layout/linear/row_demo.dart';

final CatalogEntry linearCatalog = CatalogEntry.catalog(
  path: 'linear',
  title: '线性布局',
  subtitle: 'Row、Column、Flexible、Expanded',
  children: <CatalogEntry>[
    CatalogEntry.page(
      path: 'row',
      title: 'Row',
      subtitle: '水平布局',
      pageBuilder: (BuildContext context) => const RowDemoPage(title: 'Row'),
    ),
    CatalogEntry.page(
      path: 'column',
      title: 'Column',
      subtitle: '垂直布局',
      pageBuilder: (BuildContext context) => const ColumnDemoPage(title: 'Column'),
    ),
    CatalogEntry.page(
      path: 'flexible-expanded',
      title: 'Flexible & Expanded',
      subtitle: '弹性布局',
      pageBuilder: (BuildContext context) =>
          const FlexibleExpandedDemoPage(title: 'Flexible & Expanded'),
    ),
  ],
);
