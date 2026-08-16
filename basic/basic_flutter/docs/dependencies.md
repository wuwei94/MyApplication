# 依赖清单

> 按分类列出所有三方包，详见 `pubspec.yaml`。

## 网络请求（本地 package）

网络请求能力由 `../basic_flutter_libs/` 下的两个独立本地 package 提供：

- [network_dio](https://pub.dev/packages/dio) — `package:network_dio/network_dio.dart`，DioClient；持有 Dio 依赖
- [network_http](https://pub.dev/packages/http) — `package:network_http/network_http.dart`，HttpClient；持有 package:http、Async 与 Logger 依赖，启用日志后原样输出且不脱敏

主 `basic_flutter` 仅保留示例直接使用的 [Dio](https://pub.dev/packages/dio) 与 [Async](https://pub.dev/packages/async)，并通过 path 依赖接入两个本地 package。两个 package 之间不存在共享的 `network_core`。

## 数据存储

- [Shared Preferences](https://pub.dev/packages/shared_preferences) — 键值存储
- [Secure Storage](https://pub.dev/packages/flutter_secure_storage) — 安全存储
- [Hive](https://pub.dev/packages/hive) — NoSQL 数据库
- [Hive Flutter](https://pub.dev/packages/hive_flutter) — Hive Flutter 集成
- [Path Provider](https://pub.dev/packages/path_provider) — 路径管理
- [Get Storage](https://pub.dev/packages/get_storage) — GetX 存储
- [Drift](https://pub.dev/packages/drift) — SQLite 数据库
- [Isar](https://pub.dev/packages/isar) — NoSQL 数据库
- [ObjectBox](https://pub.dev/packages/objectbox) — ObjectBox 数据库

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

网络图片加载由 `../basic_flutter_libs/image_loader` 本地 package 提供，与 Android `lib_imageloader` 结构对齐：

- [image_loader](../basic_flutter_libs/image_loader) — `package:image_loader/image_loader.dart`，`IImageLoader` 接口 + `ImageLoader` 门面，默认内核 `CachedNetworkImageLoader`；持有 cached_network_image 依赖，切换内核只需替换 `ImageLoader.kernel`，调用方零改动

主 `basic_flutter` 通过 path 依赖接入该 package，其余图片与资源加载类三方依赖见下。

## 图片与资源加载

- [Cached Network Image](https://pub.dev/packages/cached_network_image) — 网络图片缓存（由 image_loader 持有）
- [Extended Image](https://pub.dev/packages/extended_image) — 增强图片组件
- [PhotoView](https://pub.dev/packages/photo_view) — 图片缩放
- [Image Picker](https://pub.dev/packages/image_picker) — 图片选择
- [WeChat Assets Picker](https://pub.dev/packages/wechat_assets_picker) — 微信风格选择器
- [WeChat Camera Picker](https://pub.dev/packages/wechat_camera_picker) — 微信相机选择器

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

## 工具库

- [Logger](https://pub.dev/packages/logger) — 日志工具
- [Toast](https://pub.dev/packages/fluttertoast) — Toast 提示
- [Event Bus](https://pub.dev/packages/event_bus) — 事件总线
- [Uuid](https://pub.dev/packages/uuid) — UUID 生成
- [Freezed](https://pub.dev/packages/freezed) — 不可变数据类
- [Json Serializable](https://pub.dev/packages/json_serializable) — JSON 序列化
- [Flutter Linkify](https://pub.dev/packages/flutter_linkify) — 链接识别
- [Time Machine](https://pub.dev/packages/time_machine) — 时间处理
