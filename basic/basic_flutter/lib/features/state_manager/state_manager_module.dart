import 'package:basic_flutter/features/state_manager/bloc/counter_bloc_example.dart';
import 'package:basic_flutter/features/state_manager/provider/counter_provider_example.dart';
import 'package:basic_flutter/features/state_manager/riverpod/counter_riverpod_example.dart';
import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:flutter/widgets.dart';

/// StateManager 模块
/// 
/// 包含：GetX、BloC、Provider、Riverpod 等状态管理示例
class StateManagerModule {
  const StateManagerModule._();

  /// 首页目录入口
  RouteItem get catalog => RouteItem.section(
        path: '/state-manager',
        title: 'StateManager',
        subtitle: '状态管理',
        routeItems: routes,
      );

  /// 所有路由列表
  List<RouteItem> get routes => _routes;

  static final List<RouteItem> _routes = [
    RouteItem.page(
      path: '/state-manager/bloc',
      title: 'BloC',
      subtitle: 'BloC示例',
      pageBuilder: (BuildContext context) =>
          const CounterBlocExample(title: 'BloC'),
    ),
    RouteItem.page(
      path: '/state-manager/provider',
      title: 'Provider',
      subtitle: 'Provider示例',
      pageBuilder: (BuildContext context) =>
          const CounterProviderExample(title: 'Provider'),
    ),
    RouteItem.page(
      path: '/state-manager/riverpod',
      title: 'Riverpod',
      subtitle: 'Riverpod示例',
      pageBuilder: (BuildContext context) =>
          const CounterRiverpodExample(title: 'Riverpod'),
    ),
  ];
}

/// 单例实例
const StateManagerModule stateManagerModule = StateManagerModule._();
