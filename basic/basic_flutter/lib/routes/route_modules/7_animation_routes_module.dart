import 'package:basic_flutter/features/animation/my_animation.dart';
import 'package:basic_flutter/routes/constants/route_groups.dart';
import 'package:go_router/go_router.dart';

final List<GoRoute> animationRoutes = [
  GoRoute(
    path: AnimationRoutes.animation,
    builder: (context, state) => const MyAnimation(),
  ),
];
