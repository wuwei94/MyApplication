#!/usr/bin/env dart
// ignore_for_file: avoid_print
// 在 Flutter Android 根工程(.android/build.gradle)注入 subprojects 钩子
// 让 flutter_keyboard_visibility 使用 flutter.compileSdkVersion,避免 AAR metadata 不兼容
// 运行: dart tools/android/apply_compile_sdk.dart

import 'dart:io';

const String _header = '// compileSdk helper injected by tools/android/apply_compile_sdk.dart';
const String _plugin = 'flutter_keyboard_visibility';

void main() {
  final File file = File('.android/build.gradle');
  if (!file.existsSync()) {
    err('未找到 .android/build.gradle,请先 flutter pub get');
    return;
  }
  String content = file.readAsStringSync();

  if (content.contains(_header)) {
    out('钩子已存在,跳过');
    return;
  }

  final int at = _afterBlock(content, 'allprojects');
  if (at < 0) {
    err('无法定位 allprojects 块结束位置');
    return;
  }

  final String hook = '''
$_header
subprojects {
    afterEvaluate { project ->
        if (project.name == "$_plugin") {
            project.android.compileSdkVersion flutter.compileSdkVersion
        }
    }
}
''';
  final String updated =
      content.substring(0, at) + '\n' + hook + '\n' + content.substring(at);
  file.writeAsStringSync(updated);
  out('已添加 \$_plugin compileSdkVersion 对齐钩子');
}

int _afterBlock(String source, String name) {
  final m = RegExp('${RegExp.escape(name)}\\s*\\{').firstMatch(source);
  if (m == null) return -1;
  int depth = 0;
  for (int i = m.end - 1; i < source.length; i++) {
    if (source[i] == '{') depth++;
    if (source[i] == '}') {
      depth--;
      if (depth == 0) return i + 1;
    }
  }
  return -1;
}

void out(String s) => // ignore: avoid_print
    print('[apply_compile_sdk] $s');
void err(String s) {
  // ignore: avoid_print
  print('[apply_compile_sdk] $s');
  exit(1);
}
