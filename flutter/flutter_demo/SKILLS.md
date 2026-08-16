# SKILLS

这个文件用于说明 `flutter_demo` 项目里可直接使用的 Skills，以及每个 Skill 更适合解决什么问题。

## 使用说明

- 这是一个 Flutter 示例 + 学习项目，入口在 `lib/main.dart`。
- 路由支持 `GoRouter` 与 `AutoRoute`，核心配置在 `lib/app/router/`。
- 新示例优先放进已有分类目录，不要随意在 `lib/` 顶层新增无关文件。
- 修改代码后，优先运行 `fvm flutter analyze` 和 `fvm flutter test`。
- 项目固定 Flutter 版本为 `3.47.0`（见 `.fvmrc`），命令优先使用 `fvm flutter ...`。
- 若 `fvm flutter ...` 在受限 agent 环境中无法启动子进程，可直连 SDK：`%USERPROFILE%\fvm\versions\3.47.0\bin\flutter.bat analyze`（`test` 同理）。

## 项目自定义 Skill

### `flutter-demo-page`

位置：`.agents/skills/flutter-demo-page/SKILL.md`

适用场景：

- 为 `flutter_demo` 新增统一风格的示例页面
- 生成 `XxxDemoPage -> XxxDemoView` 页面骨架
- 在复杂示例里扩展为 `xxx_demo.dart + pages/` 等子目录
- 通过 `CatalogEntry` 接入现有导航分组

这个 Skill 最适合在你要新增一个新的 Flutter 示例页面、包示例页面、状态管理示例页面时使用。
它只负责页面骨架与目录接入，动画、状态、网络等具体技术实现请使用下方对应的官方 Skill。

## 常用 Flutter Skills

以下 Skills 已在当前工作区可用，适合按主题调用：

### 页面与交互

- `flutter-building-layouts`：搭建页面结构、布局与响应式 UI
- `flutter-building-forms`：表单、校验、输入交互
- `flutter-animating-apps`：动效、过渡、动画反馈
- `flutter-theming-apps`：主题、颜色、排版、视觉风格
- `flutter-improving-accessibility`：无障碍与语义优化
- `flutter-localizing-apps`：国际化、多语言、地区适配

### 架构与状态

- `flutter-architecting-apps`：按分层结构组织 Flutter 项目
- `flutter-managing-state`：共享状态、局部状态、状态流转
- `flutter-handling-concurrency`：耗时任务、后台 isolate、避免卡顿
- `flutter-caching-data`：缓存策略、离线读取、性能优化
- `flutter-working-with-databases`：本地数据库与持久化数据

### 网络与平台能力

- `flutter-handling-http-and-json`：HTTP 请求、JSON 序列化、接口集成
- `flutter-interoperating-with-native-apis`：原生能力互操作
- `flutter-embedding-native-views`：嵌入原生 View，如地图或 WebView
- `flutter-building-plugins`：编写 Flutter 插件
- `flutter-adding-home-screen-widgets`：桌面或手机主屏 Widget

### 工程与质量

- `flutter-implementing-navigation-and-routing`：路由设计、跳转、深链
- `flutter-testing-apps`：单测、组件测试、集成测试
- `flutter-reducing-app-size`：包体积分析与优化
- `flutter-setting-up-on-macos`：macOS Flutter 环境配置
- `flutter-setting-up-on-linux`：Linux Flutter 环境配置
- `flutter-setting-up-on-windows`：Windows Flutter 环境配置

## 在本项目里的推荐用法

### 新增示例页面

优先使用 `flutter-demo-page`，并遵守当前项目约定：

- 基础示例放到 `lib/demos/basics/`
- 三方包或平台能力示例放到 `lib/demos/packages/`
- 状态管理示例放到 `lib/demos/state_management/`
- 新增页面时同步更新对应分组下的 `catalog.dart`；新增首页一级分组时再同步 `catalog_registry.dart` 与 `app_router_config.dart`

### 处理具体开发任务

- 做布局：用 `flutter-building-layouts`
- 做表单：用 `flutter-building-forms`
- 做状态：用 `flutter-managing-state`
- 做路由：用 `flutter-implementing-navigation-and-routing`
- 做接口：用 `flutter-handling-http-and-json`
- 做测试：用 `flutter-testing-apps`

## 约束提醒

- 不要修改 `pubspec.yaml` 中的 `flutter.module` 标识，除非需求明确要求
- 只用 package import，保持强类型和显式返回类型
- 优先使用 `const`、`final` 和小而清晰的组件
- 不要使用 `print`，如需日志，优先复用 `lib/core/utils/logger/`
- 不要编辑生成目录或临时目录，例如 `.dart_tool/`、`build/`

