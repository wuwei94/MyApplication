#!/usr/bin/env dart
// ignore_for_file: avoid_print
// 修复 camera_android_camerax 0.7.4+5 在 AGP 9 下的 javac 编译失败:
// camera-core 1.6.1 把 androidx.concurrent:concurrent-futures 声明为 runtime 依赖,
// AGP 9 编译期走 api jar 类路径时该类缺失,导致 compileDebugJavaWithJavac 失败
// (flutter/flutter#190505)。
// 官方修复(flutter/packages#12373,尚未发版):给插件显式添加 concurrent-futures 依赖,
// 这里在 pub cache 的插件 build.gradle.kts 中应用同样的一行修复。
// 运行: dart tools/android/apply_android_camera_camerax.dart

import 'dart:convert';
import 'dart:io';

const String _plugin = 'camera_android_camerax';
const String _marker = '// concurrent-futures patched by apply_android_camera_camerax.dart';

/// 官方修复(flutter/packages#12373)新增的依赖
const String _dependency =
    '    implementation("androidx.concurrent:concurrent-futures:1.2.0")';

/// 依赖插入锚点:camera-video 声明行
const String _cameraVideoLine =
    'implementation("androidx.camera:camera-video:\${cameraxVersion}")';

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

  final file = File('${pluginDir.path}/android/build.gradle.kts');
  if (!file.existsSync()) {
    err('未找到 ${file.path}');
    return;
  }

  final content = file.readAsStringSync();

  if (content.contains(_marker) ||
      content.contains('androidx.concurrent:concurrent-futures')) {
    out('已修补，跳过');
    return;
  }

  if (!content.contains(_cameraVideoLine)) {
    err('未找到 camera-video 依赖锚点，插件结构可能已变化');
    return;
  }

  final updated = content.replaceFirst(
    _cameraVideoLine,
    '$_cameraVideoLine\n$_dependency $_marker',
  );
  file.writeAsStringSync(updated);
  out('已添加 concurrent-futures 依赖 (${file.path})');
}

/// 解析构建实际使用的插件目录:
/// 优先读 .flutter-plugins-dependencies(与 Gradle 构建使用同一路径);
/// 回退到扫描 pub cache 并取版本号最高的目录。
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
      // 解析失败时回退到缓存扫描
    }
  }

  final hosted = Directory('${pubCache.path}${Platform.pathSeparator}hosted');
  if (!hosted.existsSync()) return null;

  Directory? best;
  for (final mirror in hosted.listSync()) {
    if (mirror is! Directory) continue;
    for (final entity in mirror.listSync()) {
      if (entity is! Directory) continue;
      final name = entity.path.split(Platform.pathSeparator).last;
      if (!name.startsWith('$_plugin-')) continue;
      final version = name.substring(_plugin.length + 1);
      if (best == null || _compareVersion(version, _bestVersion(best)) > 0) {
        best = entity;
      }
    }
  }
  return best;
}

/// 当前最优候选目录的版本号
String _bestVersion(Directory dir) {
  final name = dir.path.split(Platform.pathSeparator).last;
  return name.substring(_plugin.length + 1);
}

/// 比较语义化版本(支持 0.7.4+5 形式),a > b 返回正数
int _compareVersion(String a, String b) {
  final reg = RegExp(r'^(\d+)\.(\d+)\.(\d+)(?:\+(\d+))?$');
  final ma = reg.firstMatch(a);
  final mb = reg.firstMatch(b);
  if (ma == null) return mb == null ? 0 : -1;
  if (mb == null) return 1;
  for (final i in [1, 2, 3, 4]) {
    final ca = int.tryParse(ma.group(i) ?? '0') ?? 0;
    final cb = int.tryParse(mb.group(i) ?? '0') ?? 0;
    if (ca != cb) return ca.compareTo(cb);
  }
  return 0;
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

void out(String s) => print('[apply_android_camera_camerax] $s');
void err(String s) {
  print('[apply_android_camera_camerax] $s');
  exit(1);
}
