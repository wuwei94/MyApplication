import 'package:basic_flutter/features/concurrency/my_isolate.dart';
import 'package:basic_flutter/routes/constants/route_groups.dart';
import 'package:go_router/go_router.dart';

final List<GoRoute> concurrencyRoutes = [
  GoRoute(
    path: ConcurrencyRoutes.isolate,
    builder: (context, state) => const MyIsolate(),
  ),
];
