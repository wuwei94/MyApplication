import 'package:basic_flutter/app/catalog/catalog_item.dart';
import 'package:basic_flutter/demos/layout/stack_positioning/positioned_example.dart';
import 'package:basic_flutter/demos/layout/stack_positioning/stack_example.dart';
import 'package:flutter/widgets.dart';

final CatalogItem stackPositioningCatalog = CatalogItem.catalog(
  path: '/layout/stacking',
  title: '堆叠定位',
  subtitle: 'Stack、Positioned',
  children: <CatalogItem>[
    CatalogItem.page(
      path: '/layout/stacking/stack',
      title: 'Stack',
      subtitle: '堆叠布局',
      pageBuilder: (BuildContext context) => const StackExample(title: 'Stack'),
    ),
    CatalogItem.page(
      path: '/layout/stacking/positioned',
      title: 'Positioned',
      subtitle: '定位组件',
      pageBuilder: (BuildContext context) =>
          const PositionedExample(title: 'Positioned'),
    ),
  ],
);
