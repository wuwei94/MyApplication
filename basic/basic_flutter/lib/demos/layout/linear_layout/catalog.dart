import 'package:basic_flutter/app/catalog/catalog_item.dart';
import 'package:basic_flutter/demos/layout/linear_layout/column_example.dart';
import 'package:basic_flutter/demos/layout/linear_layout/flexible_expanded_example.dart';
import 'package:basic_flutter/demos/layout/linear_layout/row_example.dart';
import 'package:flutter/widgets.dart';

final CatalogItem linearLayoutCatalog = CatalogItem.catalog(
  path: 'linear',
  title: '线性布局',
  subtitle: 'Row、Column、Flexible、Expanded',
  children: <CatalogItem>[
    CatalogItem.page(
      path: 'row',
      title: 'Row',
      subtitle: '水平布局',
      pageBuilder: (BuildContext context) => const RowExample(title: 'Row'),
    ),
    CatalogItem.page(
      path: 'column',
      title: 'Column',
      subtitle: '垂直布局',
      pageBuilder: (BuildContext context) => const ColumnExample(title: 'Column'),
    ),
    CatalogItem.page(
      path: 'flexible-expanded',
      title: 'Flexible & Expanded',
      subtitle: '弹性布局',
      pageBuilder: (BuildContext context) =>
          const FlexibleExpandedExample(title: 'Flexible & Expanded'),
    ),
  ],
);
