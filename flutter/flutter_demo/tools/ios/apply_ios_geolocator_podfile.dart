#!/usr/bin/env dart
// ignore_for_file: avoid_print
// 自动为 Flutter 模块的 iOS 项目添加 geolocator_apple 的 Podfile 编译宏
// 使用方法: dart tools/ios/apply_ios_geolocator_podfile.dart

import 'dart:io';

void main() {
  final File podfile = File('.ios/Podfile');

  if (!podfile.existsSync()) {
    print('❌ 找不到 .ios/Podfile 文件');
    print('   请先运行 flutter pub get 生成 iOS 项目');
    exit(1);
  }

  String content = podfile.readAsStringSync();

  if (content.contains("target.name == 'geolocator_apple'")) {
    print('✅ geolocator_apple Podfile 宏已存在，无需修改');
    return;
  }

  const String buildSettingsAnchor =
      '    flutter_additional_ios_build_settings(target)';
  const String buildSettingsWithGeolocatorMacro = r'''
    flutter_additional_ios_build_settings(target)
    if target.name == 'geolocator_apple'
      target.build_configurations.each do |config|
        config.build_settings['GCC_PREPROCESSOR_DEFINITIONS'] ||= [
          '$(inherited)',
          'BYPASS_PERMISSION_LOCATION_ALWAYS=1',
        ]
      end
    end''';

  if (!content.contains(buildSettingsAnchor)) {
    print('❌ 未找到可插入 geolocator_apple Podfile 宏的位置');
    print('   当前脚本依赖 .ios/Podfile 中的默认 post_install 结构');
    exit(1);
  }

  content = content.replaceFirst(
    buildSettingsAnchor,
    buildSettingsWithGeolocatorMacro,
  );
  podfile.writeAsStringSync(content);
  print('✅ 已添加 geolocator_apple Podfile 宏');
}
