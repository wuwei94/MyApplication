import 'package:basic_flutter/catalog/models/catalog_entry.dart';
import 'package:basic_flutter/demos/layout/decoration/backdrop_filter_example.dart';
import 'package:basic_flutter/demos/layout/decoration/clip_example.dart';
import 'package:basic_flutter/demos/layout/decoration/decoratedbox_example.dart';
import 'package:basic_flutter/demos/layout/decoration/opacity_example.dart';
import 'package:basic_flutter/demos/layout/decoration/shader_mask_example.dart';
import 'package:flutter/widgets.dart';

final CatalogEntry decorationCatalog = CatalogEntry.catalog(
  path: 'decoration',
  title: '装饰组件',
  subtitle: 'DecoratedBox、Opacity、Clip、BackdropFilter、ShaderMask',
  children: <CatalogEntry>[
    CatalogEntry.page(
      path: 'decorated-box',
      title: 'DecoratedBox',
      subtitle: '装饰盒子',
      pageBuilder: (BuildContext context) =>
          const DecoratedBoxDemoPage(title: 'DecoratedBox'),
    ),
    CatalogEntry.page(
      path: 'opacity',
      title: 'Opacity',
      subtitle: '透明度',
      pageBuilder: (BuildContext context) =>
          const OpacityDemoPage(title: 'Opacity'),
    ),
    CatalogEntry.page(
      path: 'clip',
      title: 'Clip',
      subtitle: '裁剪',
      pageBuilder: (BuildContext context) => const ClipDemoPage(title: 'Clip'),
    ),
    CatalogEntry.page(
      path: 'backdrop-filter',
      title: 'BackdropFilter',
      subtitle: '背景滤镜',
      pageBuilder: (BuildContext context) =>
          const BackdropFilterDemoPage(title: 'BackdropFilter'),
    ),
    CatalogEntry.page(
      path: 'shader-mask',
      title: 'ShaderMask',
      subtitle: '着色器遮罩',
      pageBuilder: (BuildContext context) =>
          const ShaderMaskDemoPage(title: 'ShaderMask'),
    ),
  ],
);
