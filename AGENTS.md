# AGENTS.md — Agent 运行时硬性契约

> 本文件是所有 AI Agent 参与本项目开发时的最高优先级硬性执行契约。
> 完整架构全景与技术选型文档详见 [docs/README.md](docs/README.md)。

## 1. 交付纪律（最高优先级 · 违反即判定失败）
1. **严格爆炸半径（绝对禁止擅自附赠）**：
   - 严格仅修改任务明确指定的文件与逻辑。未被点名的文件、重构、依赖升级、额外测试或格式化一律不碰。
   - 严禁对纯理论或极其罕见的输入做过度防御（不加无依据的容错分支、自定义异常或空兜底）。
   - 实现若必须触及范围外文件，必须立即暂停并向用户说明原因申请授权。未获明确指令严禁 `git commit` / `git push`。
2. **马尔可夫单向终态（绝对禁止复盘式交付）**：
   - 交付物（PR 标题、提交正文、代码注释、文档变更）只描述【系统的最终状态与客观技术原因】。
   - 严禁在任何地方保留「无 X 版 / 已去掉 X / 为何不加 X / 修复上版多余逻辑」等试错或纠错叙事。
   - 纠错必须整理为干净的语义化提交；diff 中严禁出现对冲撤销或死代码占位。

## 2. 核心技术不变量
1. **组件规范**：每个 Activity 必须标注 `@Route(path = RouterPath.Xxx.Yyy)`；资源强制使用 `<模块名>_` 前缀；必须依赖 `basic_lib` 与 `basic_shared`。
2. **消灭匈牙利命名**：彻底禁止 `m` 前缀（使用 `binding`、`log`、`isScanning` 代替 `mBinding` 等）。
3. **示例页架构**：一个 `buildList` 操作项 ↔ 一个示例方法；在 `onRecyclerClick` 中通过 `when(position)` 直接调用库 API，严禁在页面自造任务编排器。
4. **注释底线**：默认中文；解释职责、约束与底层非显而易见原因（Why > What）；严禁同义反复；保护既有技术决策注释。
5. **日志契约**：开始 `→ `，成功 `✓ `，失败 `✗ `，高频更新必须使用 `updateLog`；严禁使用 `【成功】/【失败】` 等结果装饰。
6. **根因治理**：严禁使用 magic number 偏移、`postDelayed` 瞎猜耗时、粗暴空 catch 绕过问题。

## 3. 风格锚点（严格模仿，禁止自造）
- 示例页（类头 + 交互 + 日志）：`modules/module_http/.../okhttp/OkHttpActivity.kt`
- 库类短注释：`libs/lib_okhttp/.../interceptor/InterceptorLogging.kt`
- 交付与范围自检：`docs/01-rules/scope.md`
- 详细风格清单：`docs/01-rules/STYLE.md`

## 4. 完成定义（交付前自检清单）
- [ ] `git status` + `git diff --stat`：触碰文件完全在任务白名单内
- [ ] `git diff`：无非预期的注释减号行（`- *` 或 `- //`），无负向复盘词汇
- [ ] 代码规范校验：执行 `./gradlew :<模块路径>:spotlessApply` 并确保 `spotlessCheck` 通过
- [ ] 架构文档同步：若涉及 Activity/模块增删，已按 `docs/01-rules/conventions.md` 完成文档同步
