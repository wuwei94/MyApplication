import 'package:basic_flutter/features/state_management/bloc/my_bloc.dart';
import 'package:basic_flutter/features/state_management/getX/my_get_app.dart';
import 'package:basic_flutter/features/state_management/provider/my_provider.dart';
import 'package:basic_flutter/routes/models/route_item_model.dart';
import 'package:flutter/widgets.dart';

/// State Management 状态管理路由
final List<RouteItem> stateManagementRoutes = [
  RouteItem(
    path: '/state/provider',
    name: 'Provider',
    describe: 'Provider',
    routeBuilder: (BuildContext context, _) => const MyProvider(),
  ),
  RouteItem(
    path: '/state/bloc',
    name: 'BloC',
    describe: 'BloC',
    routeBuilder: (BuildContext context, _) => const MyBloC(),
  ),
  RouteItem(
    path: '/state/getx',
    name: 'GetX',
    describe: 'GetX',
    routeBuilder: (BuildContext context, _) => const MyGet(),
  ),
];
