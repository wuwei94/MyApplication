import 'package:basic_flutter/catalog/models/catalog_entry.dart';
import 'package:basic_flutter/demos/layout/stack/positioned_example.dart';
import 'package:basic_flutter/demos/layout/stack/stack_example.dart';
import 'package:flutter/widgets.dart';

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
