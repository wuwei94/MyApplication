import 'package:flutter/material.dart';
import 'package:flutter_markdown_plus/flutter_markdown_plus.dart';

/// 代码块文本着色器（仅作用于 Markdown `pre` 代码块内容）
///
/// flutter_markdown_plus 的 `MarkdownStyleSheet` 只有一个 `code` 文本样式，
/// 行内代码与代码块共用；而 [SyntaxHighlighter] 只会被用来格式化 `pre`
/// 元素内容（见 widget.dart `formatText`），因此借助它把「代码块文字颜色」
/// 与「行内代码颜色」解耦：暗色代码块背景上使用浅色文字，行内代码仍可
/// 独立配置高亮色。
///
/// 如需真正的多语言词法着色，可替换为 flutter_highlight 的 token 结果，
/// 但流式场景下无法预知代码块语言，此实现采用纯色方案保持稳定可读。
class MarkdownCodeHighlighter extends SyntaxHighlighter {
  MarkdownCodeHighlighter({this.textColor = const Color(0xFFABB2BF)});

  final Color textColor;

  @override
  TextSpan format(String source) {
    return TextSpan(
      text: source,
      style: TextStyle(
        color: textColor,
        fontFamily: 'monospace',
        fontSize: 12.5,
        height: 1.5,
      ),
    );
  }
}
