import 'package:basic_flutter/features/state_manager/bloc/counter_bloc_example.dart';
import 'package:basic_flutter/features/state_manager/provider/counter_provider_example.dart';
import 'package:basic_flutter/features/state_manager/riverpod/counter_riverpod_example.dart';
import 'package:basic_flutter/navigation/models/route_module.dart';
import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:flutter/widgets.dart';

/// State 状态管理路由
final RouteModule stateManagerModule = RouteModule(
  entry: RouteItem.section(
    path: '/state-manager',
    title: 'StateManager',
    subtitle: '状态管理',
    routeItems: stateManagerRoutes,
  ),
  routes: stateManagerRoutes,
);

final List<RouteItem> stateManagerRoutes = [
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
