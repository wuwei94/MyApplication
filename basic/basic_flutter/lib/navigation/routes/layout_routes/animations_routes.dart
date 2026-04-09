import 'package:basic_flutter/features/layout/animations/fade_transition_example.dart';
import 'package:basic_flutter/features/layout/animations/rotation_transition_example.dart';
import 'package:basic_flutter/features/layout/animations/scale_transition_example.dart';
import 'package:basic_flutter/features/layout/animations/size_transition_example.dart';
import 'package:basic_flutter/features/layout/animations/slide_transition_example.dart';
import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:flutter/widgets.dart';

/// Animations 路由
final List<RouteItem> animationsRoutes = [
  RouteItem(
    path: '/animations/fade-transition',
    title: 'FadeTransition',
    subtitle: '淡入淡出动画',
    pageBuilder: (BuildContext context) =>
        const FadeTransitionExample(title: 'FadeTransition'),
  ),
  RouteItem(
    path: '/animations/scale-transition',
    title: 'ScaleTransition',
    subtitle: '缩放动画',
    pageBuilder: (BuildContext context) =>
        const ScaleTransitionExample(title: 'ScaleTransition'),
  ),
  RouteItem(
    path: '/animations/rotation-transition',
    title: 'RotationTransition',
    subtitle: '旋转动画',
    pageBuilder: (BuildContext context) =>
        const RotationTransitionExample(title: 'RotationTransition'),
  ),
  RouteItem(
    path: '/animations/size-transition',
    title: 'SizeTransition',
    subtitle: '尺寸动画',
    pageBuilder: (BuildContext context) =>
        const SizeTransitionExample(title: 'SizeTransition'),
  ),
  RouteItem(
    path: '/animations/slide-transition',
    title: 'SlideTransition',
    subtitle: '滑动动画',
    pageBuilder: (BuildContext context) =>
        const SlideTransitionExample(title: 'SlideTransition'),
  ),
];
