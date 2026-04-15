import 'package:basic_flutter/app/catalog/catalog_item.dart';
import 'package:basic_flutter/demos/layout/flow_layout/flow_example.dart';
import 'package:basic_flutter/demos/layout/flow_layout/wrap_example.dart';
import 'package:flutter/widgets.dart';

final CatalogItem flowLayoutCatalog = CatalogItem.catalog(
  path: '/layout/flow',
  title: '流式布局',
  subtitle: 'Wrap、Flow',
  children: <CatalogItem>[
    CatalogItem.page(
      path: '/layout/flow/wrap',
      title: 'Wrap',
      subtitle: '自动换行',
      pageBuilder: (BuildContext context) => const WrapExample(title: 'Wrap'),
    ),
    CatalogItem.page(
      path: '/layout/flow/flow-widget',
      title: 'Flow',
      subtitle: '流式布局',
      pageBuilder: (BuildContext context) => const FlowExample(title: 'Flow'),
    ),
  ],
);
