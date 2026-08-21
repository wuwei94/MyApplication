import 'package:flutter_demo/catalog/models/catalog_entry.dart';
import 'package:flutter_demo/catalog/models/catalog_section.dart';
import 'package:flutter_demo/demos/layout/adaptive/catalog.dart';
import 'package:flutter_demo/demos/layout/async_widgets/catalog.dart';
import 'package:flutter_demo/demos/layout/asynchronous/catalog.dart';
import 'package:flutter_demo/demos/layout/containers/catalog.dart';
import 'package:flutter_demo/demos/layout/decoration/catalog.dart';
import 'package:flutter_demo/demos/layout/dialogs/catalog.dart';
import 'package:flutter_demo/demos/layout/flow/catalog.dart';
import 'package:flutter_demo/demos/layout/interaction/catalog.dart';
import 'package:flutter_demo/demos/layout/linear/catalog.dart';
import 'package:flutter_demo/demos/layout/scroll/catalog.dart';
import 'package:flutter_demo/demos/layout/slivers/catalog.dart';
import 'package:flutter_demo/demos/layout/stack/catalog.dart';
import 'package:flutter_demo/demos/layout/state_primitives/catalog.dart';
import 'package:flutter_demo/demos/layout/transitions/catalog.dart';

/// Layout 目录。
class LayoutCatalog extends CatalogSection {
  const LayoutCatalog._();

  @override
  String get path => 'layout';

  @override
  String get title => 'Layout';

  @override
  String get subtitle => '布局组件';

  @override
  List<CatalogEntry> get items => _items;

  static final List<CatalogEntry> _items = <CatalogEntry>[
    // 基础布局：先了解容器、线性排布、层叠和流式布局。
    containersCatalog, // 基础容器
    linearCatalog, // 线性布局
    stackCatalog, // 堆叠布局
    flowCatalog, // 流式布局
    // 滚动与 Sliver：聚焦常见滚动容器和 sliver 体系。
    scrollCatalog, // 滚动组件
    sliversCatalog, // Sliver组件
    // 视觉效果与弹层：覆盖装饰、过渡动画和弹窗模态场景。
    decorationCatalog, // 装饰组件
    transitionsCatalog, // 过渡动画组件
    dialogsCatalog, // 弹窗与模态组件
    // 交互：覆盖常见手势和返回拦截场景。
    interactionCatalog, // 手势交互
    // 感知与状态驱动：按布局约束、异步数据和可监听状态触发重建。
    layoutAwareCatalog, // 布局感知组件
    asyncDataDrivenCatalog, // 异步数据驱动组件
    stateSharingCatalog, // 状态监听与共享组件
    // 异步编程：集中展示 Future、Stream、Compute、Completer、Isolate。
    asynchronousCatalog, // 异步编程
  ];
}

const LayoutCatalog layoutCatalog = LayoutCatalog._();
