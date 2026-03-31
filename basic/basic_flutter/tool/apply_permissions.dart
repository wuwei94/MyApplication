#!/usr/bin/env dart
// ignore_for_file: avoid_print
// 自动为 Flutter 模块的 Android 项目添加常用权限
// 使用方法: dart tool/apply_permissions.dart

import 'dart:io';

void main() {
  final File manifestFile = File('.android/app/src/main/AndroidManifest.xml');

  if (!manifestFile.existsSync()) {
    print('❌ 找不到 .android/app/src/main/AndroidManifest.xml 文件');
    print('   请先运行 flutter pub get 生成 Android 项目');
    exit(1);
  }

  String content = manifestFile.readAsStringSync();
  final List<String> permissions = <String>[
    '    <uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>',
    '    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>',
    '    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>',
    '    <uses-permission android:name="android.permission.CAMERA"/>',
    '    <uses-permission android:name="android.permission.READ_MEDIA_IMAGES"/>',
  ];

  final List<String> missingPermissions = permissions
      .where((String permission) => !content.contains(permission))
      .toList();

  if (missingPermissions.isEmpty) {
    print('✅ 常用权限已存在，无需修改');
    return;
  }

  const String anchor =
      '    <uses-permission android:name="android.permission.INTERNET"/>';

  if (!content.contains(anchor)) {
    print('❌ 未找到可插入权限配置的位置');
    print('   当前脚本依赖 .android/app/src/main/AndroidManifest.xml 中的默认结构');
    exit(1);
  }

  content = content.replaceFirst(
    anchor,
    '$anchor\n${missingPermissions.join('\n')}',
  );

  manifestFile.writeAsStringSync(content);
  print('✅ 已成功添加常用权限到 .android/app/src/main/AndroidManifest.xml');
}
