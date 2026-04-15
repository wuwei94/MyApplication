import 'package:basic_flutter/demos/state_manager/bloc/counter_bloc_example.dart';
import 'package:basic_flutter/demos/state_manager/provider/counter_provider_example.dart';
import 'package:basic_flutter/demos/state_manager/riverpod/counter_riverpod_example.dart';
import 'package:basic_flutter/app/catalog/catalog_item.dart';
import 'package:basic_flutter/app/catalog/catalog_section.dart';
import 'package:flutter/widgets.dart';

/// StateManager 模块
/// 
/// 包含：GetX、BloC、Provider、Riverpod 等状态管理示例
class StateManagerCatalog extends CatalogSection {
  const StateManagerCatalog._();

  @override
  String get path => 'state-manager';

  @override
  String get title => 'StateManager';

  @override
  String get subtitle => '状态管理';

  @override
  List<CatalogItem> get items => _items;

  static final List<CatalogItem> _items = [
    CatalogItem.page(
      path: 'bloc',
      title: 'BloC',
      subtitle: 'BloC示例',
      pageBuilder: (BuildContext context) =>
          const CounterBlocExample(title: 'BloC'),
    ),
    CatalogItem.page(
      path: 'provider',
      title: 'Provider',
      subtitle: 'Provider示例',
      pageBuilder: (BuildContext context) =>
          const CounterProviderExample(title: 'Provider'),
    ),
    CatalogItem.page(
      path: 'riverpod',
      title: 'Riverpod',
      subtitle: 'Riverpod示例',
      pageBuilder: (BuildContext context) =>
          const CounterRiverpodExample(title: 'Riverpod'),
    ),
  ];
}

/// 单例实例
const StateManagerCatalog stateManagerCatalog = StateManagerCatalog._();
