# 依赖清单

> 按分类列出所有三方包，详见 `pubspec.yaml`。

## 网络请求（本地 package）

网络请求能力由 `../flutter_libs/` 下的两个独立本地 package 提供：

- [lib_network_dio](https://pub.dev/packages/dio) — `package:lib_network_dio/lib_network_dio.dart`，DioClient；持有 Dio 依赖
- [lib_network_http](https://pub.dev/packages/http) — `package:lib_network_http/lib_network_http.dart`，HttpClient；持有 package:http、Async 与 Logger 依赖，启用日志后原样输出且不脱敏

主 `flutter_demo` 仅保留示例直接使用的 [Dio](https://pub.dev/packages/dio)、[Async](https://pub.dev/packages/async) 与 [web_socket_channel](https://pub.dev/packages/web_socket_channel)，并通过 path 依赖接入两个本地 package。两个 package 之间不存在共享的 `network_core`。

## 数据存储

按用途分为 KV 键值存储与数据库两类：KV 键值类由本地 package 统一封装，数据库类仅作技术选型示例。

### KV 键值存储（本地 package）

键值存储由 `../flutter_libs/lib_storage` 本地 package 提供：

- [lib_storage](../flutter_libs/lib_storage) — `package:lib_storage/lib_storage.dart`，`IStorage` 接口 + `Storage` 门面，默认内核 `HiveStorage`，可切换 `SharedPreferencesStorage`；持有 hive 与 shared_preferences 依赖；敏感数据不在本包范围，由业务侧直接使用 flutter_secure_storage

主 `flutter_demo` 通过 path 依赖接入该 package，同时为 Hive / SharedPreferences / SecureStorage 原生示例保留三个三方包直接依赖。Get Storage 仅用于 GetX 示例：

- [Get Storage](https://pub.dev/packages/get_storage) — GetX 存储

### 数据库

- [Drift](https://pub.dev/packages/drift) — SQLite 数据库
- [Isar](https://pub.dev/packages/isar) — NoSQL 数据库
- [ObjectBox](https://pub.dev/packages/objectbox) — ObjectBox 数据库
- [Path Provider](https://pub.dev/packages/path_provider) — 数据库路径管理

## 状态管理

- [GetX](https://pub.dev/packages/get) — GetX 状态管理
- [BloC](https://pub.dev/packages/flutter_bloc) — Bloc 状态管理
- [Cubit](https://pub.dev/packages/flutter_bloc) — Cubit 状态管理
- [Provider](https://pub.dev/packages/provider) — Provider 状态管理
- [Riverpod](https://pub.dev/packages/flutter_riverpod) — Riverpod 状态管理

## 路由框架

- [GoRouter](https://pub.dev/packages/go_router) — 声明式路由
- [AutoRoute](https://pub.dev/packages/auto_route) — 代码生成路由

## 图片加载（本地 package）

网络图片加载由 `../flutter_libs/lib_image_loader` 本地 package 提供，与 Android `lib_imageloader` 结构对齐：

- [lib_image_loader](../flutter_libs/lib_image_loader) — `package:lib_image_loader/image_loader.dart`，`IImageLoader` 接口 + `ImageLoader` 门面，内核为 `CachedNetworkImageLoader`，负责常规网络图加载、缓存、占位图与错误态；持有 cached_network_image 依赖，切换内核只需替换 `ImageLoader.kernel`，调用方零改动

主 `flutter_demo` 通过 path 依赖接入该 package，同时为 CachedNetworkImage / ExtendedImage 原生示例保留两个三方包直接依赖；extended_image 的大图查看场景不属于本包范围。其余图片与资源加载类三方依赖见下。

## 图片与资源加载

- [Cached Network Image](https://pub.dev/packages/cached_network_image) — 网络图片缓存（由 lib_image_loader 持有，原生示例直接使用）
- [Extended Image](https://pub.dev/packages/extended_image) — 增强图片组件
- [PhotoView](https://pub.dev/packages/photo_view) — 图片缩放
- [Image Picker](https://pub.dev/packages/image_picker) — 图片选择
- [WeChat Assets Picker](https://pub.dev/packages/wechat_assets_picker) — 微信风格选择器
- [WeChat Camera Picker](https://pub.dev/packages/wechat_camera_picker) — 微信相机选择器

## 相机插件临时修复

`camera` 传递依赖的 [camera_android_camerax](https://pub.dev/packages/camera_android_camerax) 0.7.4+5 固定使用 `camera-core 1.6.1`，该版本的 Gradle 元数据把 `androidx.concurrent:concurrent-futures` 声明为 runtime 依赖，AGP 9 编译期走 api jar 类路径时该依赖缺失，导致 `compileDebugJavaWithJavac` 失败（[flutter/flutter#190505](https://github.com/flutter/flutter/issues/190505)）。

官方修复（[flutter/packages#12373](https://github.com/flutter/packages/pull/12373)：给插件显式添加 `androidx.concurrent:concurrent-futures:1.2.0`）尚未发版，由 `tools/android/apply_android_camera_camerax.dart` 在 `flutter pub get` 之后对 pub cache 中的插件应用同一行修复（已注册进 `dart tools/apply_android_fixes.dart`）。

> 上游发布包含修复的新版本后，删除 `tools/android/apply_android_camera_camerax.dart` 及其在 `apply_android_fixes.dart` 中的注册即可。

## 插件 compileSdkVersion 固定

宿主工程（Gradle 9 + AGP 9，`android.newDsl=true`）以 add-to-app 方式构建时，部分插件（如 `camera_android_camerax`、`flutter_plugin_android_lifecycle`、`image_picker_android` 等）的 `android/build.gradle.kts` 在配置阶段无法解析 `flutter.compileSdkVersion`，报 `Unresolved reference 'compileSdkVersion'`；模块独立构建（`flutter build apk/aar`）不受影响。

由 `tools/android/apply_android_flutter_compile_sdk.dart` 在 `flutter pub get` 之后统一处理：扫描 `.flutter-plugins-dependencies` 中实际参与构建的插件，把 `flutter.compileSdkVersion` 替换为从当前 Flutter SDK `FlutterExtension.kt` 解析出的字面值（当前为 36，与宿主工程 `compileSdk = 36` 一致），已注册进 `dart tools/apply_android_fixes.dart`。

> Flutter 升级后 `compileSdkVersion` 默认值变化时，重新执行一次该脚本即可；上游修复插件脚本后按 `apply_android_fixes.dart` 中的注释清理对应注册。

## 动画与多媒体

- [Flutter SVG](https://pub.dev/packages/flutter_svg) — SVG 渲染
- [Flutter SVGA](https://pub.dev/packages/flutter_svga) — SVGA 动画
- [Lottie](https://pub.dev/packages/lottie) — Lottie 动画
- [PAG Flutter](https://pub.dev/packages/pag_flutter) — PAG 动画
- [Video Player](https://pub.dev/packages/video_player) — 视频播放
- [Chewie](https://pub.dev/packages/chewie) — 视频播放器

## UI 组件

- [ScreenUtil](https://pub.dev/packages/flutter_screenutil) — 屏幕适配
- [Easy Refresh](https://pub.dev/packages/easy_refresh) — 下拉刷新
- [Scroll To Index](https://pub.dev/packages/scroll_to_index) — 滚动定位
- [Flutter Slidable](https://pub.dev/packages/flutter_slidable) — 滑动操作
- [Extended Text Field](https://pub.dev/packages/extended_text_field) — 增强文本框
- [Keyboard Visibility](https://pub.dev/packages/flutter_keyboard_visibility) — 键盘可见性
- [Auto Size Text](https://pub.dev/packages/auto_size_text) — 自动缩放文本
- [ConstraintLayout](https://pub.dev/packages/flutter_constraintlayout) — 约束布局
- [Google Fonts](https://pub.dev/packages/google_fonts) — Google 字体

## 平台能力

- [Permission Handler](https://pub.dev/packages/permission_handler) — 权限管理
- [URL Launcher](https://pub.dev/packages/url_launcher) — URL 启动
- [Flutter Local Notifications](https://pub.dev/packages/flutter_local_notifications) — 本地通知
- [Webview Flutter](https://pub.dev/packages/webview_flutter) — WebView
- [Geolocator](https://pub.dev/packages/geolocator) — 定位

## 事件总线（本地 package）

事件总线由 `../flutter_libs/lib_event_bus` 本地 package 提供，与 Android `lib_eventbus` 结构对齐：

- [lib_event_bus](../flutter_libs/lib_event_bus) — `package:lib_event_bus/lib_event_bus.dart`，`FlutterEventBus` 单例封装；持有 event_bus 依赖，业务侧统一通过 `FlutterEventBus.instance` 收发事件

主 `flutter_demo` 通过 path 依赖接入该 package，不再直接依赖 event_bus。

## 工具库

- [Logger](https://pub.dev/packages/logger) — 日志工具
- [Toast](https://pub.dev/packages/fluttertoast) — Toast 提示
- [Uuid](https://pub.dev/packages/uuid) — UUID 生成
- [Freezed](https://pub.dev/packages/freezed) — 不可变数据类
- [Json Serializable](https://pub.dev/packages/json_serializable) — JSON 序列化
- [Flutter Linkify](https://pub.dev/packages/flutter_linkify) — 链接识别
- [Time Machine](https://pub.dev/packages/time_machine) — 时间处理
