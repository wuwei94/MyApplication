import 'package:basic_flutter/routes/constants/route_groups.dart';
import 'package:basic_flutter/features/layout/my_column.dart';
import 'package:basic_flutter/features/layout/my_flex.dart';
import 'package:basic_flutter/features/layout/my_flow.dart';
import 'package:basic_flutter/features/layout/my_row.dart';
import 'package:basic_flutter/features/layout/my_stack.dart';
import 'package:basic_flutter/features/layout/my_wrap.dart';
import 'package:go_router/go_router.dart';

/// Layout 布局路由
final List<GoRoute> layoutRoutes = [
  GoRoute(path: LayoutRoutes.row, builder: (context, state) => const MyRow()),
  GoRoute(
    path: LayoutRoutes.column,
    builder: (context, state) => const MyColumn(),
  ),
  GoRoute(path: LayoutRoutes.flex, builder: (context, state) => const MyFlex()),
  GoRoute(path: LayoutRoutes.wrap, builder: (context, state) => const MyWrap()),
  GoRoute(path: LayoutRoutes.flow, builder: (context, state) => const MyFlow()),
  GoRoute(
    path: LayoutRoutes.stack,
    builder: (context, state) => const MyStack(),
  ),
];
