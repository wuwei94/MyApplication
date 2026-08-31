#!/usr/bin/env dart
// ignore_for_file: avoid_print
// 修复 tflite_flutter 插件在 JDK 21+ / JDK 25 下 Kotlin 与 Java 编译目标不一致问题:
// tflite_flutter 的 android/build.gradle 中设置了 compileOptions Java 11，但未指定 kotlinOptions.jvmTarget，
// 导致在较新 JDK 环境下 Kotlin 默认继承高版本 target 触发编译中断。
// 运行: dart tools/android/apply_android_tflite_flutter.dart

import 'dart:convert';
import 'dart:io';

const String _plugin = 'tflite_flutter';
const String _marker = '// kotlinOptions patched by apply_android_tflite_flutter.dart';

const String _kotlinOptionsBlock = '''
    kotlinOptions {
        jvmTarget = "11"
    } $_marker''';

void main() {
  final pubCache = _findPubCache();
  if (pubCache == null) {
    err('无法定位 PUB_CACHE');
    return;
  }

  final pluginDir = _findPluginDir(pubCache);
  if (pluginDir == null) {
    err('未找到 $_plugin 插件目录，请先 flutter pub get');
    return;
  }

  final file = File('${pluginDir.path}/android/build.gradle');
  if (!file.existsSync()) {
    err('未找到 ${file.path}');
    return;
  }

  final content = file.readAsStringSync();

  if (content.contains(_marker) || content.contains('jvmTarget = "11"')) {
    out('已修补，跳过');
    return;
  }

  // 匹配 compileOptions 块作为插入锚点
  final compileOptionsPattern = RegExp(
    r'compileOptions\s*\{[^}]*\}',
    multiLine: true,
  );

  final match = compileOptionsPattern.firstMatch(content);
  if (match == null) {
    err('未找到 compileOptions 声明锚点');
    return;
  }

  final updated = content.replaceFirst(
    match.group(0)!,
    '${match.group(0)!}\n\n$_kotlinOptionsBlock',
  );

  file.writeAsStringSync(updated);
  out('已成功为 $_plugin 补全 kotlinOptions { jvmTarget = "11" } (${file.path})');
}

/// 解析插件目录：优先读取 .flutter-plugins-dependencies，回退到扫描 PUB_CACHE
Directory? _findPluginDir(Directory pubCache) {
  final depsFile = File('.flutter-plugins-dependencies');
  if (depsFile.existsSync()) {
    try {
      final json =
          jsonDecode(depsFile.readAsStringSync()) as Map<String, dynamic>;
      final android =
          ((json['plugins'] as Map<String, dynamic>?)?['android'] as List?)
              ?.cast<dynamic>() ??
          const <dynamic>[];
      for (final p in android) {
        if (p is Map<String, dynamic> && p['name'] == _plugin) {
          final dir = Directory(p['path'] as String);
          if (dir.existsSync()) return dir;
        }
      }
    } catch (_) {
      // 忽略解析失败
    }
  }

  final hosted = Directory('${pubCache.path}${Platform.pathSeparator}hosted');
  if (!hosted.existsSync()) return null;

  for (final mirror in hosted.listSync()) {
    if (mirror is! Directory) continue;
    for (final entity in mirror.listSync()) {
      if (entity is! Directory) continue;
      final name = entity.path.split(Platform.pathSeparator).last;
      if (name == _plugin || name.startsWith('$_plugin-')) {
        return entity;
      }
    }
  }
  return null;
}

/// 按优先级查找 PUB_CACHE 目录
Directory? _findPubCache() {
  final env = Platform.environment['PUB_CACHE'];
  if (env != null) {
    final dir = Directory(env);
    if (dir.existsSync()) return dir;
  }

  final localProperties = File('.android/local.properties');
  if (localProperties.existsSync()) {
    for (final line in localProperties.readAsLinesSync()) {
      if (line.startsWith('flutter.sdk=')) {
        final flutterSdk = line
            .substring('flutter.sdk='.length)
            .trim()
            .replaceAll(r'\\', r'/')
            .replaceAll(r'\', r'/');
        final dir = Directory('$flutterSdk/bin/cache/pkg');
        if (dir.existsSync()) return Directory('$flutterSdk/bin/cache');
        final hostedDir = Directory('$flutterSdk/cache/hosted');
        if (hostedDir.existsSync()) return Directory('$flutterSdk/cache');
      }
    }
  }

  final home = Platform.environment['HOME'] ??
      Platform.environment['USERPROFILE'];
  if (home != null) {
    for (final path in [
      '$home/.pub-cache',
      '$home/AppData/Local/Pub/Cache',
    ]) {
      final dir = Directory(path);
      if (dir.existsSync()) return dir;
    }
  }
  return null;
}

void out(String s) => print('[apply_android_tflite_flutter] $s');
void err(String s) {
  print('[apply_android_tflite_flutter] $s');
  exit(1);
}
