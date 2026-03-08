import 'package:basic_flutter/features/state_management/bloc/my_bloc.dart';
import 'package:basic_flutter/features/state_management/getX/my_get_app.dart';
import 'package:basic_flutter/features/state_management/provider/my_provider.dart';
import 'package:basic_flutter/routes/models/route_item_model.dart';
import 'package:flutter/widgets.dart';

/// State Management 状态管理路由
final List<RouteItem> stateManagementRoutes = [
  RouteItem(
    name: 'Provider',
    path: '/provider',
    describe: 'Provider',
    routeBuilder: (BuildContext context, _) => const MyProvider(),
  ),
  RouteItem(
    name: 'GetX',
    path: '/getx',
    describe: 'GetX',
    routeBuilder: (BuildContext context, _) => const MyGet(),
  ),
  RouteItem(
    name: 'BloC',
    path: '/bloc',
    describe: 'BloC',
    routeBuilder: (BuildContext context, _) => const MyBloC(),
  ),
];
