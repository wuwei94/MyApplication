import 'package:basic_flutter/demos/anim/lottie_example.dart';
import 'package:basic_flutter/demos/anim/pag_example.dart';
import 'package:basic_flutter/demos/anim/svg_example.dart';
import 'package:basic_flutter/demos/anim/svga_example.dart';
import 'package:basic_flutter/app/catalog/catalog_item.dart';
import 'package:basic_flutter/app/catalog/catalog_module.dart';
import 'package:flutter/widgets.dart';

/// Anim 模块
/// 
/// 包含：SVG、SVGA、Lottie、PAG 等动画组件示例
class AnimModule extends CatalogModule {
  const AnimModule._();

  @override
  String get path => '/animations';

  @override
  String get title => 'Animations';

  @override
  String get subtitle => '动画组件';

  @override
  List<CatalogItem> get items => _items;

  static final List<CatalogItem> _items = [
    CatalogItem.page(
      path: 'svg',
      title: 'SVG',
      subtitle: 'SVG动画示例',
      pageBuilder: (BuildContext context) => const SvgExample(title: 'SVG'),
    ),
    CatalogItem.page(
      path: 'svga',
      title: 'SVGA',
      subtitle: 'SVGA动画示例',
      pageBuilder: (BuildContext context) => const SvgaExample(title: 'SVGA'),
    ),
    CatalogItem.page(
      path: 'lottie',
      title: 'Lottie',
      subtitle: 'Lottie动画示例',
      pageBuilder: (BuildContext context) => const LottieExample(title: 'Lottie'),
    ),
    CatalogItem.page(
      path: 'pag',
      title: 'PAG',
      subtitle: 'PAG动画示例',
      pageBuilder: (BuildContext context) => const PagExample(title: 'PAG'),
    ),
  ];
}

/// 单例实例
const AnimModule animModule = AnimModule._();
