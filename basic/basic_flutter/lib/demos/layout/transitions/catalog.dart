import 'package:basic_flutter/catalog/models/catalog_entry.dart';
import 'package:basic_flutter/demos/layout/transitions/fade_transition_example.dart';
import 'package:basic_flutter/demos/layout/transitions/rotation_transition_example.dart';
import 'package:basic_flutter/demos/layout/transitions/scale_transition_example.dart';
import 'package:basic_flutter/demos/layout/transitions/size_transition_example.dart';
import 'package:basic_flutter/demos/layout/transitions/slide_transition_example.dart';
import 'package:flutter/widgets.dart';

final CatalogEntry transitionsCatalog = CatalogEntry.catalog(
  path: 'transitions',
  title: '动画效果',
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
