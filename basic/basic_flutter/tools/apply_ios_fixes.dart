#!/usr/bin/env dart
// ignore_for_file: avoid_print
// 自动应用 Flutter iOS 侧常见修复
// 使用方法: dart tools/apply_ios_fixes.dart

import 'dart:io';

void main() {
  final List<String> scripts = <String>[
    'tools/ios/apply_ios_geolocator.dart',
  ];

  for (final String script in scripts) {
    final ProcessResult result = Process.runSync('dart', <String>[script]);

    stdout.write(result.stdout);
    stderr.write(result.stderr);

    if (result.exitCode != 0) {
      print('❌ 执行失败: $script');
      exit(result.exitCode);
    }
  }

  print('✅ 已完成 iOS module 修复脚本执行');
}
