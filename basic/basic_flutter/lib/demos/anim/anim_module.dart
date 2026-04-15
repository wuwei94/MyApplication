import 'package:basic_flutter/demos/anim/lottie_example.dart';
import 'package:basic_flutter/demos/anim/pag_example.dart';
import 'package:basic_flutter/demos/anim/svg_example.dart';
import 'package:basic_flutter/demos/anim/svga_example.dart';
import 'package:basic_flutter/app/catalog/catalog_item.dart';
import 'package:flutter/widgets.dart';

/// Anim 模块
/// 
/// 包含：SVG、SVGA、Lottie、PAG 等动画组件示例
class AnimModule {
  const AnimModule._();

  /// 首页目录入口
  CatalogItem get catalog => CatalogItem.catalog(
        path: '/animations',
        title: 'Animations',
        subtitle: '动画组件',
        children: routes,
      );

  /// 所有路由列表
  List<CatalogItem> get routes => _routes;

  static final List<CatalogItem> _routes = [
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
