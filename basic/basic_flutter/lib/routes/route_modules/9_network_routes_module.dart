import 'package:basic_flutter/routes/constants/route_groups.dart';
import 'package:basic_flutter/features/network/my_dio.dart';
import 'package:go_router/go_router.dart';

/// Network 网络请求路由
final List<GoRoute> networkRoutes = [
  GoRoute(path: NetworkRoutes.dio, builder: (context, state) => const MyDio()),
];
