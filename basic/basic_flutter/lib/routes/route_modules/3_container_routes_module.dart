import 'package:basic_flutter/routes/constants/route_groups.dart';
import 'package:basic_flutter/features/container/my_align.dart';
import 'package:basic_flutter/features/container/my_center.dart';
import 'package:basic_flutter/features/container/my_constrained_box.dart';
import 'package:basic_flutter/features/container/my_container.dart';
import 'package:basic_flutter/features/container/my_decorated_box.dart';
import 'package:basic_flutter/features/container/my_padding.dart';
import 'package:basic_flutter/features/container/my_sized_box.dart';
import 'package:go_router/go_router.dart';

/// Container 容器路由
final List<GoRoute> containerRoutes = [
  GoRoute(
    path: ContainerRoutes.container,
    builder: (context, state) => const MyContainer(),
  ),
  GoRoute(
    path: ContainerRoutes.padding,
    builder: (context, state) => const MyPadding(),
  ),
  GoRoute(
    path: ContainerRoutes.align,
    builder: (context, state) => const MyAlign(),
  ),
  GoRoute(
    path: ContainerRoutes.center,
    builder: (context, state) => const MyCenter(),
  ),
  GoRoute(
    path: ContainerRoutes.constrainedBox,
    builder: (context, state) => const MyConstrainedBox(),
  ),
  GoRoute(
    path: ContainerRoutes.decoratedBox,
    builder: (context, state) => const MyDecoratedBox(),
  ),
  GoRoute(
    path: ContainerRoutes.sizedBox,
    builder: (context, state) => const MySizedBox(),
  ),
];
