# 开发约定

> 所有代码和文档必须遵守的规则。

## Catalog 设计约定

- 每个顶层分组使用一个 `catalog.dart` 描述目录结构
- 如果分组内容较多，可以像 `layout/` 一样继续拆分子目录和子级 `catalog.dart`
- `CatalogEntry.path` 必须使用相对路径
- 顶层目录相对根路径，如 `basics`、`layout`、`network`
- 子分组和叶子页面相对父目录路径，如 `containers`、`dio`、`shared-preferences`
- 运行时绝对路径统一由 `lib/catalog/services/catalog_tree_resolver.dart` 解析
- 新增顶层分组时，通常只需要修改对应分组下的 `catalog.dart`，并把它加入 `lib/catalog/registry/catalog_registry.dart`

## 路由模式

- 项目同时支持 `GoRouter` 和 `AutoRoute`
- 路由切换位置：`lib/app/router/app_router_type.dart`
- 当前默认值：`AppRouterType.autoRoute`
- 首页固定路由：`/`
- 新增普通 Demo 时，默认不需要手写额外路由文件，只需要维护对应 `catalog.dart`

## 编码与风格规范

- **语法约束与强类型**：遵守 `analysis_options.yaml` 中的约束，使用 package import、显式返回类型与强类型。
- **不可变性优先**：无状态 Widget 及常量对象一律使用 `const` 构造函数；声明不可变变量使用 `final`，禁止无明确理由的 `var`。
- **空安全与解包**：严格遵循 Sound Null Safety，禁止滥用 `!` 强行解包非空，优先使用 `?.`、`??`、空判断或模式匹配（Pattern Matching）。
- **组件单一职责**：避免在单个 `build` 方法中堆砌复杂视图；过长布局应拆分为职责单一的小 Widget（或 private Widget），提升可读性。
- **参数与命名可读性**：命名参数清晰，避免魔法数字；变量与方法名自解释。
- **严禁 `print` 调试**：不要使用 `print()`；如需日志，统一使用 `lib/core/utils/logger/` 下的工具或 `debugPrint`。
- **示例纯粹性原则**：Demo 页面优先直接展示目标库/功能的核心 API，不为了写示例而封装与演示无关的复杂业务状态编排器。
- **禁止使用已废弃 API**：严禁使用 Flutter 已废弃的组件（如 `RaisedButton`/`FlatButton`）与废弃属性，必须使用现代替代项（如 `ElevatedButton`）。
- **敏感信息防护**：严禁在前端代码中硬编码真实 API Key / Secret 或敏感 Token，网络请求示例一律使用占位符、配置或 Mock 数据。
- **目录隔离**：不要编辑生成目录或临时目录，如 `.dart_tool/`、`build/`。
- **语言风格**：现有代码和注释中英文混用，新增内容尽量保持周边风格一致。

## Agent 协作与工作流守则

- **交付静态检查**：每次完成代码新增或修改后，交付前必须运行 `fvm flutter analyze`，确保 **0 warning, 0 error**。
- **根因分析排错**：遇到报错时，必须先完整阅读错误堆栈和上下文，查明根本原因再做针对性修复，并在修改后说明“改动位置”与“修复原因”，禁止盲目试错。
- **最小化变更**：遵循渐进式改动原则，单次聚焦一个功能模块或页面，避免大面积重构无关文件。

## 文档同步

修改代码时，必须同步更新以下文档：

| 修改内容 | 需更新的文档 |
|---------|------------|
| 新增/删除示例 | `docs/demos.md` + `README.md` |
| 新增/删除依赖 | `docs/dependencies.md` + `README.md` |
| 修改开发约定 | `docs/conventions.md` |
| 修改架构或技术栈 | `README.md` |

## 快速查找

- **新增示例**：在对应 `lib/demos/*/catalog.dart` 中添加 `CatalogEntry.page(...)`，叶子页面用 `xxx_demo.dart` 命名
- **新增顶层分组**：创建 `lib/demos/<分组>/catalog.dart`，在 `lib/catalog/registry/catalog_registry.dart` 注册
- **路由配置**：`lib/app/router/app_router_type.dart`
- **目录注册表**：`lib/catalog/registry/catalog_registry.dart`
- **入口文件**：`lib/main.dart`

## 环境要求

- Flutter：项目通过 `.fvmrc` 固定为 `3.47.0`
- Dart SDK：`3.13.0`（随 Flutter SDK 提供）
- 推荐使用 `fvm`

## 常用命令

```bash
# 安装依赖
fvm flutter pub get

# 运行项目
fvm flutter run

# 静态检查
fvm flutter analyze

# 运行测试
fvm flutter test

# 代码格式化
dart format lib test tools
```

## Android/iOS 修复脚本

- **Android**：`dart tools/apply_android_fixes.dart` — 自动补齐权限、Java 8+ API 兼容、compileSdk、相机插件依赖、TFLite Kotlin JVM 目标对齐
- **iOS**：`dart tools/apply_ios_fixes.dart` — 自动补齐 geolocator 定位权限（Info.plist 用途说明 + Podfile 编译宏）
