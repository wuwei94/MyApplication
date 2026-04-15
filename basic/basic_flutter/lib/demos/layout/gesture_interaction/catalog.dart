import 'package:basic_flutter/app/catalog/catalog_item.dart';
import 'package:basic_flutter/demos/layout/gesture_interaction/gesturedetector_example.dart';
import 'package:basic_flutter/demos/layout/gesture_interaction/pop_scope_example.dart';
import 'package:flutter/widgets.dart';

final CatalogItem gestureInteractionCatalog = CatalogItem.catalog(
  path: 'gestures',
  title: '手势交互',
  subtitle: 'GestureDetector、PopScope',
  children: <CatalogItem>[
    CatalogItem.page(
      path: 'gesture-detector',
      title: 'GestureDetector',
      subtitle: '手势检测',
      pageBuilder: (BuildContext context) =>
          const GestureDetectorExample(title: 'GestureDetector'),
    ),
    CatalogItem.page(
      path: 'pop-scope',
      title: 'PopScope',
      subtitle: '返回拦截',
      pageBuilder: (BuildContext context) =>
          const PopScopeExample(title: 'PopScope'),
    ),
  ],
);
