/// 示例页统一骨架 — 对应 Android `basic_shared.activity.Basic*Activity` 族。
///
/// | Android | Flutter | 适用 |
/// |---------|---------|------|
/// | BasicControlActivity | [BasicControlPage] | 纯操作列表 |
/// | BasicResponseActivity | [BasicResponsePage] | 控制台 + 操作列表 |
/// | BasicLayoutActivity | [BasicLayoutPage] | 预览画布 + 可选操作 |
/// | BasicImageActivity | [BasicImagePage] | 图片/动画 + 操作列表 |
library;

export 'package:flutter_demo/core/basic/basic_control_page.dart';
export 'package:flutter_demo/core/basic/basic_demo_colors.dart';
export 'package:flutter_demo/core/basic/basic_demo_dimens.dart';
export 'package:flutter_demo/core/basic/basic_image_page.dart';
export 'package:flutter_demo/core/basic/basic_layout_page.dart';
export 'package:flutter_demo/core/basic/basic_response_page.dart';
export 'package:flutter_demo/core/basic/demo_action_list.dart';
export 'package:flutter_demo/core/basic/demo_console.dart';
