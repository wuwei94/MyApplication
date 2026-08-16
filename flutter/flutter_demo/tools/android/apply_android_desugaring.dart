#!/usr/bin/env dart
// ignore_for_file: avoid_print
// 为 Flutter 模块的 Android 项目添加 coreLibraryDesugaring 配置，
// 满足 flutter_local_notifications 等插件对 Java 8+ API 脱糖的要求。
// 运行: dart tools/android/apply_android_desugaring.dart

import 'dart:io';

void main() {
  final appBuildGradle = File('.android/app/build.gradle');

  if (!appBuildGradle.existsSync()) {
    print('❌ 找不到 .android/app/build.gradle 文件');
    print('   请先运行 flutter pub get 生成 Android 项目');
    exit(1);
  }

  String content = appBuildGradle.readAsStringSync();

  // 检查是否已经添加过配置
  if (content.contains('coreLibraryDesugaringEnabled')) {
    print('✅ coreLibraryDesugaring 配置已存在，无需修改');
    return;
  }

  // 1. 添加 coreLibraryDesugaringEnabled 到 compileOptions
  content = content.replaceFirst(
    'compileOptions {',
    'compileOptions {\n        coreLibraryDesugaringEnabled = true',
  );

  // 2. 添加 desugaring 依赖到 dependencies
  content = content.replaceFirst(
    'dependencies {',
    'dependencies {\n    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")',
  );

  appBuildGradle.writeAsStringSync(content);
  print('✅ 已成功添加 coreLibraryDesugaring 配置到 .android/app/build.gradle');
}
