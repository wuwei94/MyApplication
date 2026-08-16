import 'package:flutter/widgets.dart';
import 'package:flutter_demo/catalog/models/catalog_entry.dart';
import 'package:flutter_demo/demos/layout/interaction/gesturedetector_demo.dart';
import 'package:flutter_demo/demos/layout/interaction/pop_scope_demo.dart';

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
