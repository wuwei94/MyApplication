import 'package:flutter/widgets.dart';
import 'package:flutter_demo/catalog/models/catalog_entry.dart';
import 'package:flutter_demo/demos/layout/transitions/fade_transition_demo.dart';
import 'package:flutter_demo/demos/layout/transitions/rotation_transition_demo.dart';
import 'package:flutter_demo/demos/layout/transitions/scale_transition_demo.dart';
import 'package:flutter_demo/demos/layout/transitions/size_transition_demo.dart';
import 'package:flutter_demo/demos/layout/transitions/slide_transition_demo.dart';

final CatalogEntry transitionsCatalog = CatalogEntry.catalog(
  path: 'transitions',
  title: '过渡动画组件',
  subtitle: 'FadeTransition、ScaleTransition、RotationTransition...',
  children: <CatalogEntry>[
    CatalogEntry.page(
      path: 'fade-transition',
      title: 'FadeTransition',
      subtitle: '淡入淡出动画',
      pageBuilder: (BuildContext context) =>
          const FadeTransitionDemoPage(title: 'FadeTransition'),
    ),
    CatalogEntry.page(
      path: 'scale-transition',
      title: 'ScaleTransition',
      subtitle: '缩放动画',
      pageBuilder: (BuildContext context) =>
          const ScaleTransitionDemoPage(title: 'ScaleTransition'),
    ),
    CatalogEntry.page(
      path: 'rotation-transition',
      title: 'RotationTransition',
      subtitle: '旋转动画',
      pageBuilder: (BuildContext context) =>
          const RotationTransitionDemoPage(title: 'RotationTransition'),
    ),
    CatalogEntry.page(
      path: 'size-transition',
      title: 'SizeTransition',
      subtitle: '尺寸动画',
      pageBuilder: (BuildContext context) =>
          const SizeTransitionDemoPage(title: 'SizeTransition'),
    ),
    CatalogEntry.page(
      path: 'slide-transition',
      title: 'SlideTransition',
      subtitle: '滑动动画',
      pageBuilder: (BuildContext context) =>
          const SlideTransitionDemoPage(title: 'SlideTransition'),
    ),
  ],
);
