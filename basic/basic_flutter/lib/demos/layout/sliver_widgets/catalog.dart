import 'package:basic_flutter/app/catalog/catalog_item.dart';
import 'package:basic_flutter/demos/layout/sliver_widgets/sliver_appbar_example.dart';
import 'package:basic_flutter/demos/layout/sliver_widgets/sliver_grid_example.dart';
import 'package:basic_flutter/demos/layout/sliver_widgets/sliver_list_example.dart';
import 'package:flutter/widgets.dart';

final CatalogItem sliverWidgetsCatalog = CatalogItem.catalog(
  path: 'slivers',
  title: 'Sliver 组件',
  subtitle: 'SliverList、SliverGrid、SliverAppBar',
  children: <CatalogItem>[
    CatalogItem.page(
      path: 'sliver-list',
      title: 'SliverList',
      subtitle: 'Sliver列表',
      pageBuilder: (BuildContext context) =>
          const SliverListExample(title: 'SliverList'),
    ),
    CatalogItem.page(
      path: 'sliver-grid',
      title: 'SliverGrid',
      subtitle: 'Sliver网格',
      pageBuilder: (BuildContext context) =>
          const SliverGridExample(title: 'SliverGrid'),
    ),
    CatalogItem.page(
      path: 'sliver-app-bar',
      title: 'SliverAppBar',
      subtitle: 'Sliver应用栏',
      pageBuilder: (BuildContext context) =>
          const SliverAppBarExample(title: 'SliverAppBar'),
    ),
  ],
);
