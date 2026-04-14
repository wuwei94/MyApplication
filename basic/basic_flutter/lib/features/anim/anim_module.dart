import 'package:basic_flutter/features/anim/lottie_example.dart';
import 'package:basic_flutter/features/anim/pag_example.dart';
import 'package:basic_flutter/features/anim/svg_example.dart';
import 'package:basic_flutter/features/anim/svga_example.dart';
import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:flutter/widgets.dart';

/// Anim 模块
/// 
/// 包含：SVG、SVGA、Lottie、PAG 等动画组件示例
class AnimModule {
  const AnimModule._();

  /// 首页目录入口
  RouteItem get catalog => RouteItem.section(
        path: '/animations',
        title: 'Animations',
        subtitle: '动画组件',
        routeItems: routes,
      );

  /// 所有路由列表
  List<RouteItem> get routes => _routes;

  static final List<RouteItem> _routes = [
    RouteItem.page(
      path: '/package/svg',
      title: 'SVG',
      subtitle: 'SVG动画示例',
      pageBuilder: (BuildContext context) => const SvgExample(title: 'SVG'),
    ),
    RouteItem.page(
      path: '/package/svga',
      title: 'SVGA',
      subtitle: 'SVGA动画示例',
      pageBuilder: (BuildContext context) => const SvgaExample(title: 'SVGA'),
    ),
    RouteItem.page(
      path: '/package/lottie',
      title: 'Lottie',
      subtitle: 'Lottie动画示例',
      pageBuilder: (BuildContext context) => const LottieExample(title: 'Lottie'),
    ),
    RouteItem.page(
      path: '/package/pag',
      title: 'PAG',
      subtitle: 'PAG动画示例',
      pageBuilder: (BuildContext context) => const PagExample(title: 'PAG'),
    ),
  ];
}

/// 单例实例
const AnimModule animModule = AnimModule._();
