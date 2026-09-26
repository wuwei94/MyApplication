import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// extended_text_field — 富文本输入预览
///
/// 核心机制与避坑点：
/// 1. 依赖状态：`extended_text_field` 当前在 pubspec 中未启用，页面保持可编译骨架。
/// 2. 启用后：在 [buildPreview] 中接入 `ExtendedTextField` + `SpecialTextSpanBuilder`。
///
/// 官方参考：
/// https://pub.dev/packages/extended_text_field
class ExtendedTextFieldDemoPage extends BasicLayoutPage {
  const ExtendedTextFieldDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<ExtendedTextFieldDemoPage> createState() =>
      _ExtendedTextFieldDemoPageState();
}

class _ExtendedTextFieldDemoPageState
    extends BasicLayoutPageState<ExtendedTextFieldDemoPage> {
  @override
  Widget buildPreview() {
    return const Center(
      child: Padding(
        padding: EdgeInsets.all(24),
        child: Text(
          'extended_text_field 依赖尚未启用，页面骨架已就绪。',
          textAlign: TextAlign.center,
        ),
      ),
    );
  }
}
