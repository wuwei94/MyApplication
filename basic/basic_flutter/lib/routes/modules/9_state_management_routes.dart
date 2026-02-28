import 'package:basic_flutter/features/9_state_management/bloc/my_bloc.dart';
import 'package:basic_flutter/features/9_state_management/get/my_get_app.dart';
import 'package:basic_flutter/features/9_state_management/getX/my_get_app.dart';
import 'package:basic_flutter/features/9_state_management/provider/my_provider.dart';
import 'package:basic_flutter/routes/models/route_item_model.dart';
import 'package:flutter/widgets.dart';

/// State Management 状态管理路由
final List<RouteItem> stateManagementRoutes = [
  RouteItem(
    name: 'Provider',
    path: '/provider',
    describe: 'Provider',
    builder: (BuildContext context, _) => const MyProvider(),
  ),
  RouteItem(
    name: 'GetX',
    path: '/getx',
    describe: 'GetX',
    builder: (BuildContext context, _) => const MyGet(),
  ),
  RouteItem(
    name: 'GetX2',
    path: '/getx2',
    describe: 'GetX',
    builder: (BuildContext context, _) => const MyGetX2(),
  ),
  RouteItem(
    name: 'BloC',
    path: '/bloc',
    describe: 'BloC',
    builder: (BuildContext context, _) => const MyBloC(),
  ),
];
