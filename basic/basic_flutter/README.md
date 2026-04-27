# basic_flutter

一个面向学习、验证和沉淀示例的 Flutter Demo Catalog 项目。

它不是面向业务场景的完整 App，而是一个可以持续扩展的示例目录：通过首页浏览不同 Demo 分组，进入目录页查看具体示例，再进入最终页面验证组件、框架或能力的使用方式。同时，这个仓库也是一个 Flutter module，可用于 add-to-app 集成。

## 项目特点

- Demo Catalog First 架构：`app` 负责壳层和路由，`catalog` 负责目录树和解析，`demos` 负责示例内容。
- 同时支持 `GoRouter` 和 `AutoRoute`，通过常量切换路由实现。
- 当前已收录 10 个顶层分组、93 个叶子示例页面。
- 既有基础 Flutter 组件示例，也有三方包、网络、存储、状态管理、动画、视频等专项示例。
- 保留 Flutter module 能力，可用于原生宿主 App 的 add-to-app 集成。

## 当前示例分组

| 分组 | 说明 |
| --- | --- |
| `basics` | 基础示例与完整小型示例应用，如 `counter`、`getx_app` |
| `layout` | 布局与交互类示例，含 `containers`、`linear`、`stack`、`flow`、`scroll`、`slivers`、`dialogs`、`transitions`、`asynchronous` 等子目录 |
| `showcase` | 杂项展示类示例，如本地字体 |
| `network` | 网络请求示例，如 `dio`、`http` |
| `image` | 图片加载与选择相关示例，如 `image_picker`、`wechat_picker`、`cached_network_image`、`extended_image` |
| `storage` | 本地存储示例，如 `shared_preferences`、`secure_storage`、`hive`、`path_provider` |
| `animation` | 动画资源和播放示例，如 `svg`、`lottie`、`svga`、`pag` |
| `video` | 视频播放示例，如 `video_player`、`chewie` |
| `packages` | 常用三方包示例，如 `notification`、`permission`、`webview`、`url_launcher`、`screen_util`、`easy_refresh` |
| `state_management` | 状态管理示例，如 `provider`、`cubit`、`bloc`、`riverpod` |

## 已覆盖依赖与示例

下面这部分依赖清单保留原有分组方式，并结合当前仓库内已经接入或已编写 Demo 的能力持续补充。

* 网络请求
    * [Dio](https://pub.dev/packages/dio)
    * [Http](https://pub.dev/packages/http)
* 数据存储
    * [freezed](https://pub.dev/packages/freezed)
    * [json_serializable](https://pub.dev/packages/json_serializable)
    * [freezed_annotation](https://pub.dev/packages/freezed_annotation)
    * [Shared Preferences](https://pub.dev/packages/shared_preferences)
    * [Secure Storage](https://pub.dev/packages/flutter_secure_storage)
    * [hive](https://pub.dev/packages/hive)
    * [hive_flutter](https://pub.dev/packages/hive_flutter)
    * [Path Provider](https://pub.dev/packages/path_provider)
    * [Get Storage](https://pub.dev/packages/get_storage)
* 三方框架
    * [GoRouter](https://pub.dev/packages/go_router)
    * [AutoRoute](https://pub.dev/packages/auto_route)
    * [Logger](https://pub.dev/packages/logger)
    * [Toast](https://pub.dev/packages/fluttertoast)
    * [Notification](https://pub.dev/packages/flutter_local_notifications)
    * [Image Picker](https://pub.dev/packages/image_picker)
    * [WeChat Assets Picker](https://pub.dev/packages/wechat_assets_picker)
    * [WeChat Camera Picker](https://pub.dev/packages/wechat_camera_picker)
    * [Webview](https://pub.dev/packages/webview_flutter)
    * [Url Launcher](https://pub.dev/packages/url_launcher)
    * [Flutter Linkify](https://pub.dev/packages/flutter_linkify)
    * [Permission](https://pub.dev/packages/permission_handler)
    * [Geolocator](https://pub.dev/packages/geolocator)
    * [ScreenUtil](https://pub.dev/packages/flutter_screenutil)
    * [Easy Refresh](https://pub.dev/packages/easy_refresh)
    * [Scroll To Index](https://pub.dev/packages/scroll_to_index)
    * [Flutter Slidable](https://pub.dev/packages/flutter_slidable)
    * [Extended Text Field](https://pub.dev/packages/extended_text_field)
    * [Keyboard Visibility](https://pub.dev/packages/flutter_keyboard_visibility)
    * [Auto Size Text](https://pub.dev/packages/auto_size_text)
    * [Time Machine](https://pub.dev/packages/time_machine)
    * [ConstraintLayout](https://pub.dev/packages/flutter_constraintlayout)
    * [Google Fonts](https://pub.dev/packages/google_fonts)
    * [Event Bus](https://pub.dev/packages/event_bus)
    * [Uuid](https://pub.dev/packages/uuid)
* 状态管理
    * [GetX](https://pub.dev/packages/get)
    * [BloC](https://pub.dev/packages/flutter_bloc)
    * [Cubit](https://pub.dev/packages/flutter_bloc)
    * [Provider](https://pub.dev/packages/provider)
    * [Riverpod](https://pub.dev/packages/flutter_riverpod)
* 图片与资源加载
    * [Cached Network Image](https://pub.dev/packages/cached_network_image)
    * [Extended Image](https://pub.dev/packages/extended_image)
* 动画与多媒体
    * [Flutter SVG](https://pub.dev/packages/flutter_svg)
    * [Flutter SVGA](https://pub.dev/packages/flutter_svga)
    * [Lottie](https://pub.dev/packages/lottie)
    * [PAG Flutter](https://pub.dev/packages/pag_flutter)
    * [Video Player](https://pub.dev/packages/video_player)
    * [Chewie](https://pub.dev/packages/chewie)

## 目录结构

```text
lib/
├── main.dart                          # 应用入口
├── app/                               # App 壳层、首页、导航与路由配置
│   ├── app.dart
│   ├── home/app_home.dart
│   ├── navigation/app_navigator.dart
│   └── router/
├── catalog/                           # Catalog 模型、目录页、注册表、路由工厂、路径解析
│   ├── models/
│   ├── pages/catalog_page.dart
│   ├── registry/catalog_registry.dart
│   ├── routing/catalog_route_factory.dart
│   └── services/catalog_tree_resolver.dart
├── demos/                             # 所有示例内容
│   ├── basics/
│   ├── layout/
│   ├── network/
│   ├── image/
│   ├── storage/
│   ├── animation/
│   ├── video/
│   ├── packages/
│   ├── state_management/
│   └── showcase/
├── core/                              # 通用工具能力：网络、日志、存储、UI 辅助等
├── l10n/                              # 多语言与字符串资源
└── boost/                             # add-to-app / Boost 相关桥接代码
```

## Catalog 设计约定

- 每个顶层分组使用一个 `catalog.dart` 描述目录结构。
- 如果分组内容较多，可以像 `layout/` 一样继续拆分子目录和子级 `catalog.dart`。
- `CatalogEntry.path` 必须使用相对路径。
- 顶层目录相对根路径，如 `basics`、`layout`、`network`。
- 子分组和叶子页面相对父目录路径，如 `containers`、`dio`、`shared-preferences`。
- 运行时绝对路径统一由 `lib/catalog/services/catalog_tree_resolver.dart` 解析。
- 新增顶层分组时，通常只需要修改对应分组下的 `catalog.dart`，并把它加入 `lib/catalog/registry/catalog_registry.dart`。

## 路由模式

项目同时支持 `GoRouter` 和 `AutoRoute`。

- 路由切换位置：`lib/app/router/app_router_type.dart`
- 当前默认值：`AppRouterType.autoRoute`
- 首页固定路由：`/`
- 新增普通 Demo 时，默认不需要手写额外路由文件，只需要维护对应 `catalog.dart`

## 快速开始

### 环境要求

- Flutter：项目通过 `.fvmrc` 固定为 `3.41.7`
- Dart SDK：`^3.10.8`
- 推荐使用 `fvm`

### 安装依赖

```bash
fvm flutter pub get
```

如果本机未安装 `fvm`，可以退回：

```bash
flutter pub get
```

### 运行项目

```bash
fvm flutter run
```

如果需要指定设备：

```bash
fvm flutter devices
fvm flutter run -d <device-id>
```

## 常用命令

### 静态检查

```bash
fvm flutter analyze
```

### 运行测试

```bash
fvm flutter test
```

### 代码格式化

```bash
dart format lib test tools
```

## 新增 Demo 的推荐方式

1. 优先把示例放进已有分类目录，例如 `lib/demos/network/`、`lib/demos/layout/containers/`。
2. 叶子示例页面统一使用 `xxx_example.dart` 命名。
3. 在对应分组或子分组的 `catalog.dart` 中通过 `CatalogEntry.page(...)` 或 `CatalogEntry.catalog(...)` 接入。
4. 只有在新增顶层分组时，才需要同步更新 `lib/catalog/registry/catalog_registry.dart`。
5. 除非用户明确要求，否则不要破坏现有 `flutter.module` 配置，也不要移除 `AppRouterType` 的双路由切换能力。

## Android Module 修复脚本

当 `.android/` 重新生成后，如果 Android 侧兼容修复丢失，可以执行：

```bash
dart tools/apply_android_fixes.dart
```

该脚本当前会自动补齐：

- `coreLibraryDesugaring` 配置
- 常用 Android 权限声明，包括通知、定位、相机、图片读取权限
- `image_cropper` 需要的 `UCropActivity` 声明

如果 `.android/` 目录尚未生成，请先执行一次 `flutter pub get` 或 `fvm flutter pub get`。

## iOS geolocator 配置脚本

当 `.ios/` 重新生成后，如果 geolocator 的 iOS 前台定位配置丢失，可以执行：

```bash
dart tools/apply_ios_geolocator.dart
```

该脚本会自动补齐：

- `NSLocationWhenInUseUsageDescription` 前台定位用途说明
- `geolocator_apple` 的 `BYPASS_PERMISSION_LOCATION_ALWAYS=1` Podfile 宏

## 开发约定

- 遵守 `analysis_options.yaml` 中的约束：只用 package import、显式返回类型、强类型。
- 优先使用 `const`、`final` 和职责单一的小组件。
- 不要使用 `print`；如需日志，优先使用 `lib/core/utils/logger/` 下的工具。
- 不要编辑生成目录或临时目录，如 `.dart_tool/`、`build/`。
- 现有代码和注释中英文混用，新增内容尽量保持周边风格一致。

## 适用场景

这个仓库适合用于：

- 快速查阅 Flutter 常见能力的最小可运行示例
- 验证三方包在当前 Flutter 版本下的集成方式
- 演示不同状态管理和路由方案的接入差异
- 作为 add-to-app Flutter module 的基础实验项目

## 补充说明

- 应用入口：`lib/main.dart`
- 首页：`lib/app/home/app_home.dart`
- 顶层目录注册表：`lib/catalog/registry/catalog_registry.dart`
- 当前项目包含一个位于 `lib/demos/basics/getx_app/` 的 GetX 完整示例应用，它有自己的内部导航结构
