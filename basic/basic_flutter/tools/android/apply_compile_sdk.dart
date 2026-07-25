#!/usr/bin/env dart
// ignore_for_file: avoid_print
// 直接修改 flutter_keyboard_visibility 插件的 android/build.gradle，
// 将 compileSdkVersion 对齐到项目使用的版本，避免 AAR metadata 不兼容。
// 运行: dart tools/android/apply_compile_sdk.dart
//
// 旧方案在 .android/build.gradle 注入 afterEvaluate 钩子，
// 但 CheckAarMetadataWorkAction 在配置阶段就执行，afterEvaluate 来不及生效。
// 新方案直接改插件自身的 build.gradle，在配置阶段之前就生效。

import 'dart:io';

const String _plugin = 'flutter_keyboard_visibility';
const String _marker = '// compileSdk patched by apply_compile_sdk.dart';

/// 需要对齐的最低 compileSdkVersion
const int _minCompileSdk = 34;

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

  if (content.contains(_marker)) {
    out('已修补,跳过');
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
    err('未找到 compileSdk/compileSdkVersion 声明');
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

/// 在 pub cache 中查找插件目录（支持带版本号后缀）
Directory? _findPluginDir(Directory pubCache) {
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

void out(String s) => print('[apply_compile_sdk] $s');
void err(String s) {
  print('[apply_compile_sdk] $s');
  exit(1);
}
