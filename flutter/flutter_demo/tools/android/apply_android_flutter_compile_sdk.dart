#!/usr/bin/env dart
// ignore_for_file: avoid_print
// 将各插件 android/build.gradle(.kts) 中的 flutter.compileSdkVersion 替换为
// 当前 Flutter SDK 的字面 compileSdkVersion。
// 背景:host 构建（Gradle 9 + AGP 9 + newDsl）下,插件脚本在配置阶段无法解析
// FlutterExtension.compileSdkVersion,所有引用该值的 .kts 插件会依次报
// "Unresolved reference 'compileSdkVersion'"。
// 该问题与具体插件无关,因此这里统一扫描 .flutter-plugins-dependencies 中
// 实际参与构建的插件并固定为字面值,与 apply_android_camera_camerax.dart 的
// 单插件修复互补。
// 运行: dart tools/android/apply_android_flutter_compile_sdk.dart

import 'dart:convert';
import 'dart:io';

const String _marker = '// compileSdk pinned by apply_android_flutter_compile_sdk.dart';

/// 解析 Flutter SDK 失败时使用的回退值
/// （与 Flutter 3.47.0 的 FlutterExtension.compileSdkVersion 保持一致）
const int _fallbackCompileSdk = 36;

void main() {
  final compileSdk = _resolveCompileSdk();
  if (compileSdk == null) {
    err('无法解析 compileSdkVersion，请先 flutter pub get');
    return;
  }
  out('目标 compileSdkVersion: $compileSdk');

  final depsFile = File('.flutter-plugins-dependencies');
  if (!depsFile.existsSync()) {
    err('未找到 .flutter-plugins-dependencies，请先 flutter pub get');
    return;
  }

  List<dynamic> plugins = const <dynamic>[];
  try {
    final json = jsonDecode(depsFile.readAsStringSync()) as Map<String, dynamic>;
    plugins = ((json['plugins'] as Map<String, dynamic>?)?['android'] as List?)
            ?.cast<dynamic>() ??
        const <dynamic>[];
  } catch (_) {
    err('.flutter-plugins-dependencies 解析失败');
    return;
  }

  var patched = 0;
  for (final p in plugins) {
    if (p is! Map<String, dynamic>) continue;
    final name = p['name'] as String?;
    final path = p['path'] as String?;
    if (name == null || path == null) continue;

    final pluginDir = Directory(path);
    if (!pluginDir.existsSync()) continue;

    for (final buildFile in ['android/build.gradle', 'android/build.gradle.kts']) {
      final file = File('${pluginDir.path}${Platform.pathSeparator}$buildFile');
      if (!file.existsSync()) continue;
      final content = file.readAsStringSync();
      if (content.contains(_marker)) continue;
      if (!content.contains('flutter.compileSdkVersion')) continue;
      file.writeAsStringSync(
        content.replaceAll(
          'flutter.compileSdkVersion',
          '$compileSdk $_marker',
        ),
      );
      out('已固定 compileSdkVersion ($name/$buildFile)');
      patched++;
    }
  }

  if (patched == 0) {
    out('没有需要固定的插件，跳过');
  }
}

/// 从当前 Flutter SDK 的 FlutterExtension.kt 解析默认 compileSdkVersion
int? _resolveCompileSdk() {
  final localProperties = File('.android/local.properties');
  if (!localProperties.existsSync()) return null;

  String? flutterSdk;
  for (final line in localProperties.readAsLinesSync()) {
    if (line.startsWith('flutter.sdk=')) {
      flutterSdk =
          line.substring('flutter.sdk='.length).trim().replaceAll(r'\\', r'\');
      break;
    }
  }
  if (flutterSdk == null) return null;

  final extensionFile = File(
    '$flutterSdk/packages/flutter_tools/gradle/src/main/kotlin/FlutterExtension.kt',
  );
  if (!extensionFile.existsSync()) return _fallbackCompileSdk;

  final match = RegExp(r'val compileSdkVersion: Int = (\d+)')
      .firstMatch(extensionFile.readAsStringSync());
  return match == null ? _fallbackCompileSdk : int.tryParse(match.group(1)!);
}

void out(String s) => print('[apply_android_flutter_compile_sdk] $s');
void err(String s) {
  print('[apply_android_flutter_compile_sdk] $s');
  exit(1);
}
