import 'package:basic_flutter/routes/constants/route_groups.dart';
import 'package:basic_flutter/features/examples/my_counter.dart';
import 'package:go_router/go_router.dart';

/// 计数器示例路由
final List<GoRoute> exampleRoutes = [
  GoRoute(
    path: ExampleRoutes.counter,
    builder: (context, state) => const MyCounter(),
  ),
];
