#!/usr/bin/env dart
// ignore_for_file: avoid_print
// tflite_flutter 插件补丁集合（必须在 flutter pub get 之后运行）。
//
// 补丁 1 —— kotlinOptions.jvmTarget = "11"
//   tflite_flutter 的 android/build.gradle 里设了 compileOptions Java 11，却没写
//   kotlinOptions.jvmTarget，在 JDK 21+ / JDK 25 下 Kotlin 会默认继承更高版本目标，
//   与 Java 11 不一致 → 编译中断。
//
// 补丁 2 —— LiteRT 运行时 1.4.0 → 2.3.0（与 Android 侧 module_ml 对齐）
//   0.12.1 默认依赖 `com.google.ai.edge.litert:litert:1.4.0`，其 `libtensorflowlite_jni.so`
//   **不解析「外置权重」布局**（Buffer.offset / Buffer.size 引用 flatbuffer 之外的权重，
//   Model.version = 3）。litert-community 的 Matcha / Laya / Qwen3-TTS / whisper 等模型
//   都是这个布局，1.4.0 下权重张量 data.raw == nullptr，任何 invoke 都会在
//   Subgraph::Invoke() 节点预检处报 `Input tensor %d lacks data`。
//   2.3.0 的 `libLiteRt.so` 是 1.4.0 那个库的**超集**（同时导出 TfLite* 经典 C API
//   与 LiteRt* 新 C API），加载即正常解析外置权重。
//   实测（同一探针二进制 / 同一模型 matcha_decoder_fp16，仅换运行时）：
//     1.4.0 → 无数据指针张量 181 个，Invoke 状态 1，报 `Input tensor %d lacks data`
//     2.3.0 → 无数据指针张量 0 个，Invoke 状态 0
//   `litert-gpu` 保持 1.4.0 不动：GPU delegate 的 `libtensorflowlite_gpu_jni.so`
//   只有 1.4.0 的 AAR 提供（2.3.0 换成了 `libLiteRtClGlAccelerator.so`，且 2.x 已无
//   GpuDelegateV2），而本仓的视觉页仍在用 `GpuDelegateV2` 开关。
//
// 补丁 3 —— Dart 侧动态库名
//   插件源码里写死了 `DynamicLibrary.open('libtensorflowlite_jni.so')`，与补丁 2 换过的
//   运行时文件名不符，需要改成 `libLiteRt.so`。
//
// 运行: dart tools/android/apply_android_tflite_flutter.dart
// 入口: dart tools/apply_android_fixes.dart

import 'dart:convert';
import 'dart:io';

const String _plugin = 'tflite_flutter';

const String _markerKotlin =
    '// kotlinOptions patched by apply_android_tflite_flutter.dart';
const String _markerLitert =
    '// litert 2.3.0 patched by apply_android_tflite_flutter.dart';
const String _markerLitertGpu =
    '// litert-gpu pinned by apply_android_tflite_flutter.dart';
const String _markerBindings =
    '// lib name patched by apply_android_tflite_flutter.dart';

const String _kotlinOptionsBlock = '''
    kotlinOptions {
        jvmTarget = "11"
    } $_markerKotlin''';

const String _litertOldVersion = 'def litert_version = "1.4.0"';
const String _litertNewVersion =
    'def litert_version = "2.3.0" $_markerLitert';

const String _litertGpuOld =
    'implementation("com.google.ai.edge.litert:litert-gpu:\${litert_version}")';
const String _litertGpuNew =
    'implementation("com.google.ai.edge.litert:litert-gpu:1.4.0") $_markerLitertGpu';

const String _bindingsOld =
    "    return DynamicLibrary.open('libtensorflowlite_jni.so');";
const String _bindingsNew = '    $_markerBindings\n'
    "    return DynamicLibrary.open('libLiteRt.so');";

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

  var failed = false;
  failed |= !_patchKotlinOptions(pluginDir);
  failed |= !_patchLitertVersion(pluginDir);
  failed |= !_patchBindingsLibName(pluginDir);

  if (failed) exit(1);
}

/// 补丁 1：补全 kotlinOptions.jvmTarget
bool _patchKotlinOptions(Directory pluginDir) {
  final file = File('${pluginDir.path}/android/build.gradle');
  if (!file.existsSync()) {
    err('未找到 ${file.path}');
    return false;
  }

  final content = file.readAsStringSync();
  if (content.contains(_markerKotlin)) {
    out('补丁 1（kotlinOptions）：已修补，跳过');
    return true;
  }

  final match =
      RegExp(r'compileOptions\s*\{[^}]*\}', multiLine: true).firstMatch(content);
  if (match == null) {
    err('补丁 1：未找到 compileOptions 声明锚点');
    return false;
  }

  file.writeAsStringSync(
    content.replaceFirst(
      match.group(0)!,
      '${match.group(0)!}\n\n$_kotlinOptionsBlock',
    ),
  );
  out('补丁 1（kotlinOptions）：已补全 jvmTarget = "11"');
  return true;
}

/// 补丁 2：litert 运行时升到 2.3.0，并把 litert-gpu 钉在 1.4.0
bool _patchLitertVersion(Directory pluginDir) {
  final file = File('${pluginDir.path}/android/build.gradle');
  if (!file.existsSync()) {
    err('未找到 ${file.path}');
    return false;
  }

  var content = file.readAsStringSync();
  var ok = true;

  if (content.contains(_markerLitert)) {
    out('补丁 2a（litert 2.3.0）：已修补，跳过');
  } else if (!content.contains(_litertOldVersion)) {
    err('补丁 2a：未找到锚点 `$_litertOldVersion`');
    ok = false;
  } else {
    content = content.replaceFirst(_litertOldVersion, _litertNewVersion);
    out('补丁 2a（litert 2.3.0）：1.4.0 → 2.3.0');
  }

  if (content.contains(_markerLitertGpu)) {
    out('补丁 2b（litert-gpu 1.4.0）：已修补，跳过');
  } else if (!content.contains(_litertGpuOld)) {
    err('补丁 2b：未找到锚点 `$_litertGpuOld`');
    ok = false;
  } else {
    content = content.replaceFirst(_litertGpuOld, _litertGpuNew);
    out('补丁 2b（litert-gpu 1.4.0）：已把 GPU delegate 运行时钉回 1.4.0');
  }

  if (ok) file.writeAsStringSync(content);
  return ok;
}

/// 补丁 3：Dart 侧动态库名改指 libLiteRt.so
bool _patchBindingsLibName(Directory pluginDir) {
  final file = File('${pluginDir.path}/lib/src/bindings/bindings.dart');
  if (!file.existsSync()) {
    err('未找到 ${file.path}');
    return false;
  }

  final content = file.readAsStringSync();
  if (content.contains(_markerBindings)) {
    out('补丁 3（动态库名）：已修补，跳过');
    return true;
  }
  if (!content.contains(_bindingsOld)) {
    err('补丁 3：未找到锚点 `$_bindingsOld`');
    return false;
  }

  file.writeAsStringSync(content.replaceFirst(_bindingsOld, _bindingsNew));
  out('补丁 3（动态库名）：libtensorflowlite_jni.so → libLiteRt.so');
  return true;
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

  final home =
      Platform.environment['HOME'] ?? Platform.environment['USERPROFILE'];
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
}
