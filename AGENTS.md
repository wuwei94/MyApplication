# AGENTS.md — Agent 运行时硬性契约

> 本文件是所有 AI Agent 参与本项目开发时的最高优先级硬性执行契约。
> 按任务展开的规范入口：[docs/01-rules/README.md](docs/01-rules/README.md)。架构全景见 [docs/README.md](docs/README.md)。
> Flutter Demo 子仓另有 [flutter/flutter_demo/AGENTS.md](flutter/flutter_demo/AGENTS.md)；改 Flutter 侧时以该文件 + Flutter 自身 `analysis_options` 为准。

## 1. 交付纪律（最高优先级 · 违反即判定失败）
1. **严格爆炸半径（绝对禁止擅自附赠）**：
   - 严格仅修改任务明确指定的文件与逻辑。未被点名的文件、重构、依赖升级、额外测试或格式化一律不碰。
   - 严禁对纯理论或极其罕见的输入做过度防御。
   - 实现若必须触及范围外文件，必须立即暂停并向用户说明原因申请授权。未获明确指令严禁 `git commit` / `git push`。
2. **马尔可夫单向终态（绝对禁止复盘式交付）**：
   - 交付物只描述系统的最终状态与客观技术原因。
   - 严禁保留「无 X 版 / 已去掉 X / 为何不加 X」等试错叙事。
   - 详情见 [docs/01-rules/delivery.md](docs/01-rules/delivery.md)。

## 2. 核心技术不变量
1. **分层单向依赖**：`modules` 互不依赖；`libs` 不依赖 `modules`/`app`/`basic_*`；`basic_model` 保持纯 JVM；数据栈 `basic_*` 可按需依赖更底层 `basic_*` 与 `libs`。依赖用 typesafe accessors（`projects.xxx`）（见 [structure.md](docs/01-rules/structure.md)）。
2. **组件规范**：Activity 必须 `@Route(path = RouterPath.Xxx.Yyy)`；资源前缀与包名地图见 [structure.md](docs/01-rules/structure.md)；modules 模块依赖 `basic_lib` 与 `basic_shared`。
3. **构建约束**：版本只进 `libs.versions.toml`；SDK/flavor/lint/spotless 只走 Convention Plugin；源码落 `src/main/java`；Android 侧本地验证默认 `-PenableFlutter=false`。
4. **消灭匈牙利命名**：禁止 `m` 前缀（见 [style.md](docs/01-rules/style.md)）。
5. **示例页架构**：一个 `buildList` 项 ↔ 一个示例方法；`when(position)` 直调库 API，严禁页面级任务编排器（见 [showcase.md](docs/01-rules/showcase.md)）。
6. **注释底线**：中文；Why > What；保护既有技术决策注释。
7. **日志契约**：`→ ` / `✓ ` / `✗ `；高频用 `updateLog`；禁止 `【成功】/【失败】` 结果装饰。
8. **根因治理**：禁止 magic number、`postDelayed` 瞎猜、空 catch（见 [structure.md](docs/01-rules/structure.md)）。

## 3. 风格锚点（严格模仿，禁止自造）
- 示例页：`modules/module_http/.../okhttp/OkHttpActivity.kt` + [showcase.md](docs/01-rules/showcase.md)
- 库类短注释：`libs/lib_okhttp/.../interceptor/InterceptorLogging.kt` + [style.md](docs/01-rules/style.md)
- 平行实现与平行注释：[showcase.md](docs/01-rules/showcase.md) §1.5 / §2.4
- 交付与范围自检：[delivery.md](docs/01-rules/delivery.md)
- 规范导航：[docs/01-rules/README.md](docs/01-rules/README.md)

## 4. 完成定义（交付前自检清单）
- [ ] `git status` + `git diff --stat`：触碰文件完全在任务白名单内
- [ ] `git diff`：无非预期的注释减号行，无负向复盘词汇
- [ ] 提交粒度（若已获授权落历史）：按 [docs/01-rules/git.md](docs/01-rules/git.md)「提交粒度」切片——不跨 type / 主模块混提，同关注点不碎切；任务单写有提交计划时严格照做
- [ ] 触碰 Kotlin/Java/Gradle 源码：`./gradlew :<模块路径>:spotlessApply` 且 `spotlessCheck` 通过；纯文档/规约任务不强制跑 Gradle spotless
- [ ] 涉及 Activity 新增：`@Route` + Manifest 注册 + `RouterPath` + 入口 `buildRouter()` 四件套齐全，并按 [structure.md](docs/01-rules/structure.md)「文档同步」更新
- [ ] 涉及模块增删：已按 [structure.md](docs/01-rules/structure.md)「文档同步」更新，且未引入跨层依赖
- [ ] 涉及依赖/版本：版本在 `libs.versions.toml`；Dependency Guard 通过或已说明 baseline 变更
- [ ] 平行组变更：类头按 [showcase.md](docs/01-rules/showcase.md) §2.4 同构；入口文案与 `modules.md` 已同步
- [ ] 涉及 UI 尺寸/设计 token：对齐 [design.md](docs/01-rules/design.md) 与 `basic_shared` `dimens.xml`
- [ ] 涉及 Flutter 侧：另过 [flutter/flutter_demo/AGENTS.md](flutter/flutter_demo/AGENTS.md) 不变量与 `fvm flutter analyze`
