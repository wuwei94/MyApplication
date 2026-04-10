import 'package:basic_flutter/features/layout/gesture_interaction/gesturedetector_example.dart';
import 'package:basic_flutter/features/layout/gesture_interaction/pop_scope_example.dart';
import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:flutter/widgets.dart';

/// Gesture Interaction 路由
final List<RouteItem> gestureInteractionRoutes = [
  RouteItem(
    path: 'gesturedetector',
    title: 'GestureDetector',
    subtitle: '手势检测组件',
    pageBuilder: (BuildContext context) =>
        const GestureDetectorExample(title: 'GestureDetector'),
  ),
  RouteItem(
    path: 'pop-scope',
    title: 'PopScope',
    subtitle: '返回拦截组件',
    pageBuilder: (BuildContext context) =>
        const PopScopeExample(title: 'PopScope'),
  ),
];
