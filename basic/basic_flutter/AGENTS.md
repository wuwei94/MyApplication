# basic_flutter

## 项目定位

- 这是一个 Flutter Demo Catalog 项目，目标是浏览、学习和验证各类 Flutter 示例，而不是业务 App。
- 项目同时也是一个 Flutter module，用于 add-to-app；除非用户明确要求，否则不要修改 `pubspec.yaml` 里的 `flutter.module` 标识。
- 应用入口是 `lib/main.dart`。
- App 壳层位于 `lib/app/`，Demo 内容位于 `lib/demos/`。

## 当前架构

- `lib/app/app.dart`：应用根组件。
- `lib/app/home/app_home.dart`：首页。
- `lib/app/navigation/app_navigator.dart`：统一导航入口。
- `lib/app/router/`：路由配置和路由类型切换。
- `lib/catalog/`：catalog 模型、目录页、根目录聚合、路径解析和路由工厂。
- `lib/demos/*/catalog.dart`：顶层 Demo 分组清单。
- `lib/demos/layout/*/catalog.dart`：layout 子分组清单。

当前项目采用 `Demo Catalog First` 结构：

- `app` 只负责壳层和路由入口。
- `catalog` 负责目录模型、解析、目录页和根目录聚合。
- `demos` 只负责示例内容和分组清单。
- 新增目录清单文件统一使用 `catalog.dart`。

## 目录结构

- `lib/app/router/app_router_config.dart`：应用主路由配置。
- `lib/app/router/app_router_type.dart`：`GoRouter` / `AutoRoute` 切换开关。
- `lib/catalog/models/catalog_entry.dart`：目录树节点模型 `CatalogEntry`。
- `lib/catalog/models/resolved_catalog_entry.dart`：运行时绝对路径目录树节点模型 `ResolvedCatalogEntry`。
- `lib/catalog/models/catalog_section.dart`：顶层目录分组抽象 `CatalogSection`。
- `lib/catalog/services/catalog_tree_resolver.dart`：将相对路径目录树解析为运行时绝对路径目录树。
- `lib/catalog/registry/catalog_registry.dart`：首页顶层目录注册表聚合。
- `lib/catalog/pages/catalog_page.dart`：通用目录页，用于展示分组或示例列表。
- `lib/catalog/routing/catalog_route_factory.dart`：将 `CatalogEntry` 转成路由定义。

- `lib/demos/basics/`：基础示例和完整小型示例应用，例如 `counter`、`getx_app`。
- `lib/demos/network/`：网络请求示例，例如 `dio`、`http`、图片加载。
- `lib/demos/storage/`：本地存储示例，例如 `shared_preferences`、`secure_storage`、`hive`。
- `lib/demos/animation/`：动画资源和播放示例，例如 `lottie`、`svg`、`svga`、`pag`。
- `lib/demos/packages/`：三方包示例，例如 `notification`、`permission`、`image_picker`、`wechat_picker`、`webview`、`flutter_screenutil`、`toast`。
- `lib/demos/video/`：视频播放示例，例如 `video_player`、`chewie`。
- `lib/demos/state_management/`：`provider`、`bloc`、`riverpod` 等状态管理示例。
- `lib/demos/layout/`：布局和交互类示例；子目录各自拥有 `catalog.dart`。
- `lib/demos/showcase/`：杂项展示类示例，例如本地字体。

- 普通叶子 demo 优先直接放在分组目录或子分组目录下，例如 `lib/demos/network/dio_example.dart`、`lib/demos/layout/containers/align_example.dart`。
- 如果某个分类已经有稳定的子目录结构，就沿用现有组织方式，例如 `lib/demos/basics/counter/counter_example.dart`、`lib/demos/state_management/provider/provider_example.dart`。
- 复杂 demo 可在对应目录下继续拆 `pages/`、`controllers/`、`providers/`、`cubits/`、`bindings/` 等子目录。

- `lib/core/`：通用工具，例如网络、日志、存储、UI 辅助工具。
- `lib/l10n/`：字符串和国际化相关辅助代码。
- `lib/boost/`：add-to-app/Boost 相关桥接代码。
- `tools/`：项目辅助脚本。
- `assets/` 和 `images/` 已在 `pubspec.yaml` 中声明。

## Catalog 约定

- 每个顶层 Demo 分组使用一个 `catalog.dart`。
- 如果某个分组内容较多，可以像 `layout` 一样继续在子目录下拆 `catalog.dart`。
- `CatalogEntry.path` 统一使用相对路径。
- 顶层分组相对根目录，例如 `basics`、`layout`、`network`。
- 子分组和具体示例页相对父级目录，例如 `containers`、`dio`、`shared-preferences`。
- 运行时绝对路径由 `lib/catalog/services/catalog_tree_resolver.dart` 统一解析。
- `CatalogEntry` 是纯数据模型，不要把导航行为塞回模型里。
- 顶层目录统一由 `lib/catalog/registry/catalog_registry.dart` 聚合。
- 叶子页面统一通过 `CatalogEntry.page(...)` 接入。
- 子分组统一通过 `CatalogEntry.catalog(...)` 接入。
- 新增顶层分组时，优先只改：
  - 对应分组下的 `catalog.dart`
  - `lib/catalog/registry/catalog_registry.dart`

## 路由模式

- 首页路由是 `/home`，对应 `AppHome`。
- 首页展示顶层 Demo 分组。
- 点击分组后进入 `CatalogPage`。
- 点击最终示例项后进入具体示例页面。
- 当前项目必须同时支持 `GoRouter` 和 `AutoRoute`，通过 `AppRouterType` 切换。
- 不要删除 `AppRouterType` 这层切换，除非用户明确要求。
- 新增示例时，默认不需要手写额外路由文件，只需要维护 `catalog.dart`。

## 编码约定

- 严格遵守 `analysis_options.yaml`：只用 package import、显式返回类型、强类型、不要使用宽松 raw type。
- 优先使用 `const`、`final` 和职责单一的小组件。
- 现有代码和注释中英文混用；修改时尽量保持周边文件的语言风格一致。
- 新示例应优先放在现有 `lib/demos/` 分类目录下，不要随意在 `lib/` 顶层新增无关文件。
- 不要使用 `print`；如需日志，优先使用 `lib/core/utils/logger/` 下的工具。
- 不要编辑生成目录或临时目录，例如 `.dart_tool/`、`build/`。

## 依赖与示例说明

- 这个项目当前保留多种状态管理相关示例：`provider`、`bloc`、`riverpod`，以及位于 `lib/demos/basics/getx_app/` 的 GetX 完整示例应用。
- GetX 完整示例应用位于 `lib/demos/basics/getx_app/`，它有自己的内部导航结构。

## 测试与验证

- 当前自动化测试较少，现有测试文件主要是 `test/widget_test.dart`。
- 修改代码后，优先运行：
  - `fvm flutter analyze`
  - `fvm flutter test`
- 如果本机没有 `fvm`，再退回使用 `flutter analyze` 和 `flutter test`。
- 如果改动了目录或路由，至少验证：
  - 首页顶层分组入口正常
  - 分组目录页入口正常
  - 最终示例页面跳转正常

## 工具与技能

- 项目在 `.fvmrc` 中固定 Flutter 版本为 `3.41.6`，命令优先使用 `fvm flutter ...`。
- 项目自定义了 `flutter-example` skill。
- 新增示例时，应遵循当前 Demo Catalog 架构。
