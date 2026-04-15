import 'package:basic_flutter/app/catalog/catalog_item.dart';
import 'package:basic_flutter/demos/layout/decoration_effects/backdrop_filter_example.dart';
import 'package:basic_flutter/demos/layout/decoration_effects/clip_example.dart';
import 'package:basic_flutter/demos/layout/decoration_effects/decoratedbox_example.dart';
import 'package:basic_flutter/demos/layout/decoration_effects/opacity_example.dart';
import 'package:basic_flutter/demos/layout/decoration_effects/shader_mask_example.dart';
import 'package:flutter/widgets.dart';

final CatalogItem decorationEffectsCatalog = CatalogItem.catalog(
  path: '/layout/decorations',
  title: '装饰效果',
  subtitle: 'DecoratedBox、Opacity、Clip、BackdropFilter、ShaderMask',
  children: <CatalogItem>[
    CatalogItem.page(
      path: '/layout/decorations/decorated-box',
      title: 'DecoratedBox',
      subtitle: '装饰盒子',
      pageBuilder: (BuildContext context) =>
          const DecoratedBoxExample(title: 'DecoratedBox'),
    ),
    CatalogItem.page(
      path: '/layout/decorations/opacity',
      title: 'Opacity',
      subtitle: '透明度',
      pageBuilder: (BuildContext context) =>
          const OpacityExample(title: 'Opacity'),
    ),
    CatalogItem.page(
      path: '/layout/decorations/clip',
      title: 'Clip',
      subtitle: '裁剪',
      pageBuilder: (BuildContext context) => const ClipExample(title: 'Clip'),
    ),
    CatalogItem.page(
      path: '/layout/decorations/backdrop-filter',
      title: 'BackdropFilter',
      subtitle: '背景滤镜',
      pageBuilder: (BuildContext context) =>
          const BackdropFilterExample(title: 'BackdropFilter'),
    ),
    CatalogItem.page(
      path: '/layout/decorations/shader-mask',
      title: 'ShaderMask',
      subtitle: '着色器遮罩',
      pageBuilder: (BuildContext context) =>
          const ShaderMaskExample(title: 'ShaderMask'),
    ),
  ],
);
