import 'package:basic_flutter/app/catalog/catalog_item.dart';
import 'package:basic_flutter/demos/layout/state_driven/futurebuilder_example.dart';
import 'package:basic_flutter/demos/layout/state_driven/streambuilder_example.dart';
import 'package:flutter/widgets.dart';

final CatalogItem stateDrivenCatalog = CatalogItem.catalog(
  path: '/layout/state-driven',
  title: '状态驱动组件',
  subtitle: 'FutureBuilder、StreamBuilder',
  children: <CatalogItem>[
    CatalogItem.page(
      path: '/layout/state-driven/future-builder',
      title: 'FutureBuilder',
      subtitle: 'Future构建器',
      pageBuilder: (BuildContext context) =>
          const FutureBuilderExample(title: 'FutureBuilder'),
    ),
    CatalogItem.page(
      path: '/layout/state-driven/stream-builder',
      title: 'StreamBuilder',
      subtitle: 'Stream构建器',
      pageBuilder: (BuildContext context) =>
          const StreamBuilderExample(title: 'StreamBuilder'),
    ),
  ],
);
