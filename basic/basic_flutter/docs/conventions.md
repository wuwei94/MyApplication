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

## 编码规范

- 遵守 `analysis_options.yaml` 中的约束：只用 package import、显式返回类型、强类型
- 优先使用 `const`、`final` 和职责单一的小组件
- 不要使用 `print`；如需日志，优先使用 `lib/core/utils/logger/` 下的工具
- 不要编辑生成目录或临时目录，如 `.dart_tool/`、`build/`
- 现有代码和注释中英文混用，新增内容尽量保持周边风格一致

## 文档同步

修改代码时，必须同步更新以下文档：

| 修改内容 | 需更新的文档 |
|---------|------------|
| 新增/删除示例 | `docs/demos.md` + `README.md` |
| 新增/删除依赖 | `docs/dependencies.md` + `README.md` |
| 修改开发约定 | `docs/conventions.md` |
| 修改架构或技术栈 | `README.md` |

## 快速查找

- **新增示例**：在对应 `lib/demos/*/catalog.dart` 中添加 `CatalogEntry.page(...)`，叶子页面用 `xxx_example.dart` 命名
- **新增顶层分组**：创建 `lib/demos/<分组>/catalog.dart`，在 `lib/catalog/registry/catalog_registry.dart` 注册
- **路由配置**：`lib/app/router/app_router_type.dart`
- **目录注册表**：`lib/catalog/registry/catalog_registry.dart`
- **入口文件**：`lib/main.dart`

## 环境要求

- Flutter：项目通过 `.fvmrc` 固定为 `3.41.7`
- Dart SDK：`^3.10.8`
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

- **Android**：`dart tools/apply_android_fixes.dart` — 自动补齐权限、Java 8+ API 兼容、compileSdk
- **iOS**：`dart tools/apply_ios_fixes.dart` — 自动补齐 geolocator 定位权限
