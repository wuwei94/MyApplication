# basic_flutter

> 本文件是 Agent 知识的目录索引，而非百科全书。

## 项目定位

Flutter Demo Catalog，不是业务 App。同时是 Flutter module，可用于 add-to-app 集成。

**入口**：`lib/main.dart`

## 架构

```
lib/
├── app/          # 壳层（首页、导航、路由配置）
├── catalog/      # 目录系统（模型、解析、注册表、路由工厂）
├── demos/        # 示例内容（10 个分组，93 个叶子页面）
├── core/         # 核心工具（网络、日志、存储、UI 辅助）
├── l10n/         # 多语言
└── boost/        # add-to-app 桥接
```

## 文档目录

| 文档 | 内容 |
|------|------|
| docs/demos.md | 示例分组详情（10 个分组，93 个叶子页面） |
| docs/dependencies.md | 依赖清单（按分类列出所有三方包） |
| docs/conventions.md | 开发约定（Catalog 设计、路由模式、编码规范） |
| SKILLS.md | Skills 参考（23 个 Flutter 相关 Skill） |

## Skills

- Skill 文件存放位置：`.agents/skills/`
- 项目自定义 Skill：`flutter-example`
- 完整 Skill 列表见 `SKILLS.md`

## 不变量

1. 新增示例必须在对应 `catalog.dart` 中注册
2. 叶子页面统一使用 `xxx_example.dart` 命名
3. 必须同时支持 GoRouter 和 AutoRoute
4. 遵守 `analysis_options.yaml` 编码规范
5. 修改代码时必须同步更新文档，详见 `docs/conventions.md`
