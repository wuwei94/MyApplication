import 'package:basic_flutter/features/layout/decoration_effects/backdrop_filter_example.dart';
import 'package:basic_flutter/features/layout/decoration_effects/clip_example.dart';
import 'package:basic_flutter/features/layout/decoration_effects/decoratedbox_example.dart';
import 'package:basic_flutter/features/layout/decoration_effects/opacity_example.dart';
import 'package:basic_flutter/features/layout/decoration_effects/shader_mask_example.dart';
import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:flutter/widgets.dart';

/// Decoration & Effects 路由
final List<RouteItem> decorationEffectsRoutes = [
  RouteItem.page(
    path: 'decorated-box',
    title: 'DecoratedBox',
    subtitle: '装饰盒子组件',
    pageBuilder: (BuildContext context) =>
        const DecoratedBoxExample(title: 'DecoratedBox'),
  ),
  RouteItem.page(
    path: 'opacity',
    title: 'Opacity',
    subtitle: '透明度组件',
    pageBuilder: (BuildContext context) =>
        const OpacityExample(title: 'Opacity'),
  ),
  RouteItem.page(
    path: 'clip',
    title: 'Clip',
    subtitle: '裁剪组件',
    pageBuilder: (BuildContext context) => const ClipExample(title: 'Clip'),
  ),
  RouteItem.page(
    path: 'backdrop-filter',
    title: 'BackdropFilter',
    subtitle: '毛玻璃效果',
    pageBuilder: (BuildContext context) =>
        const BackdropFilterExample(title: 'BackdropFilter'),
  ),
  RouteItem.page(
    path: 'shader-mask',
    title: 'ShaderMask',
    subtitle: '着色器遮罩',
    pageBuilder: (BuildContext context) =>
        const ShaderMaskExample(title: 'ShaderMask'),
  ),
];
