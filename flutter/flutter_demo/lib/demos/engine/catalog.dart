import 'package:flutter/widgets.dart';
import 'package:flutter_demo/catalog/models/catalog_entry.dart';
import 'package:flutter_demo/catalog/models/catalog_section.dart';
import 'package:flutter_demo/demos/engine/custom_gesture_demo.dart';
import 'package:flutter_demo/demos/engine/flip_card_demo.dart';
import 'package:flutter_demo/demos/engine/fragment_shader_demo.dart';
import 'package:flutter_demo/demos/engine/particle_system_demo.dart';
import 'package:flutter_demo/demos/engine/path_animation_demo.dart';
import 'package:flutter_demo/demos/engine/ring_layout_demo.dart';
import 'package:flutter_demo/demos/engine/signature_pad_demo.dart';
import 'package:flutter_demo/demos/engine/staggered_animation_demo.dart';

/// Engine 模块
///
/// Flutter 引擎层特性深度示例：自绘渲染、动画与布局协议。
class EngineCatalog extends CatalogSection {
  const EngineCatalog._();

  @override
  String get path => 'engine';

  @override
  String get title => 'Engine';

  @override
  String get subtitle => '自绘、动画与布局协议';

  @override
  List<CatalogEntry> get items => _items;

  static final List<CatalogEntry> _items = [
    CatalogEntry.page(
      path: 'particle-system',
      title: 'Particle System',
      subtitle: 'Ticker 帧循环 + CustomPainter 触摸粒子',
      pageBuilder: (BuildContext context) =>
          const ParticleSystemDemoPage(title: 'Particle System'),
    ),
    CatalogEntry.page(
      path: 'signature-pad',
      title: 'Signature Pad',
      subtitle: '贝塞尔平滑手写与 PNG 离屏导出',
      pageBuilder: (BuildContext context) =>
          const SignaturePadDemoPage(title: 'Signature Pad'),
    ),
    CatalogEntry.page(
      path: 'ring-layout',
      title: 'Ring Layout',
      subtitle: '自定义 RenderObject 环形布局',
      pageBuilder: (BuildContext context) =>
          const RingLayoutDemoPage(title: 'Ring Layout'),
    ),
    CatalogEntry.page(
      path: 'staggered-animation',
      title: 'Staggered Animation',
      subtitle: 'AnimationController 交错编排',
      pageBuilder: (BuildContext context) =>
          const StaggeredAnimationDemoPage(title: 'Staggered Animation'),
    ),
    CatalogEntry.page(
      path: 'fragment-shader',
      title: 'Fragment Shader',
      subtitle: 'GLSL 运行时编译波纹着色器',
      pageBuilder: (BuildContext context) =>
          const FragmentShaderDemoPage(title: 'Fragment Shader'),
    ),
    CatalogEntry.page(
      path: 'path-animation',
      title: 'Path Animation',
      subtitle: 'PathMetric 路径测量与切线动画',
      pageBuilder: (BuildContext context) =>
          const PathAnimationDemoPage(title: 'Path Animation'),
    ),
    CatalogEntry.page(
      path: 'flip-card',
      title: 'Flip Card',
      subtitle: 'Matrix4 透视 + 3D 翻转卡片',
      pageBuilder: (BuildContext context) =>
          const FlipCardDemoPage(title: 'Flip Card'),
    ),
    CatalogEntry.page(
      path: 'custom-gesture',
      title: 'Custom Gesture',
      subtitle: '双指缩放旋转手势识别',
      pageBuilder: (BuildContext context) =>
          const CustomGestureDemoPage(title: 'Custom Gesture'),
    ),
  ];
}

/// 单例实例
const EngineCatalog engineCatalog = EngineCatalog._();
