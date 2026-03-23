# SKILLS

这个文件用于说明 `basic_flutter` 项目里可直接使用的 Skills，以及每个 Skill 更适合解决什么问题。

## 使用说明

- 这是一个 Flutter 示例 + 学习项目，入口在 `lib/main.dart`。
- 路由使用 `GoRouter`，核心配置在 `lib/navigation/app_router.dart`。
- 新示例优先放进已有分类目录，不要随意在 `lib/` 顶层新增无关文件。
- 修改代码后，优先运行 `fvm flutter analyze` 和 `fvm flutter test`。
- 项目固定 Flutter 版本为 `3.41.0`，命令优先使用 `fvm flutter ...`。

## 项目自定义 Skill

### `flutter-example`

位置：`.agents/skills/flutter-example/SKILL.md`

适用场景：

- 为 `basic_flutter` 新增统一风格的示例页面
- 生成 `XxxExample -> XxxRoute` 页面骨架
- 在复杂示例里扩展为 `XxxExample -> XxxPage`
- 接入现有导航分组与 `RouteItem`

这个 Skill 最适合在你要新增一个新的 Flutter 示例页面、包示例页面、状态管理示例页面时使用。

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

优先使用 `flutter-example`，并遵守当前项目约定：

- 基础示例放到 `lib/features/examples/`
- 三方包或平台能力示例放到 `lib/features/packages/`
- 状态管理示例放到 `lib/features/state_management/`
- 新增路由时同步更新 `lib/navigation/modules/` 下对应模块文件

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

