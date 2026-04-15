import 'package:basic_flutter/app/catalog/catalog_item.dart';
import 'package:basic_flutter/app/catalog/catalog_section.dart';
import 'package:basic_flutter/demos/layout/catalogs/layout_basics_catalog.dart';
import 'package:basic_flutter/demos/layout/catalogs/layout_interaction_catalog.dart';
import 'package:basic_flutter/demos/layout/catalogs/layout_scrolling_catalog.dart';
import 'package:basic_flutter/demos/layout/catalogs/layout_state_catalog.dart';

/// Layout 目录。
class LayoutCatalog extends CatalogSection {
  const LayoutCatalog._();

  @override
  String get path => '/layout';

  @override
  String get title => 'Layout';

  @override
  String get subtitle => '布局组件';

  @override
  List<CatalogItem> get items => _items;

  static final List<CatalogItem> _items = <CatalogItem>[
    ...layoutBasicsCatalogItems,
    ...layoutScrollingCatalogItems,
    ...layoutInteractionCatalogItems,
    ...layoutStateCatalogItems,
  ];
}

const LayoutCatalog layoutCatalog = LayoutCatalog._();
