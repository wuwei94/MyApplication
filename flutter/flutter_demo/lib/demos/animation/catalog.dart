import 'package:flutter/widgets.dart';
import 'package:flutter_demo/catalog/models/catalog_entry.dart';
import 'package:flutter_demo/catalog/models/catalog_section.dart';
import 'package:flutter_demo/demos/animation/lottie_demo.dart';
import 'package:flutter_demo/demos/animation/pag_demo.dart';
import 'package:flutter_demo/demos/animation/svg_demo.dart';
import 'package:flutter_demo/demos/animation/svga_demo.dart';

/// Animation 模块
///
/// 包含：SVG、SVGA、Lottie、PAG 等动画组件示例
class AnimationCatalog extends CatalogSection {
  const AnimationCatalog._();

  @override
  String get path => 'animation';

  @override
  String get title => 'Animation';

  @override
  String get subtitle => '矢量、序列与特效动画预览';

  @override
  List<CatalogEntry> get items => _items;

  static final List<CatalogEntry> _items = [
    CatalogEntry.page(
      path: 'svg',
      title: 'SVG',
      subtitle: '本地 SVG 资源渲染与预览',
      pageBuilder: (BuildContext context) => const SvgDemoPage(title: 'SVG'),
    ),
    CatalogEntry.page(
      path: 'lottie',
      title: 'Lottie',
      subtitle: 'Lottie JSON 动画资源预览',
      pageBuilder: (BuildContext context) =>
          const LottieDemoPage(title: 'Lottie'),
    ),
    CatalogEntry.page(
      path: 'svga',
      title: 'SVGA',
      subtitle: 'SVGA 动画资源加载与循环播放',
      pageBuilder: (BuildContext context) => const SvgaDemoPage(title: 'SVGA'),
    ),
    CatalogEntry.page(
      path: 'pag',
      title: 'PAG',
      subtitle: 'PAG 特效动画播放与适配展示',
      pageBuilder: (BuildContext context) => const PagDemoPage(title: 'PAG'),
    ),
  ];
}

/// 单例实例
const AnimationCatalog animationCatalog = AnimationCatalog._();
