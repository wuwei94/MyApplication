import 'package:flutter/widgets.dart';
import 'package:flutter_demo/catalog/models/catalog_entry.dart';
import 'package:flutter_demo/demos/layout/slivers/sliver_appbar_demo.dart';
import 'package:flutter_demo/demos/layout/slivers/sliver_grid_demo.dart';
import 'package:flutter_demo/demos/layout/slivers/sliver_list_demo.dart';

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
