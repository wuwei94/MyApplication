import 'package:basic_flutter/catalog/models/catalog_entry.dart';
import 'package:basic_flutter/catalog/models/catalog_section.dart';
import 'package:basic_flutter/demos/layout/transitions/catalog.dart';
import 'package:basic_flutter/demos/layout/async/catalog.dart';
import 'package:basic_flutter/demos/layout/decoration/catalog.dart';
import 'package:basic_flutter/demos/layout/dialogs/catalog.dart';
import 'package:basic_flutter/demos/layout/flow/catalog.dart';
import 'package:basic_flutter/demos/layout/interaction/catalog.dart';
import 'package:basic_flutter/demos/layout/adaptive/catalog.dart';
import 'package:basic_flutter/demos/layout/containers/catalog.dart';
import 'package:basic_flutter/demos/layout/linear/catalog.dart';
import 'package:basic_flutter/demos/layout/scroll/catalog.dart';
import 'package:basic_flutter/demos/layout/slivers/catalog.dart';
import 'package:basic_flutter/demos/layout/stack/catalog.dart';
import 'package:basic_flutter/demos/layout/async_widgets/catalog.dart';
import 'package:basic_flutter/demos/layout/state_primitives/catalog.dart';

/// Layout 目录。
class LayoutCatalog extends CatalogSection {
  const LayoutCatalog._();

  @override
  String get path => 'layout';

  @override
  String get title => 'Layout Example';

  @override
  String get subtitle => '布局组件';

  @override
  List<CatalogEntry> get items => _items;

  static final List<CatalogEntry> _items = <CatalogEntry>[
    containersCatalog,
    linearCatalog,
    stackCatalog,
    flowCatalog,
    scrollCatalog,
    sliversCatalog,
    interactionCatalog,
    transitionsCatalog,
    dialogsCatalog,
    decorationCatalog,
    adaptiveCatalog,
    asyncWidgetsCatalog,
    statePrimitivesCatalog,
    asyncCatalog,
  ];
}

const LayoutCatalog layoutCatalog = LayoutCatalog._();
