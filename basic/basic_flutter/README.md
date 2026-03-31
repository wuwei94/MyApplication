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
* 数据存储
    * [freezed](https://pub.dev/packages/freezed)
    * [json_serializable](https://pub.dev/packages/json_serializable)
    * [Shared Preferences](https://pub.dev/packages/shared_preferences)
    * [Secure Storage](https://pub.dev/packages/secure_storage)
    * [hive](https://pub.dev/packages/hive)
    * [hive_flutter](https://pub.dev/packages/hive_flutter)
* 三方框架
    * [GoRouter](https://pub.dev/packages/go_router)
    * [Logger](https://pub.dev/packages/logger)
    * [Toast](https://pub.dev/packages/fluttertoast)
    * [Notification](https://pub.dev/packages/flutter_local_notifications)
    * [Image Picker](https://pub.dev/packages/image_picker)
    * [WeChat Assets Picker](https://pub.dev/packages/wechat_assets_picker)
    * [WeChat Camera Picker](https://pub.dev/packages/wechat_camera_picker)
    * [Webview](https://pub.dev/packages/webview_flutter)
    * [Permission](https://pub.dev/packages/permission_handler)
    * [ScreenUtil](https://pub.dev/packages/flutter_screenutil)
* 状态管理
    * [GetX](https://pub.dev/packages/get)
    * [BloC](https://pub.dev/packages/flutter_bloc)
    * [Provider](https://pub.dev/packages/provider)
    * [Riverpod](https://pub.dev/packages/flutter_riverpod)