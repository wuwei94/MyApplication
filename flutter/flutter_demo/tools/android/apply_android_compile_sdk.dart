#!/usr/bin/env dart
// ignore_for_file: avoid_print
// 将 flutter_keyboard_visibility 插件的 compileSdkVersion 对齐到项目使用的版本，
// 避免 AAR metadata 不兼容。
// 运行: dart tools/android/apply_android_compile_sdk.dart

import 'dart:convert';
import 'dart:io';

const String _plugin = 'flutter_keyboard_visibility';
const String _marker = '// compileSdk patched by apply_android_compile_sdk.dart';

/// 需要对齐的最低 compileSdkVersion
const int _minCompileSdk = 37;

/// 需要排除的平台子包后缀
const _excludeSuffixes = [
  '_linux',
  '_macos',
  '_web',
  '_windows',
  '_platform_interface',
  '_android',
];

void main() {
  final pluginDir = _findPluginDir();
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

  if (content.contains(_marker) ||
      content.contains('// compileSdk pinned by apply_android_flutter_compile_sdk.dart')) {
    final pinnedPattern = RegExp(r'(\d+)\s+// compileSdk (?:patched|pinned) by apply_android_.*\.dart');
    final match = pinnedPattern.firstMatch(content);
    if (match != null) {
      final pinnedVer = int.tryParse(match.group(1)!) ?? 0;
      if (pinnedVer < _minCompileSdk) {
        final updated = content.replaceAll(
          pinnedPattern,
          '$_minCompileSdk $_marker',
        );
        file.writeAsStringSync(updated);
        out('已将 compileSdkVersion 从 $pinnedVer 更新到 $_minCompileSdk (${file.path})');
      } else {
        out('已修补,跳过');
      }
      return;
    }
  }

  // 若已使用 flutter.compileSdkVersion，后续会由 apply_android_flutter_compile_sdk.dart 统一固定
  if (content.contains('flutter.compileSdkVersion')) {
    out('插件已使用 flutter.compileSdkVersion，交由通用脚本处理，跳过');
    return;
  }

  // 匹配 compileSdk 或 compileSdkVersion 行并替换值
  // 支持: compileSdkVersion 31 / compileSdk = 31 / compileSdk 31
  final sdkPattern = RegExp(
    r'(^\s*(?:compileSdk|compileSdkVersion)\s*[=]?\s*)(\d+)',
    multiLine: true,
  );
  final match = sdkPattern.firstMatch(content);
  if (match == null) {
    out('未检测到需要提升的硬编码 compileSdk/compileSdkVersion，跳过');
    return;
  }

  final currentSdk = int.tryParse(match.group(2)!) ?? 0;
  if (currentSdk >= _minCompileSdk) {
    out('compileSdkVersion $currentSdk >= $_minCompileSdk,无需修补');
    return;
  }

  final updated = content.replaceFirst(
    sdkPattern,
    '${match.group(1)}$_minCompileSdk $_marker',
  );
  file.writeAsStringSync(updated);
  out('已将 compileSdkVersion 从 $currentSdk 提升到 $_minCompileSdk (${file.path})');
}

/// 解析插件实际使用的目录：优先读取 .flutter-plugins-dependencies，回退到扫描 PUB_CACHE
Directory? _findPluginDir() {
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
      // 解析失败时回退到缓存扫描
    }
  }

  final pubCache = _findPubCache();
  if (pubCache == null) return null;

  final hosted = Directory('${pubCache.path}${Platform.pathSeparator}hosted');
  if (!hosted.existsSync()) return null;

  for (final mirror in hosted.listSync()) {
    if (mirror is! Directory) continue;
    for (final entity in mirror.listSync()) {
      if (entity is! Directory) continue;
      final name = entity.path.split(Platform.pathSeparator).last;
      if (!name.startsWith(_plugin)) continue;
      if (_excludeSuffixes.any((s) => name.contains(s))) continue;
      return entity;
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

  // 检查 local.properties 中的 Flutter SDK 路径下的 cache/hosted
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

void out(String s) => print('[apply_android_compile_sdk] $s');
void err(String s) {
  print('[apply_android_compile_sdk] $s');
  exit(1);
}
