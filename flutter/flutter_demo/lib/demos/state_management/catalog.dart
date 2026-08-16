import 'package:flutter/widgets.dart';
import 'package:flutter_demo/catalog/models/catalog_entry.dart';
import 'package:flutter_demo/catalog/models/catalog_section.dart';
import 'package:flutter_demo/demos/state_management/bloc/bloc_demo.dart';
import 'package:flutter_demo/demos/state_management/cubit/cubit_demo.dart';
import 'package:flutter_demo/demos/state_management/provider/provider_demo.dart';
import 'package:flutter_demo/demos/state_management/riverpod/riverpod_demo.dart';

/// State Management 模块
///
/// 包含：Cubit、Bloc、Provider、Riverpod 等状态管理示例
class StateManagementCatalog extends CatalogSection {
  const StateManagementCatalog._();

  @override
  String get path => 'state-management';

  @override
  String get title => 'State Management';

  @override
  String get subtitle => '常见响应式状态管理方案';

  @override
  List<CatalogEntry> get items => _items;

  static final List<CatalogEntry> _items = [
    CatalogEntry.page(
      path: 'cubit',
      title: 'Cubit',
      subtitle: '轻量事件驱动计数器与状态流',
      pageBuilder: (BuildContext context) =>
          const CubitCounterDemoPage(title: 'Cubit'),
    ),
    CatalogEntry.page(
      path: 'bloc',
      title: 'Bloc',
      subtitle: '事件到状态流转的计数器示例',
      pageBuilder: (BuildContext context) =>
          const BlocCounterDemoPage(title: 'Bloc'),
    ),
    CatalogEntry.page(
      path: 'provider',
      title: 'Provider',
      subtitle: 'ChangeNotifier 计数器与局部刷新',
      pageBuilder: (BuildContext context) =>
          const ProviderCounterDemoPage(title: 'Provider'),
    ),
    CatalogEntry.page(
      path: 'riverpod',
      title: 'Riverpod',
      subtitle: 'ProviderScope 下的计数器状态读取',
      pageBuilder: (BuildContext context) =>
          const RiverpodCounterDemoPage(title: 'Riverpod'),
    ),
  ];
}

/// 单例实例
const StateManagementCatalog stateManagementCatalog =
    StateManagementCatalog._();
