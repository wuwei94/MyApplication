import 'package:flutter/widgets.dart';
import 'package:flutter_demo/catalog/models/catalog_entry.dart';
import 'package:flutter_demo/demos/layout/flow/flow_demo.dart';
import 'package:flutter_demo/demos/layout/flow/wrap_demo.dart';

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
