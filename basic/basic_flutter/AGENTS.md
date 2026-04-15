# basic_flutter

## 项目定位

- 这是一个 Flutter Demo Catalog 项目，目标是浏览、学习和验证各类 Flutter 示例，而不是业务 App。
- 项目同时也是一个 Flutter module，用于 add-to-app；除非用户明确要求，否则不要修改 `pubspec.yaml` 里的 `flutter.module` 标识。
- 应用入口是 `lib/main.dart`。
- App 壳层位于 `lib/app/`，Demo 内容位于 `lib/demos/`。

## 当前架构

- `lib/app/app.dart`：应用根组件。
- `lib/app/home/home_page.dart`：首页。
- `lib/app/catalog/`：目录树模型、目录页和顶层目录聚合。
- `lib/app/router/`：路由器、路由类型切换、导航工具、目录路由转换器。
- `lib/demos/*/catalog.dart`：顶层 Demo 分组清单。
- `lib/demos/layout/*/catalog.dart`：layout 子分组清单。

当前项目采用 `Demo Catalog First` 结构：

- `app` 只负责壳层、目录、路由。
- `demos` 只负责示例内容和分组清单。
- 新增目录清单文件统一使用 `catalog.dart`。

## 目录结构

- `lib/app/catalog/catalog_item.dart`：目录树节点模型 `CatalogItem`。
- `lib/app/catalog/catalog_section.dart`：顶层目录分组抽象 `CatalogSection`。
- `lib/app/catalog/demo_catalog.dart`：首页顶层目录聚合。
- `lib/app/catalog/demo_catalog_page.dart`：通用目录页，用于展示分组或示例列表。
- `lib/app/router/app_router.dart`：应用主路由配置。
- `lib/app/router/app_router_type.dart`：`GoRouter` / `AutoRoute` 切换开关。
- `lib/app/router/app_navigator.dart`：统一导航入口。
- `lib/app/router/catalog_route_converter.dart`：将 `CatalogItem` 转成路由定义。

- `lib/demos/examples/`：基础示例和独立实验区，例如 counter、singleton、GetX 示例应用。
- `lib/demos/network/`：网络请求示例，例如 `dio`、`http`、图片加载。
- `lib/demos/storage/`：本地存储示例，例如 `shared_preferences`、`secure_storage`、`hive`。
- `lib/demos/anim/`：动画资源和播放示例，例如 `lottie`、`svg`、`svga`、`pag`。
- `lib/demos/packages/`：三方包示例，例如 `notification`、`permission`、`image_picker`、`wechat_picker`、`webview`、`flutter_screenutil`、`toast`。
- `lib/demos/state_manager/`：`provider`、`bloc`、`riverpod` 等状态管理示例。
- `lib/demos/layout/`：布局和交互类示例；子目录各自拥有 `catalog.dart`。
- `lib/demos/demo/`：杂项展示类示例，例如本地字体。

- `lib/core/`：通用工具，例如网络、日志、存储、UI 辅助工具。
- `lib/data/`：基础 model、datasource 抽象、repository 抽象。
- `lib/l10n/`：字符串和国际化相关辅助代码。
- `lib/boost/`：add-to-app/Boost 相关桥接代码。
- `tool/`：项目辅助脚本。
- `assets/` 和 `images/` 已在 `pubspec.yaml` 中声明。

## Catalog 约定

- 每个顶层 Demo 分组使用一个 `catalog.dart`。
- 如果某个分组内容较多，可以像 `layout` 一样继续在子目录下拆 `catalog.dart`。
- `CatalogItem.path` 统一使用相对路径。
- 顶层分组相对根目录，例如 `examples`、`layout`、`network`。
- 子分组和具体示例页相对父级目录，例如 `containers`、`container`、`dio`。
- 运行时绝对路径由 `lib/app/router/catalog_route_converter.dart` 统一解析。
- `CatalogItem` 是纯数据模型，不要把导航行为塞回模型里。
- 顶层目录统一由 `lib/app/catalog/demo_catalog.dart` 聚合。
- 新增顶层分组时，优先只改：
  - 对应分组下的 `catalog.dart`
  - `lib/app/catalog/demo_catalog.dart`

## 路由模式

- 首页路由是 `/home`，对应 `HomePage`。
- 首页展示顶层 Demo 分组。
- 点击分组后进入 `DemoCatalogPage`。
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

- 这个项目是故意同时保留多种状态管理库：`provider`、`getx`、`bloc`、`riverpod`。
- GetX 完整示例应用位于 `lib/demos/examples/getx/`，它有自己的内部导航结构。
- 修改 GetX 相关代码时，注意区分：
  - `lib/demos/examples/getx/`
  - `lib/demos/state_manager/`

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
