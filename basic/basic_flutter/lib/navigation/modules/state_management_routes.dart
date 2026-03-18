import 'package:basic_flutter/features/state_management/bloc/counter_bloc_example.dart';
import 'package:basic_flutter/features/state_management/getx/counter_getx_example.dart';
import 'package:basic_flutter/features/state_management/provider/counter_provider_example.dart';
import 'package:basic_flutter/features/state_management/riverpod/counter_riverpod_example.dart';
import 'package:basic_flutter/navigation/models/route_item_model.dart';
import 'package:flutter/widgets.dart';

/// State Management 状态管理路由
final List<RouteItem> stateManagementRoutes = [
  RouteItem(
    path: '/state/provider',
    title: 'Provider',
    subtitle: 'Provider',
    routeBuilder: (BuildContext context, _) => const CounterProviderExample(),
  ),
  RouteItem(
    path: '/state/bloc',
    title: 'BloC',
    subtitle: 'BloC',
    routeBuilder: (BuildContext context, _) => const CounterBlocExample(),
  ),
  RouteItem(
    path: '/state/getx',
    title: 'GetX',
    subtitle: 'GetX',
    routeBuilder: (BuildContext context, _) => const CounterGetxExample(),
  ),
  RouteItem(
    path: '/state/riverpod',
    title: 'Riverpod',
    subtitle: 'Riverpod',
    routeBuilder: (BuildContext context, _) => const CounterRiverpodExample(),
  ),
];
