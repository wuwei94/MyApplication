import 'package:basic_flutter/routes/constants/route_groups.dart';
import 'package:basic_flutter/features/state_management/bloc/my_bloc.dart';
import 'package:basic_flutter/features/state_management/get/my_get_app.dart';
import 'package:basic_flutter/features/state_management/getX/my_get_app.dart';
import 'package:basic_flutter/features/state_management/provider/my_provider.dart';
import 'package:go_router/go_router.dart';

/// State Management 状态管理路由
final List<GoRoute> stateManagementRoutes = [
  GoRoute(
    path: StateManagementRoutes.provider,
    builder: (context, state) => const MyProvider(),
  ),
  GoRoute(
    path: StateManagementRoutes.getX,
    builder: (context, state) => const MyGet(),
  ),
  GoRoute(
    path: StateManagementRoutes.getX2,
    builder: (context, state) => const MyGetX2(),
  ),
  GoRoute(
    path: StateManagementRoutes.bloC,
    builder: (context, state) => const MyBloC(),
  ),
];
