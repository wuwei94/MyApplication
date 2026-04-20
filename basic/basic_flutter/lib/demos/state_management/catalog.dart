import 'package:basic_flutter/demos/state_management/bloc/bloc_example.dart';
import 'package:basic_flutter/demos/state_management/cubit/cubit_example.dart';
import 'package:basic_flutter/demos/state_management/provider/provider_example.dart';
import 'package:basic_flutter/demos/state_management/riverpod/riverpod_example.dart';
import 'package:basic_flutter/catalog/models/catalog_entry.dart';
import 'package:basic_flutter/catalog/models/catalog_section.dart';
import 'package:flutter/widgets.dart';

/// State Management 模块
///
/// 包含：Cubit、Bloc、Provider、Riverpod 等状态管理示例
class StateManagementCatalog extends CatalogSection {
  const StateManagementCatalog._();

  @override
  String get path => 'state-management';

  @override
  String get title => 'State Management Example';

  @override
  String get subtitle => '状态管理';

  @override
  List<CatalogEntry> get items => _items;

  static final List<CatalogEntry> _items = [
    CatalogEntry.page(
      path: 'cubit',
      title: 'Cubit',
      subtitle: 'Cubit示例',
      pageBuilder: (BuildContext context) =>
          const CubitCounterDemoPage(title: 'Cubit'),
    ),
    CatalogEntry.page(
      path: 'bloc',
      title: 'Bloc',
      subtitle: 'Bloc示例',
      pageBuilder: (BuildContext context) =>
          const BlocCounterDemoPage(title: 'Bloc'),
    ),
    CatalogEntry.page(
      path: 'provider',
      title: 'Provider',
      subtitle: 'Provider示例',
      pageBuilder: (BuildContext context) =>
          const ProviderCounterDemoPage(title: 'Provider'),
    ),
    CatalogEntry.page(
      path: 'riverpod',
      title: 'Riverpod',
      subtitle: 'Riverpod示例',
      pageBuilder: (BuildContext context) =>
          const RiverpodCounterDemoPage(title: 'Riverpod'),
    ),
  ];
}

/// 单例实例
const StateManagementCatalog stateManagementCatalog =
    StateManagementCatalog._();
