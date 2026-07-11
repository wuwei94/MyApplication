# basic_flutter — Flutter Demo Catalog

一个面向学习、验证和沉淀示例的 Flutter Demo Catalog 项目。

它不是面向业务场景的完整 App，而是一个可以持续扩展的示例目录：通过首页浏览不同 Demo 分组，进入目录页查看具体示例，再进入最终页面验证组件、框架或能力的使用方式。同时，这个仓库也是一个 Flutter module，可用于 add-to-app 集成。

## 项目特点

- **Demo Catalog First 架构**：`app` 负责壳层和路由，`catalog` 负责目录树和解析，`demos` 负责示例内容
- **双路由支持**：同时支持 `GoRouter` 和 `AutoRoute`，通过常量切换路由实现
- **10 个顶层分组**：basics / layout / state_management / network / storage / image / animation / video / packages / showcase
- **93 个叶子示例页面**：覆盖 Flutter 开发的绝大部分常见场景
- **add-to-app 集成**：保留 Flutter module 能力，可用于原生宿主 App 的集成

## 目录结构

```
lib/
├── main.dart                          # 应用入口
├── app/                               # App 壳层、首页、导航与路由配置
├── catalog/                           # Catalog 模型、目录页、注册表、路由工厂、路径解析
├── demos/                             # 所有示例内容（10 个分组）
├── core/                              # 通用工具能力：网络、日志、存储、UI 辅助等
├── l10n/                              # 多语言与字符串资源
└── boost/                             # add-to-app / Boost 相关桥接代码
```

## 文档目录

| 文档 | 内容 |
|------|------|
| docs/demos.md | 示例分组详情（10 个分组，93 个叶子页面） |
| docs/dependencies.md | 依赖清单（按分类列出所有三方包） |
| docs/conventions.md | 开发约定（Catalog 设计、路由模式、编码规范） |

## 快速开始

### 环境要求

- Flutter：项目通过 `.fvmrc` 固定为 `3.41.7`
- Dart SDK：`^3.10.8`
- 推荐使用 `fvm`

### 安装依赖

```bash
fvm flutter pub get
```

### 运行项目

```bash
fvm flutter run
```

### 常用命令

```bash
fvm flutter analyze    # 静态检查
fvm flutter test       # 运行测试
dart format lib test tools  # 代码格式化
```

## 不变量

1. 新增示例必须在对应 `catalog.dart` 中注册
2. 叶子页面统一使用 `xxx_example.dart` 命名
3. 必须同时支持 GoRouter 和 AutoRoute
4. 遵守 `analysis_options.yaml` 编码规范
5. 修改代码时必须同步更新文档，详见 `docs/conventions.md`
