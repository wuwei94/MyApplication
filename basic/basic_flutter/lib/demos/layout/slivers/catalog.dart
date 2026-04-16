import 'package:basic_flutter/catalog/models/catalog_entry.dart';
import 'package:basic_flutter/demos/layout/slivers/sliver_appbar_example.dart';
import 'package:basic_flutter/demos/layout/slivers/sliver_grid_example.dart';
import 'package:basic_flutter/demos/layout/slivers/sliver_list_example.dart';
import 'package:flutter/widgets.dart';

final CatalogEntry sliversCatalog = CatalogEntry.catalog(
  path: 'slivers',
  title: 'Sliver组件',
  subtitle: 'SliverList、SliverGrid、SliverAppBar',
  children: <CatalogEntry>[
    CatalogEntry.page(
      path: 'sliver-list',
      title: 'SliverList',
      subtitle: 'Sliver列表',
      pageBuilder: (BuildContext context) =>
          const SliverListDemoPage(title: 'SliverList'),
    ),
    CatalogEntry.page(
      path: 'sliver-grid',
      title: 'SliverGrid',
      subtitle: 'Sliver网格',
      pageBuilder: (BuildContext context) =>
          const SliverGridDemoPage(title: 'SliverGrid'),
    ),
    CatalogEntry.page(
      path: 'sliver-app-bar',
      title: 'SliverAppBar',
      subtitle: 'Sliver应用栏',
      pageBuilder: (BuildContext context) =>
          const SliverAppBarDemoPage(title: 'SliverAppBar'),
    ),
  ],
);
