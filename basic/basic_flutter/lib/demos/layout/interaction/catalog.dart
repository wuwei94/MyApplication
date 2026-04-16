import 'package:basic_flutter/catalog/models/catalog_entry.dart';
import 'package:basic_flutter/demos/layout/interaction/gesturedetector_example.dart';
import 'package:basic_flutter/demos/layout/interaction/pop_scope_example.dart';
import 'package:flutter/widgets.dart';

final CatalogEntry interactionCatalog = CatalogEntry.catalog(
  path: 'interaction',
  title: '手势交互',
  subtitle: 'GestureDetector、PopScope',
  children: <CatalogEntry>[
    CatalogEntry.page(
      path: 'gesture-detector',
      title: 'GestureDetector',
      subtitle: '手势检测',
      pageBuilder: (BuildContext context) =>
          const GestureDetectorDemoPage(title: 'GestureDetector'),
    ),
    CatalogEntry.page(
      path: 'pop-scope',
      title: 'PopScope',
      subtitle: '返回拦截',
      pageBuilder: (BuildContext context) =>
          const PopScopeDemoPage(title: 'PopScope'),
    ),
  ],
);
