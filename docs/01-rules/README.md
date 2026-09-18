# 项目规范（01-rules）

> **按读者任务查文档，不要按历史文件名猜。**
> 运行时硬契约（项目身份、绝对禁令、技术不变量摘要、交付门禁）见仓库根 [AGENTS.md](../../AGENTS.md)。

## 任务判型路由 / 我想……

| 判型 / 我想…… | 去读 | 一句话 | 锚点 / 模板 |
|---|---|---|---|
| 知道交付时能改哪些文件、标题怎么写 | [delivery.md](delivery.md) | 禁止附赠、禁止复盘式交付、范围自检 | 根 [AGENTS.md](../../AGENTS.md) §4 |
| 新写/改 Showcase 示例页、平行多库横评 | [showcase.md](showcase.md) | 页型、类头、日志、平行实现与平行注释 | `modules/module_http/.../OkHttpActivity.kt`；`templates/ShowcaseActivity.kt.stub` |
| 新增/移动模块、Activity、路由、分类归属 | [structure.md](structure.md) + [../05-catalog/modules.md](../05-catalog/modules.md) | 分层依赖、包名/资源前缀、源码分包（兄弟横评=能力包；控件集合=activity/）、路由、双初始化、文档同步 | `modules/module_storage`；`modules/module_widget_custom`；`basic/basic_shared/.../RouterPath.kt` |
| 定命名、库类/成员注释等通用代码风格 | [style.md](style.md) | 禁止 m 前缀、Why>What、KDoc | `libs/lib_okhttp/.../InterceptorLogging.kt` |
| 改 Gradle/Convention/CI/钩子/混淆/Flutter/XML 注释 | [engineering.md](engineering.md) + [../02-engineering/](../02-engineering/engineering.md) | 工程化落点注释与文件头 | `build-logic/`；`tools/pre-push` |
| 抄现成模板（Showcase/UDF/MVI/Compose/测试/废弃页） | [templates.md](templates.md) + 仓内 `templates/` | 模板骨架；与规则冲突时以规则为准 | `ShowcaseActivity.kt.stub`、`MviViewModel.kt.stub`、`ComposeScreen.kt.stub`、`DeprecatedShowcaseActivity.kt.stub` |
| 查 UI 尺寸、字体、图标规范 | [design.md](design.md) + `basic_shared` `dimens.xml` | 4dp 网格与设计体系 | `shared_dp_*` |
| 写 Commit / 装钩子 / 确认提交粒度 | [git.md](git.md) | Conventional Commits、提交粒度与门禁 | `tools/commit-msg`；`tools/pre-push` |
| 写架构演进页（MVP/MVVM/MVI/Mavericks） | [showcase.md](showcase.md) §1.4 + [../03-architecture/architecture.md](../03-architecture/architecture.md) | SSOT/UDF 载体与模式对比 | `modules/module_arch`；`templates/MviViewModel.kt.stub` |
| 写 Compose UI 原语页 | [showcase.md](showcase.md) §1.3 + [templates.md](templates.md) Compose 节 | Stateful/Stateless 分离、单页自包含 | `modules/module_compose`；`templates/ComposeScreen.kt.stub` |
| 改 Flutter Demo 侧 | [../../flutter/flutter_demo/AGENTS.md](../../flutter/flutter_demo/AGENTS.md) | 子仓不变量与静态检查 | `fvm flutter analyze` |
| 查模块/库全景与 Activity 清单 | [../05-catalog/modules.md](../05-catalog/modules.md)、[../05-catalog/libs.md](../05-catalog/libs.md) | 检索型索引 | — |

### 阅读纪律（Agent）

- 根 [AGENTS.md](../../AGENTS.md) 恒读：项目身份先验、绝对禁令、技术不变量摘要、交付门禁命令。
- 按上表**只读命中判型**的文档与锚点；禁止无方向通读 `docs/`。
- 文档与代码冲突时：**行为以代码为准，契约意图以文档为准**；发现漂移先报告，不擅自扩大 diff 去「顺手对齐」。

## 分层

```
AGENTS.md (L0)  运行时硬契约（身份 / 禁令 / 不变量摘要 / 门禁），冲突时最高优先
    ↓
docs/01-rules/  按任务展开的 L1 规范 + 本文件判型路由（本目录）
    ↓
docs/02-engineering/、docs/03-architecture/、docs/04-domains/  领域与工程化细节
```

## 冲突优先级

同一事项出现口径不一致时，按下列顺序取舍（高 → 低）：

1. 仓库根 [AGENTS.md](../../AGENTS.md)（L0 运行时硬契约）
2. [delivery.md](delivery.md) 的「禁止附赠」「禁止复盘式交付」
3. 任务对应真相源：示例页/平行 → [showcase.md](showcase.md)；命名与注释 → [style.md](style.md)；结构/路由/依赖 → [structure.md](structure.md)；Git → [git.md](git.md)；UI 尺寸/token → [design.md](design.md)
4. [engineering.md](engineering.md) 工程化落点（不得放宽 2–3 的禁止项）
5. [templates.md](templates.md) 模板（与规则冲突时以规则为准）
6. `docs/02-engineering/*`、`docs/03-architecture/*`、`docs/04-domains/*` 领域细节

## 维护约定

- **一个文件只服务一个读者任务**；禁止把「路由 + 交付 + 注释 + Git」堆进同一文件。
- **示例页与平行注释的真相源是 showcase.md**；通用代码注释真相源是 style.md；命名表以 style.md 为准，structure.md 不重复维护。
- **任务判型路由的真相源是本文件**；根 AGENTS.md 只保留身份、禁令、不变量摘要与门禁命令，不在此重复长表。
- 旧文件名（`STYLE.md` / `conventions.md` / `scope.md` / `comments.md` / `agent-style-prompt.md`）已收敛，勿再扩展；外链请改指本目录新文件。
- **Windows 不区分大小写**：删除/重命名规范文件时注意 `STYLE.md` 与 `style.md` 会互相覆盖，改完用目录列表核对。
- **规约变更**：升级本目录或 `AGENTS.md` 时，同步核对本文件路由表锚点、AGENTS.md §4 门禁命令、structure「文档同步」表与完成定义；数量类描述（模块个数、提交统计）避免写死，或标明统计时点。
