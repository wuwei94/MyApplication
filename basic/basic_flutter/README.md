# basic_flutter

A new Flutter project.

## Getting Started

For help getting started with Flutter development, view the online
[documentation](https://flutter.dev/).

For instructions integrating Flutter modules to your existing applications,
see the [add-to-app documentation](https://flutter.dev/docs/development/add-to-app).

## Android Module Fixes

如果 `.android/` 重新生成后丢失了 Android 侧兼容修复，可执行：

```bash
dart tool/apply_android_fixes.dart
```

当前会自动补齐：

* `coreLibraryDesugaring` 配置
* Android权限 配置

## Basic_flutter

* 网络请求
    * [Dio](https://pub.dev/packages/dio)
    * [Http](https://pub.dev/packages/http)
* 三方框架
    * [Toast](https://pub.dev/packages/fluttertoast)
    * [Notification](https://pub.dev/packages/flutter_local_notifications)

    * [Image Picker](https://pub.dev/packages/image_picker)
    * [WeChat Assets Picker](https://pub.dev/packages/wechat_assets_picker)
    * [WeChat Camera Picker](https://pub.dev/packages/wechat_camera_picker)

    * [Secure Storage](https://pub.dev/packages/secure_storage)
    * [Shared Preferences](https://pub.dev/packages/shared_preferences)
    
    * [Webview](https://pub.dev/packages/webview_flutter)
    
    * [Permission](https://pub.dev/packages/permission_handler)
    * [ScreenUtil](https://pub.dev/packages/flutter_screenutil)
* 状态管理
    * [GetX](https://pub.dev/packages/get)
    * [BloC](https://pub.dev/packages/flutter_bloc)
    * [Provider](https://pub.dev/packages/provider)
    * [Riverpod](https://pub.dev/packages/flutter_riverpod)