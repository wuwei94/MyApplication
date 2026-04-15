import 'package:basic_flutter/app/catalog/catalog_item.dart';
import 'package:basic_flutter/demos/layout/animations/fade_transition_example.dart';
import 'package:basic_flutter/demos/layout/animations/rotation_transition_example.dart';
import 'package:basic_flutter/demos/layout/animations/scale_transition_example.dart';
import 'package:basic_flutter/demos/layout/animations/size_transition_example.dart';
import 'package:basic_flutter/demos/layout/animations/slide_transition_example.dart';
import 'package:flutter/widgets.dart';

final CatalogItem animationsCatalog = CatalogItem.catalog(
  path: 'animations',
  title: '动画效果',
  subtitle: 'FadeTransition、ScaleTransition、RotationTransition...',
  children: <CatalogItem>[
    CatalogItem.page(
      path: 'fade-transition',
      title: 'FadeTransition',
      subtitle: '淡入淡出动画',
      pageBuilder: (BuildContext context) =>
          const FadeTransitionExample(title: 'FadeTransition'),
    ),
    CatalogItem.page(
      path: 'scale-transition',
      title: 'ScaleTransition',
      subtitle: '缩放动画',
      pageBuilder: (BuildContext context) =>
          const ScaleTransitionExample(title: 'ScaleTransition'),
    ),
    CatalogItem.page(
      path: 'rotation-transition',
      title: 'RotationTransition',
      subtitle: '旋转动画',
      pageBuilder: (BuildContext context) =>
          const RotationTransitionExample(title: 'RotationTransition'),
    ),
    CatalogItem.page(
      path: 'size-transition',
      title: 'SizeTransition',
      subtitle: '尺寸动画',
      pageBuilder: (BuildContext context) =>
          const SizeTransitionExample(title: 'SizeTransition'),
    ),
    CatalogItem.page(
      path: 'slide-transition',
      title: 'SlideTransition',
      subtitle: '滑动动画',
      pageBuilder: (BuildContext context) =>
          const SlideTransitionExample(title: 'SlideTransition'),
    ),
  ],
);
