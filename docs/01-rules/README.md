# 项目规范（01-rules）

> **按读者任务查文档，不要按历史文件名猜。**
> 运行时硬契约（违反即失败）见仓库根 [AGENTS.md](../../AGENTS.md)。

## 我想……

| 我想…… | 去读 | 一句话 |
|---|---|---|
| 知道交付时能改哪些文件、标题怎么写 | [delivery.md](delivery.md) | 禁止附赠、禁止复盘式交付、范围自检 |
| 新增/移动模块、Activity、路由、分类归属 | [structure.md](structure.md) | 分层依赖、包名/资源前缀、路由、分类、双初始化、文档同步 |
| 写/改示例页（Showcase）、平行多库横评 | [showcase.md](showcase.md) | 页型、类头、日志、平行实现与平行注释 |
| 定命名、库类/成员注释等通用代码风格 | [style.md](style.md) | 禁止 m 前缀、Why>What、KDoc |
| 改 Gradle/CI/钩子/混淆/Flutter/XML 注释 | [engineering.md](engineering.md) | 工程化落点注释与文件头 |
| 抄现成模板（UDF/Compose/测试/Fake） | [templates.md](templates.md) | 代码模板库 |
| 查 UI 尺寸、字体、图标规范 | [design.md](design.md) | 4dp 网格与设计体系 |
| 写 Commit / 装钩子 | [git.md](git.md) | Conventional Commits、提交粒度与门禁 |

## 分层

```
AGENTS.md (L0)  运行时硬契约，极短，冲突时最高优先
    ↓
docs/01-rules/  按任务展开的 L1 规范（本目录）
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
- 旧文件名（`STYLE.md` / `conventions.md` / `scope.md` / `comments.md` / `agent-style-prompt.md`）已收敛，勿再扩展；外链请改指本目录新文件。
- **Windows 不区分大小写**：删除/重命名规范文件时注意 `STYLE.md` 与 `style.md` 会互相覆盖，改完用目录列表核对。
- **规约变更**：升级本目录或 `AGENTS.md` 时，同步核对锚点文件路径、完成定义与「文档同步」表；数量类描述（模块个数、提交统计）避免写死，或标明统计时点。
