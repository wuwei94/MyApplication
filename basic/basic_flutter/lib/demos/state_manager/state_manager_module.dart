import 'package:basic_flutter/demos/state_manager/bloc/counter_bloc_example.dart';
import 'package:basic_flutter/demos/state_manager/provider/counter_provider_example.dart';
import 'package:basic_flutter/demos/state_manager/riverpod/counter_riverpod_example.dart';
import 'package:basic_flutter/app/catalog/catalog_item.dart';
import 'package:flutter/widgets.dart';

/// StateManager 模块
/// 
/// 包含：GetX、BloC、Provider、Riverpod 等状态管理示例
class StateManagerModule {
  const StateManagerModule._();

  /// 首页目录入口
  CatalogItem get catalog => CatalogItem.catalog(
        path: '/state-manager',
        title: 'StateManager',
        subtitle: '状态管理',
        children: routes,
      );

  /// 所有路由列表
  List<CatalogItem> get routes => _routes;

  static final List<CatalogItem> _routes = [
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
const StateManagerModule stateManagerModule = StateManagerModule._();
