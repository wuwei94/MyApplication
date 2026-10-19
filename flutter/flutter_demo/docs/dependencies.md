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

- [Drift](https://pub.dev/packages/drift) — SQLite 数据库，类型安全 DSL + 代码生成
- [Sqflite](https://pub.dev/packages/sqflite) — SQLite 数据库，手写 SQL 的薄封装（仅 Android / iOS / macOS）
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

网络图片加载由 `../flutter_libs/lib_image_loader` 本地 package 提供，与 Android `lib_image_loader` 结构对齐：

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

宿主工程（Gradle 9 + AGP 9）以 add-to-app 方式构建时，部分插件（如 `camera_android_camerax`、`flutter_plugin_android_lifecycle`、`image_picker_android` 等）的 `android/build.gradle.kts` 在配置阶段无法解析 `flutter.compileSdkVersion`，报 `Unresolved reference 'compileSdkVersion'`；模块独立构建（`flutter build apk/aar`）不受影响。

由 `tools/android/apply_android_flutter_compile_sdk.dart` 在 `flutter pub get` 之后统一处理：扫描 `.flutter-plugins-dependencies` 中实际参与构建的插件，把 `flutter.compileSdkVersion` 替换为从当前 Flutter SDK `FlutterExtension.kt` 解析出的字面值（当前为 37，与宿主工程 `compileSdk = 37` 一致），已注册进 `dart tools/apply_android_fixes.dart`。

> Flutter 升级后 `compileSdkVersion` 默认值变化时，重新执行一次该脚本即可；上游修复插件脚本后按 `apply_android_fixes.dart` 中的注释清理对应注册。

## TFLite Flutter 插件补丁（JVM 目标 + LiteRT 运行时升级）

`tflite_flutter` 0.12.1 有两处需要在本机修正，统一由
`tools/android/apply_android_tflite_flutter.dart` 在 `flutter pub get` 之后处理
（已注册进 `dart tools/apply_android_fixes.dart`）。

### 1. Kotlin / Java 编译目标不一致

插件的 `android/build.gradle` 声明了 `compileOptions { sourceCompatibility 11; targetCompatibility 11 }`，但未配置 `kotlinOptions.jvmTarget`。在 JDK 17/21+ 环境下，Kotlin Gradle Plugin 默认继承更高 JVM Target，导致 Kotlin 与 Java 编译目标不一致（报 `Inconsistent JVM Target Compatibility Between Java and Kotlin Tasks: compileDebugJavaWithJavac (11) and compileDebugKotlin (17)`）。

补丁补全 `kotlinOptions { jvmTarget = "11" }`。

### 2. LiteRT 运行时 1.4.0 → 2.3.0

插件默认依赖 `com.google.ai.edge.litert:litert:1.4.0`，对应原生库
`libtensorflowlite_jni.so`。**1.4.0 不解析「外置权重」布局**：litert-community 的部分
`.tflite` 把权重放在 flatbuffer 之外，用 `Buffer.offset`（字段 1）/ `Buffer.size`
（字段 2）按**绝对文件偏移**引用，`Buffer.data` 为空（`Model.version = 3`）。1.4.0 读不到
这些权重 → 权重张量 `data.raw == nullptr` → 任何 `invoke` 都会在 `Subgraph::Invoke()`
的节点预检处失败，报 `Input tensor %d lacks data`。

受影响的模型：Matcha TTS 全部 4 图、Kitten 共用的 G2P、Laya 零样本嵌入表、
Qwen3-TTS talker（int4 / fp32 均外置）、whisper 等 ASR 模型。

改成 `litert:2.3.0` 即可。2.3.0 的 `libLiteRt.so` 是 1.4.0 那个库的**超集**：同时导出
207 个 `TfLite*` 经典 C API 与 431 个 `LiteRt*` 新 C API，因此 **Dart 侧无需手写任何
`LiteRt*` 绑定**，只用把插件的动态库名指过去。

实测（同一探针二进制 / 同一模型 `matcha_decoder_fp16`，仅换运行时）：

| 运行时 | 无数据指针张量 | `invoke` 状态 |
|---|---|---|
| 1.4.0 `libtensorflowlite_jni.so` | 181 | 1（`Input tensor %d lacks data`） |
| 2.3.0 `libLiteRt.so` | 0 | 0 |

`litert-gpu` **保持 1.4.0 不动**：GPU delegate 的 `libtensorflowlite_gpu_jni.so` 只有
1.4.0 的 AAR 提供（2.3.0 换成了 `libLiteRtClGlAccelerator.so`，且 2.x 已无
`GpuDelegateV2`），而本仓视觉页仍在用 `GpuDelegateV2` 开关。

补丁同时把插件 `lib/src/bindings/bindings.dart` 里写死的
`DynamicLibrary.open('libtensorflowlite_jni.so')` 改成 `libLiteRt.so`。

> 注意：`litert:2.3.0` 的 `minSdkVersion` 是 24，与本工程 `minSdk = 24` 一致。
> 2.3.0 只提供 `arm64-v8a` / `armeabi-v7a` / `x86_64`，**没有 32 位 `x86`**。

### 3. 已知的上游绑定错误（不要踩）

`tflite_flutter` 0.12.1 的 `TfLiteInterpreterGetInputTensorCount` 被生成成
`int32_t f(Pointer<TfLiteInterpreter>)`，而真实 C 签名是
`void f(const TfLiteInterpreter*, int32_t* out_count)` —— **调用即错**。
需要输入个数时用公开 API `Interpreter.getInputTensors().length`。

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
- [Hiblob](https://pub.dev/packages/hiblob) — 确定性几何 blob 头像

## 蓝牙通信（本地 package）

低功耗蓝牙（BLE）能力由 `../flutter_libs/lib_bluetooth` 本地 package 提供，与 Android `module_bluetooth` 结构对齐：

- [lib_bluetooth](../flutter_libs/lib_bluetooth) — `package:lib_bluetooth/lib_bluetooth.dart`，`BleClient` 门面 + `BleSession` 会话封装；持有 flutter_blue_plus 依赖，内置大包自动切片分包（`writeChunked`）、流式通知监听与 Hex/CRC 编解码工具

主 `flutter_demo` 通过 path 依赖接入该 package，不再直接依赖 flutter_blue_plus。

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

## Markdown 富文本与代码高亮

对标 Android `module_markdown`，示例直接依赖以下三方包（`lib/demos/markdown/`）：

- [flutter_markdown_plus](https://pub.dev/packages/flutter_markdown_plus) — Markdown 渲染组件；flutter_markdown（Google 维护）已停更，本包为其社区延续维护版，API 兼容；默认 GitHub Flavored：标题/引用/列表/表格/任务清单复选/删除线/脚注/网络图片/链接，通过 `MarkdownStyleSheet` 定制主题、`SyntaxHighlighter` 单独控制代码块文字
- [flutter_highlight](https://pub.dev/packages/flutter_highlight) — 代码高亮 Widget，多语言多主题（暗黑 atom-one-dark / 明亮 github 等）
- [highlight](https://pub.dev/packages/highlight) — flutter_highlight 底层纯 Dart 词法分析库（`highlight.parse`），供耗时评测直接调用

## 图表库

- [fl_chart](https://pub.dev/packages/fl_chart) — 强大的 Flutter 数据可视化图表库（支持折线图、柱状图、饼图、雷达图、散点图与触摸 Tooltip 联动）
