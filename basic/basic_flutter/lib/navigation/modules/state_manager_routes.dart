import 'package:basic_flutter/features/state_manager/bloc/counter_bloc_example.dart';
import 'package:basic_flutter/features/state_manager/getx/counter_getx_example.dart';
import 'package:basic_flutter/features/state_manager/provider/counter_provider_example.dart';
import 'package:basic_flutter/features/state_manager/riverpod/counter_riverpod_example.dart';
import 'package:basic_flutter/navigation/models/route_item_model.dart';
import 'package:flutter/widgets.dart';

/// State 状态管理路由
final List<RouteItem> stateManagerRoutes = [
  RouteItem(
    path: '/state_manager/getx',
    title: 'GetX',
    subtitle: 'GetX示例',
    routeBuilder: (BuildContext context, _) =>
        const CounterGetxExample(title: 'GetX'),
  ),
  RouteItem(
    path: '/state_manager/bloc',
    title: 'BloC',
    subtitle: 'BloC示例',
    routeBuilder: (BuildContext context, _) =>
        const CounterBlocExample(title: 'BloC'),
  ),
  RouteItem(
    path: '/state_manager/provider',
    title: 'Provider',
    subtitle: 'Provider示例',
    routeBuilder: (BuildContext context, _) =>
        const CounterProviderExample(title: 'Provider'),
  ),
  RouteItem(
    path: '/state/riverpod',
    title: 'Riverpod',
    subtitle: 'Riverpod示例',
    routeBuilder: (BuildContext context, _) =>
        const CounterRiverpodExample(title: 'Riverpod'),
  ),
];
