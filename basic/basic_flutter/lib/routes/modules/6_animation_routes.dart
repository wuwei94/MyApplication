import 'package:basic_flutter/features/6_animation/my_animation.dart';
import 'package:basic_flutter/routes/models/route_item_model.dart';
import 'package:flutter/widgets.dart';

/// Animation 动画路由
final List<RouteItem> animationRoutes = [
  RouteItem(
    name: 'Animation',
    path: '/animation',
    describe: 'Animation',
    builder: (BuildContext context, _) => const MyAnimation(),
  ),
];
