import 'package:basic_flutter/app/catalog/catalog_item.dart';
import 'package:basic_flutter/app/catalog/catalog_section.dart';
import 'package:basic_flutter/demos/layout/animations/catalog.dart';
import 'package:basic_flutter/demos/layout/async_programming/catalog.dart';
import 'package:basic_flutter/demos/layout/decoration_effects/catalog.dart';
import 'package:basic_flutter/demos/layout/dialogs_sheets/catalog.dart';
import 'package:basic_flutter/demos/layout/flow_layout/catalog.dart';
import 'package:basic_flutter/demos/layout/gesture_interaction/catalog.dart';
import 'package:basic_flutter/demos/layout/layout_builder/catalog.dart';
import 'package:basic_flutter/demos/layout/layout_containers/catalog.dart';
import 'package:basic_flutter/demos/layout/linear_layout/catalog.dart';
import 'package:basic_flutter/demos/layout/scroll_widgets/catalog.dart';
import 'package:basic_flutter/demos/layout/sliver_widgets/catalog.dart';
import 'package:basic_flutter/demos/layout/stack_positioning/catalog.dart';
import 'package:basic_flutter/demos/layout/state_driven/catalog.dart';
import 'package:basic_flutter/demos/layout/state_management/catalog.dart';

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
    layoutContainersCatalog,
    linearLayoutCatalog,
    stackPositioningCatalog,
    flowLayoutCatalog,
    scrollWidgetsCatalog,
    sliverWidgetsCatalog,
    gestureInteractionCatalog,
    animationsCatalog,
    dialogsSheetsCatalog,
    decorationEffectsCatalog,
    layoutBuilderCatalog,
    stateDrivenCatalog,
    stateManagementCatalog,
    asyncProgrammingCatalog,
  ];
}

const LayoutCatalog layoutCatalog = LayoutCatalog._();
