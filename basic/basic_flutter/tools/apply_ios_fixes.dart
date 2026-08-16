#!/usr/bin/env dart

// ignore_for_file: avoid_print
// 自动应用 Flutter iOS 侧常见修复
// 使用方法: dart tools/apply_ios_fixes.dart

import 'dart:convert';
import 'dart:io';

void main() {
  final List<String> scripts = <String>[
    'tools/ios/apply_ios_geolocator_info_plist.dart',
    'tools/ios/apply_ios_geolocator_podfile.dart',
  ];

  for (final String script in scripts) {
    // 使用当前 dart 可执行文件，避免依赖 PATH 中的 dart
    // 子进程 stdout/stderr 始终为 UTF-8，显式指定解码编码，避免 Windows GBK 代码页下中文乱码
    final ProcessResult result = Process.runSync(
      Platform.resolvedExecutable,
      <String>[script],
      stdoutEncoding: utf8,
      stderrEncoding: utf8,
    );

    stdout.write(result.stdout);
    stderr.write(result.stderr);

    if (result.exitCode != 0) {
      print('❌ 执行失败: $script');
      exit(result.exitCode);
    }
  }

  print('✅ 已完成 iOS module 修复脚本执行');
}
