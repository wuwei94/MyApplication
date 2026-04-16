import 'package:basic_flutter/demos/animation/lottie_example.dart';
import 'package:basic_flutter/demos/animation/pag_example.dart';
import 'package:basic_flutter/demos/animation/svg_example.dart';
import 'package:basic_flutter/demos/animation/svga_example.dart';
import 'package:basic_flutter/catalog/models/catalog_entry.dart';
import 'package:basic_flutter/catalog/models/catalog_section.dart';
import 'package:flutter/widgets.dart';

/// Animation 模块
///
/// 包含：SVG、SVGA、Lottie、PAG 等动画组件示例
class AnimationCatalog extends CatalogSection {
  const AnimationCatalog._();

  @override
  String get path => 'animation';

  @override
  String get title => 'Animation Example';

  @override
  String get subtitle => '动画组件';

  @override
  List<CatalogEntry> get items => _items;

  static final List<CatalogEntry> _items = [
    CatalogEntry.page(
      path: 'svg',
      title: 'SVG',
      subtitle: 'SVG动画示例',
      pageBuilder: (BuildContext context) => const SvgDemoPage(title: 'SVG'),
    ),
    CatalogEntry.page(
      path: 'svga',
      title: 'SVGA',
      subtitle: 'SVGA动画示例',
      pageBuilder: (BuildContext context) => const SvgaDemoPage(title: 'SVGA'),
    ),
    CatalogEntry.page(
      path: 'lottie',
      title: 'Lottie',
      subtitle: 'Lottie动画示例',
      pageBuilder: (BuildContext context) => const LottieDemoPage(title: 'Lottie'),
    ),
    CatalogEntry.page(
      path: 'pag',
      title: 'PAG',
      subtitle: 'PAG动画示例',
      pageBuilder: (BuildContext context) => const PagDemoPage(title: 'PAG'),
    ),
  ];
}

/// 单例实例
const AnimationCatalog animationCatalog = AnimationCatalog._();
