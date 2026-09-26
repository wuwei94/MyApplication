import 'dart:convert';

import 'package:flutter/material.dart';

/// 控制台色板 — 与 Android `basic_shared` `shared_color_console_*` 对齐。
///
/// Why：双端示例页共用同一套终端视觉，降低跨栈对照时的认知切换成本。
abstract final class BasicDemoColors {
  /// 控制台主体背景（Slate 800）。
  static const Color consoleBg = Color(0xFF1E293B);

  /// 控制台 Header 背景（Slate 900）。
  static const Color consoleHeaderBg = Color(0xFF0F172A);

  /// 控制台描边 / 分割线。
  static const Color consoleBorder = Color(0xFF334155);

  /// 常规日志正文。
  static const Color consoleText = Color(0xFFE2E8F0);

  /// 时间戳与页面说明（弱化）。
  static const Color consoleTime = Color(0xFF94A3B8);

  /// 页面初始说明文字。
  static const Color consoleDesc = Color(0xFF94A3B8);

  /// 强调 / 清空按钮 / 强调日志。
  static const Color consoleAccent = Color(0xFF38BDF8);
}

/// 格式化 JSON 对象或数组；非 JSON 以及解析失败的内容保持原样。
///
/// 对齐 Android `BasicResponseActivity.formatJson`。
String formatDemoJson(String value) {
  final String content = value.trim();
  if (!content.startsWith('{') && !content.startsWith('[')) {
    return value;
  }
  try {
    final Object? decoded = jsonDecode(content);
    return const JsonEncoder.withIndent('  ').convert(decoded);
  } on Object {
    return value;
  }
}
