import 'package:basic_flutter/routes/constants/route_groups.dart';
import 'package:basic_flutter/features/functional/my_future_builder.dart';
import 'package:basic_flutter/features/functional/my_gesture_detector.dart';
import 'package:basic_flutter/features/functional/my_inherited_widget.dart';
import 'package:basic_flutter/features/functional/my_layout_builder.dart';
import 'package:basic_flutter/features/functional/my_pop_scope.dart';
import 'package:basic_flutter/features/functional/my_stream_builder.dart';
import 'package:basic_flutter/features/functional/my_value_listenable_builder.dart';
import 'package:go_router/go_router.dart';

/// Functional 功能组件路由
final List<GoRoute> functionalRoutes = [
  GoRoute(
    path: FunctionalRoutes.layoutBuilder,
    builder: (context, state) => const MyLayoutBuilder(),
  ),
  GoRoute(
    path: FunctionalRoutes.gestureDetector,
    builder: (context, state) => const MyGestureDetector(),
  ),
  GoRoute(
    path: FunctionalRoutes.popScope,
    builder: (context, state) => const MyPopScope(),
  ),
  GoRoute(
    path: FunctionalRoutes.inheritedWidget,
    builder: (context, state) => const MyInheritedWidget(),
  ),
  GoRoute(
    path: FunctionalRoutes.valueListenableBuilder,
    builder: (context, state) => const MyValueListenableBuilder(),
  ),
  GoRoute(
    path: FunctionalRoutes.futureBuilder,
    builder: (context, state) => const MyFutureBuilder(),
  ),
  GoRoute(
    path: FunctionalRoutes.streamBuilder,
    builder: (context, state) => const MyStreamBuilder(),
  ),
];
