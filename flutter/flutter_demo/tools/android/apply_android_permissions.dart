#!/usr/bin/env dart
// ignore_for_file: avoid_print
// 为 Flutter 模块的 Android 项目添加常用权限和 Manifest 声明。
// 运行: dart tools/android/apply_android_permissions.dart

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
    '    <uses-permission android:name="android.permission.BLUETOOTH" android:maxSdkVersion="30"/>',
    '    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" android:maxSdkVersion="30"/>',
    '    <uses-permission android:name="android.permission.BLUETOOTH_SCAN" android:usesPermissionFlags="neverForLocation"/>',
    '    <uses-permission android:name="android.permission.BLUETOOTH_CONNECT"/>',
    '    <uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE"/>',
    '    <uses-feature android:name="android.hardware.bluetooth_le" android:required="false"/>',
  ];

  final List<String> missingPermissions = permissions
      .where((String permission) => !content.contains(permission))
      .toList();
  const String imageCropperActivity = '''
        <activity
            android:name="com.yalantis.ucrop.UCropActivity"
            android:screenOrientation="portrait"
            android:theme="@style/Theme.AppCompat.Light.NoActionBar"/>''';
  final bool missingImageCropperActivity = !content.contains(
    'com.yalantis.ucrop.UCropActivity',
  );

  if (missingPermissions.isEmpty && !missingImageCropperActivity) {
    print('✅ 常用权限和 image_cropper Activity 已存在，无需修改');
    return;
  }

  const String permissionAnchor =
      '    <uses-permission android:name="android.permission.INTERNET"/>';

  if (missingPermissions.isNotEmpty && !content.contains(permissionAnchor)) {
    print('❌ 未找到可插入权限配置的位置');
    print('   当前脚本依赖 .android/app/src/main/AndroidManifest.xml 中的默认结构');
    exit(1);
  }

  if (missingPermissions.isNotEmpty) {
    content = content.replaceFirst(
      permissionAnchor,
      '$permissionAnchor\n${missingPermissions.join('\n')}',
    );
  }

  const String activityAnchor =
      '        <!-- Don\'t delete the meta-data below.';
  if (missingImageCropperActivity && !content.contains(activityAnchor)) {
    print('❌ 未找到可插入 image_cropper Activity 的位置');
    print('   当前脚本依赖 .android/app/src/main/AndroidManifest.xml 中的默认结构');
    exit(1);
  }

  if (missingImageCropperActivity) {
    content = content.replaceFirst(
      activityAnchor,
      '$imageCropperActivity\n$activityAnchor',
    );
  }

  manifestFile.writeAsStringSync(content);
  print('✅ 已成功更新 .android/app/src/main/AndroidManifest.xml');
}
