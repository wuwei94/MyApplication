#!/usr/bin/env dart

// ignore_for_file: avoid_print
// 从 monorepo 根 local.properties 同步 Flutter 编译期 define
// 使用方法: dart tools/sync_dart_defines.dart
// 生成 flutter_demo/dart_defines.json（已 gitignore），供 --dart-define-from-file 使用

import 'dart:convert';
import 'dart:io';

/// local.properties 键 → dart-define 键（与 Android BuildConfig 字段名对齐）
const Map<String, String> _propertyToDefine = <String, String>{
  'deepseek.api.key': 'DEEPSEEK_API_KEY',
};

void main() {
  final Directory demoRoot = File.fromUri(Platform.script).parent.parent;
  final File localProperties = File(
    '${demoRoot.parent.parent.path}${Platform.pathSeparator}local.properties',
  );
  final File output = File(
    '${demoRoot.path}${Platform.pathSeparator}dart_defines.json',
  );

  if (!localProperties.existsSync()) {
    print('❌ 未找到 ${localProperties.path}');
    print('👉 请在工程根目录 local.properties 配置 deepseek.api.key=sk-xxxx');
    exit(1);
  }

  final Map<String, String> defines = <String, String>{};
  for (final String line in localProperties.readAsLinesSync()) {
    final String trimmed = line.trim();
    if (trimmed.isEmpty || trimmed.startsWith('#')) continue;
    final int index = trimmed.indexOf('=');
    if (index <= 0) continue;
    final String key = trimmed.substring(0, index).trim();
    final String value = trimmed.substring(index + 1).trim();
    final String? defineKey = _propertyToDefine[key];
    if (defineKey != null && value.isNotEmpty) {
      defines[defineKey] = value;
    }
  }

  // 与 Android Gradle 一致：local.properties 未配置时回退环境变量
  for (final MapEntry<String, String> entry in _propertyToDefine.entries) {
    final String defineKey = entry.value;
    if (defines.containsKey(defineKey)) continue;
    final String fromEnv = Platform.environment[defineKey] ?? '';
    if (fromEnv.isNotEmpty) {
      defines[defineKey] = fromEnv;
    }
  }

  output.writeAsStringSync('${jsonEncode(defines)}\n');
  if (defines.isEmpty) {
    print('⚠️ ${output.path} 为空：local.properties 未配置 '
        '${_propertyToDefine.keys.join(' / ')}，环境变量也未设置');
  } else {
    print('✓ 已写入 ${output.path}（${defines.keys.join(', ')}）');
  }
  print('→ 运行：fvm flutter run --dart-define-from-file=dart_defines.json');
}
