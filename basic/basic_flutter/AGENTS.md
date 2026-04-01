# basic_flutter

## 项目概览

- 这是一个 Flutter 示例和学习项目，覆盖基础组件、路由、网络请求、本地存储、动画示例，以及多种状态管理方案。
- 项目同时也是一个 Flutter module，用于 add-to-app；除非用户明确要求，否则不要修改 `pubspec.yaml` 里的 `flutter.module` 标识。
- 应用入口是 `lib/main.dart`。
- 路由核心配置在 `lib/navigation/app_router.dart`，使用 `GoRouter`。

## 目录结构

- `lib/features/home/`：首页和顶层功能入口。
- `lib/features/examples/`：基础示例和独立实验区，例如 counter、singleton，以及较完整的 GetX 示例应用。
- `lib/features/network/`：网络请求示例，例如 `dio`、`http`、图片加载。
- `lib/features/storage/`：本地存储示例，例如 `shared_preferences`、`secure_storage`、`hive`。
- `lib/features/anim/`：动画资源和播放示例，例如 `lottie`、`svg`、`svga`、`pag`。
- `lib/features/packages/`：三方包示例，例如 `notification`、`permission`、`image_picker`、`wechat_picker`、`webview`、`flutter_screenutil`、`toast`。
- `lib/features/state_manager/`：`provider`、`bloc`、`getx`、`riverpod` 的并列示例。
- `lib/features/features_list_page.dart`：首页某个分组点击后进入的二级功能列表页。
- `lib/navigation/modules/`：各个功能分组的路由列表，当前通过 `index.dart` 聚合导出。
- `lib/navigation/registry/route_registry.dart`：首页展示的顶层分组注册表。
- `lib/navigation/models/` 与 `lib/navigation/utils/`：路由模型和路由转换工具。
- `lib/core/`：通用工具，例如网络、日志、存储、UI 辅助工具。
- `lib/data/`：基础 model、datasource 抽象、repository 抽象。
- `lib/l10n/`：字符串和国际化相关辅助代码。
- `lib/boost/`：add-to-app/Boost 相关桥接代码。
- `tool/`：项目辅助脚本，例如 Android 权限、兼容性修复脚本。
- `assets/` 和 `images/` 已在 `pubspec.yaml` 中声明。

## 路由模式

- `/` 对应 `HomePage`。
- `HomePage` 通过 `Navigator.push` 打开某个分组的 `FeaturesListPage`。
- `FeaturesListPage` 再通过 `context.push(item.path)` 进入具体示例页面。
- 当前首页分组由 `lib/navigation/registry/route_registry.dart` 注册，分组路由列表由 `lib/navigation/modules/index.dart` 聚合。
- 新增示例页面时，通常需要更新 `lib/navigation/modules/` 下对应的路由文件，并确认该路由列表已经接入 `lib/navigation/app_router.dart`。
- 如果新增的是全新的首页分组，通常还要同步更新 `lib/navigation/modules/index.dart` 和 `lib/navigation/registry/route_registry.dart`。

## 编码约定

- 严格遵守 `analysis_options.yaml`：只用 package import、显式返回类型、强类型、不要使用宽松 raw type。
- 优先使用 `const`、`final` 和职责单一的小组件。
- 现有代码和注释中英文混用；修改时尽量保持周边文件的语言风格一致。
- 新示例应优先放在现有分类目录下，例如 `examples`、`network`、`storage`、`anim`、`packages`、`state_manager`，不要随意在 `lib/` 顶层新增无关文件。
- 不要使用 `print`；如需日志，优先使用 `lib/core/utils/logger/` 下的工具。
- 不要编辑生成目录或临时目录，例如 `.dart_tool/`、`build/`。

## 依赖与架构说明

- 这个项目是故意同时保留多种状态管理库：`provider`、`getx`、`bloc`、`riverpod`。
- 项目同时保留独立的 GetX 完整示例应用和通用路由示例；修改 GetX 相关代码时，注意区分 `lib/features/examples/getx/` 和 `lib/features/state_manager/getx/`。

## 测试与验证

- 当前自动化测试较少，现有测试文件主要是 `test/widget_test.dart`。
- 修改代码后，优先运行：
  - `fvm flutter analyze`
  - `fvm flutter test`
- 如果本机没有 `fvm`，再退回使用 `flutter analyze` 和 `flutter test`。
- 如果改动了路由，至少验证：首页分组入口、分组列表入口、最终页面跳转，这三层都正常。

## 工具与技能

- 项目在 `.fvmrc` 中固定 Flutter 版本为 `3.41.6`，命令优先使用 `fvm flutter ...`。
- 项目自定义了 `flutter-example` skill，用来生成统一的 `XxxExample -> XxxRoute` 页面骨架。
