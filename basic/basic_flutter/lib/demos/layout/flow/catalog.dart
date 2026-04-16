import 'package:basic_flutter/catalog/models/catalog_entry.dart';
import 'package:basic_flutter/demos/layout/flow/flow_example.dart';
import 'package:basic_flutter/demos/layout/flow/wrap_example.dart';
import 'package:flutter/widgets.dart';

final CatalogEntry flowCatalog = CatalogEntry.catalog(
  path: 'flow',
  title: '流式布局',
  subtitle: 'Wrap、Flow',
  children: <CatalogEntry>[
    CatalogEntry.page(
      path: 'wrap',
      title: 'Wrap',
      subtitle: '自动换行',
      pageBuilder: (BuildContext context) => const WrapDemoPage(title: 'Wrap'),
    ),
    CatalogEntry.page(
      path: 'flow-widget',
      title: 'Flow',
      subtitle: '流式布局',
      pageBuilder: (BuildContext context) => const FlowDemoPage(title: 'Flow'),
    ),
  ],
);
