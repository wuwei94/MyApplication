#!/usr/bin/env dart
// ignore_for_file: avoid_print
// 自动为 Flutter 模块的 iOS 项目添加 geolocator 所需的前台定位用途说明
// 使用方法: dart tools/ios/apply_ios_geolocator_info_plist.dart

import 'dart:io';

void main() {
  final File infoPlistFile = File('.ios/Runner/Info.plist');

  if (!infoPlistFile.existsSync()) {
    print('❌ 找不到 .ios/Runner/Info.plist 文件');
    print('   请先运行 flutter pub get 生成 iOS 项目');
    exit(1);
  }

  String content = infoPlistFile.readAsStringSync();
  const String locationUsageKey = 'NSLocationWhenInUseUsageDescription';

  if (content.contains(locationUsageKey)) {
    print('✅ iOS 前台定位用途说明已存在，无需修改');
    return;
  }

  const String sceneManifestAnchor = '\t<key>UIApplicationSceneManifest</key>';
  const String locationUsageEntry = '''
\t<key>NSLocationWhenInUseUsageDescription</key>
\t<string>Basic Flutter 需要在示例页面中读取当前位置。</string>''';

  if (!content.contains(sceneManifestAnchor)) {
    print('❌ 未找到可插入 iOS 定位用途说明的位置');
    print('   当前脚本依赖 .ios/Runner/Info.plist 中的默认结构');
    exit(1);
  }

  content = content.replaceFirst(
    sceneManifestAnchor,
    '$locationUsageEntry\n$sceneManifestAnchor',
  );
  infoPlistFile.writeAsStringSync(content);
  print('✅ 已添加 iOS 前台定位用途说明');
}
