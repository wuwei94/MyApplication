import 'package:basic_flutter/catalog/models/catalog_entry.dart';
import 'package:basic_flutter/demos/layout/async_widgets/futurebuilder_example.dart';
import 'package:basic_flutter/demos/layout/async_widgets/streambuilder_example.dart';
import 'package:flutter/widgets.dart';

final CatalogEntry asyncDataDrivenCatalog = CatalogEntry.catalog(
  path: 'async-widgets',
  title: '异步数据驱动组件',
  subtitle: 'FutureBuilder、StreamBuilder',
  children: <CatalogEntry>[
    CatalogEntry.page(
      path: 'future-builder',
      title: 'FutureBuilder',
      subtitle: 'Future构建器',
      pageBuilder: (BuildContext context) =>
          const FutureBuilderDemoPage(title: 'FutureBuilder'),
    ),
    CatalogEntry.page(
      path: 'stream-builder',
      title: 'StreamBuilder',
      subtitle: 'Stream构建器',
      pageBuilder: (BuildContext context) =>
          const StreamBuilderDemoPage(title: 'StreamBuilder'),
    ),
  ],
);
