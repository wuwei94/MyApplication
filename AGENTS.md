# AGENTS.md — Agent 运行时硬性契约

> 本文件是所有 AI Agent 参与本项目开发时的最高优先级硬性执行契约。
> 按任务展开的规范入口与判型路由：[docs/01-rules/README.md](docs/01-rules/README.md)。架构全景见 [docs/README.md](docs/README.md)。
> Flutter Demo 子仓另有 [flutter/flutter_demo/AGENTS.md](flutter/flutter_demo/AGENTS.md)；改 Flutter 侧时以该文件 + Flutter 自身 `analysis_options` 为准。

## 0. 项目身份（判断先验）

本仓是 **Android 技术栈 Showcase 沉淀工程**，不是生产业务 App。

1. **多实现并存是产品目标**：同一能力的多库、多架构、双 Application 并列是教材矩阵，禁止合并去重或「选一个留下」。
2. **废弃 API 演示页是教材**：标注已废弃的示例页按 showcase 页型保留，禁止当死代码清理。
3. **平行组保持矩阵完整**：缺轴须有文档依据，否则补齐；禁止为省事砍兄弟页。

## 1. 绝对禁令（违反即判定失败）

1. **严格爆炸半径（禁止擅自附赠）**：仅修改任务明确指定的文件与逻辑。未点名的重构、依赖升级、额外测试或格式化一律不碰；实现若必须触及范围外文件，立即停下说明原因并申请授权。未获明确指令严禁 `git commit` / `git push`。
2. **马尔可夫单向终态（禁止复盘式交付）**：交付物只描述系统最终状态与客观技术原因；禁止「无 X 版 / 已去掉 X / 为何不加 X」等试错叙事（见 [docs/01-rules/delivery.md](docs/01-rules/delivery.md)）。
3. **禁止破坏 Showcase 不变量**：合并平行实现、清理废弃演示页、入口伪装可操作横评、示例页自造任务编排包装层。
4. **禁止破坏分层与根因底线**：`modules` 互依；`libs` 依赖 `modules`/`app`/`basic_*`；`basic_model` 引入 `android.*`；自有成员 `m` 前缀；magic number、`postDelayed` 瞎猜、空 catch；密钥/API Key 硬编码进源码。

## 2. 开工与技术不变量

- **开工协议**：先将任务收敛为允许触碰的文件白名单；按判型只读 [docs/01-rules/README.md](docs/01-rules/README.md) 路由指向的规范与锚点，禁止无方向通读 `docs/`。
- **分层单向依赖**：`modules` 互不依赖；`libs` 不依赖 `modules`/`app`/`basic_*`；`basic_model` 保持纯 JVM；数据栈 `basic_*` 可按需依赖更底层 `basic_*` 与 `libs`。依赖用 typesafe accessors（`projects.xxx`）（见 [docs/01-rules/structure.md](docs/01-rules/structure.md)）。
- **组件规范**：Activity 必须 `@Route(path = RouterPath.Xxx.Yyy)`；资源前缀与包名地图见 [docs/01-rules/structure.md](docs/01-rules/structure.md)；modules 模块依赖 `basic_lib` 与 `basic_shared`。
- **构建约束**：版本只进 `libs.versions.toml`；SDK/flavor/lint/spotless 只走 Convention Plugin；源码落 `src/main/java`；Android 侧本地验证默认 `-PenableFlutter=false`。
- **示例页架构**：一个 `buildList` 项 ↔ 一个示例方法；`when(position)` 直调库 API；`BasicResponseActivity` 须 `showDescription`（见 [docs/01-rules/showcase.md](docs/01-rules/showcase.md)）。
- **注释与日志**：中文；Why > What；保护既有技术决策注释；日志 `→ ` / `✓ ` / `✗ `，高频用 `updateLog`，禁止 `【成功】/【失败】` 结果装饰（见 [docs/01-rules/style.md](docs/01-rules/style.md)、[docs/01-rules/showcase.md](docs/01-rules/showcase.md)）。
- **协程与 UI 收集**：架构样板/可复用逻辑禁止业务类散落硬编码 `Dispatchers.*` 与 `GlobalScope`；UI 收集 Flow 用 `collectWithLifecycle` / `launchAndRepeatWithLifecycle`（见 [docs/01-rules/structure.md](docs/01-rules/structure.md)）。

## 3. 风格锚点（严格模仿，禁止自造）

- 示例页：`modules/module_http/.../okhttp/OkHttpActivity.kt` + [docs/01-rules/showcase.md](docs/01-rules/showcase.md)
- 库类短注释：`libs/lib_okhttp/.../interceptor/InterceptorLogging.kt` + [docs/01-rules/style.md](docs/01-rules/style.md)
- 代码模板：`templates/`（与 `docs/01-rules/*` 冲突时以规则为准）
- 任务判型与规范路由：[docs/01-rules/README.md](docs/01-rules/README.md)
- 交付与范围自检：[docs/01-rules/delivery.md](docs/01-rules/delivery.md)

## 4. 完成定义（交付前门禁）

**范围（任意任务）**

```bash
git status --porcelain    # 触碰文件 ⊆ 任务白名单
git diff --stat           # 变更规模与任务复杂度匹配
```

**代码（触碰 Kotlin/Java/Gradle/XML 时）** — 与 pre-push / CI 口径一致，均带 `-PenableFlutter=false`：

```bash
./gradlew :<模块路径>:spotlessApply
./gradlew :<模块路径>:spotlessCheck
./gradlew :<模块路径>:testDemoDebugUnitTest   # 有测试时；单测变体是 demo
./gradlew :<模块路径>:lintProdDebug           # Lint 用 prod 变体
```

**条件同步（触碰则必做）**

| 触碰 | 必须同步 / 验证 |
|---|---|
| 提交落历史（须已获授权） | 粒度按 [docs/01-rules/git.md](docs/01-rules/git.md)；标题/diff 无复盘叙事 |
| 新增/删除 Activity 或模块 | 四件套（`@Route` + Manifest + `RouterPath` + 入口 `buildRouter()`）；[docs/01-rules/structure.md](docs/01-rules/structure.md)「文档同步」+ `docs/05-catalog/modules.md` + 根 README |
| 依赖/版本 | 版本在 `libs.versions.toml`；Dependency Guard 通过或已说明 baseline |
| 平行组 | 类头按 [docs/01-rules/showcase.md](docs/01-rules/showcase.md) §2.4 同构；入口文案与 `modules.md` 已同步 |
| AppInit | 手动 `XxxApp` 与 Hilt `XxxInitImpl` 双轨齐全 |
| UI 尺寸/设计 token | 对齐 [docs/01-rules/design.md](docs/01-rules/design.md) 与 `basic_shared` `dimens.xml` |
| Flutter 侧 | [flutter/flutter_demo/AGENTS.md](flutter/flutter_demo/AGENTS.md) 不变量与 `fvm flutter analyze` |

任一项不过：裁剪后重验，禁止用额外说明把越界合理化。冲突优先级见 [docs/01-rules/README.md](docs/01-rules/README.md)。
