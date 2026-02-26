import 'package:basic_flutter/routes/constants/route_groups.dart';
import 'package:basic_flutter/main.dart';
import 'package:go_router/go_router.dart';

/// 首页路由
final List<GoRoute> homeRoutes = [
  GoRoute(path: HomeRoutes.home, builder: (context, state) => const HomePage()),
];
