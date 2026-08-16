import 'package:flutter/widgets.dart';
import 'package:flutter_demo/catalog/models/catalog_entry.dart';
import 'package:flutter_demo/demos/layout/stack/positioned_demo.dart';
import 'package:flutter_demo/demos/layout/stack/stack_demo.dart';

final CatalogEntry stackCatalog = CatalogEntry.catalog(
  path: 'stack',
  title: '堆叠布局',
  subtitle: 'Stack、Positioned',
  children: <CatalogEntry>[
    CatalogEntry.page(
      path: 'stack',
      title: 'Stack',
      subtitle: '堆叠布局',
      pageBuilder: (BuildContext context) => const StackDemoPage(title: 'Stack'),
    ),
    CatalogEntry.page(
      path: 'positioned',
      title: 'Positioned',
      subtitle: '定位组件',
      pageBuilder: (BuildContext context) =>
          const PositionedDemoPage(title: 'Positioned'),
    ),
  ],
);
