# basic_flutter

- 这是 Flutter Demo Catalog，不是业务 App；入口 `lib/main.dart`。项目同时是 Flutter module，除非用户明确要求，不改 `pubspec.yaml` 中 `flutter.module`。
- 架构：`lib/app/` 管壳层、首页、导航、路由；`lib/catalog/` 管目录模型、解析、目录页、注册表、路由工厂；`lib/demos/` 管示例与分组，新增清单统一用 `catalog.dart`。
- Catalog：顶层分组在 `lib/demos/*/catalog.dart`，由 `lib/catalog/registry/catalog_registry.dart` 聚合。`CatalogEntry.path` 只写相对路径，绝对路径由 `catalog_tree_resolver.dart` 解析；叶子页用 `CatalogEntry.page(...)`，子分组用 `CatalogEntry.catalog(...)`。新增顶层分组时，优先只改对应 `catalog.dart` 与 `catalog_registry.dart`。
- 路由：首页是 `/`，首页展示顶层分组，分组进入 `CatalogPage`，叶子项进入示例页。必须同时支持 `GoRouter` 和 `AutoRoute`，通过 `AppRouterType` 切换；除非用户明确要求，不删除这层切换。
- 编码：遵守 `analysis_options.yaml`，只用 package import、显式返回类型、强类型；优先 `const`、`final` 和小组件，保持周边中英文风格。新示例优先放现有 `lib/demos/` 分类，不随意在 `lib/` 顶层加无关文件；不用 `print`，日志优先 `lib/core/utils/logger/`；不改 `.dart_tool/`、`build/`。
- 验证：按 `.fvmrc` 优先用 `fvm flutter analyze`、`fvm flutter test`；若无 `fvm` 再用 `flutter analyze/test`。若改动目录或路由，至少验证首页入口、分组目录页入口、最终示例页跳转正常。
