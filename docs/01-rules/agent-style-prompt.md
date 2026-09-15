# Agent 统一代码与注释风格提示词

> [!IMPORTANT]
> **多 Agent 执行首要入口重定向**：
> 面向所有 AI Agent 的最高优先级运行时硬契约已收敛至根目录 [AGENTS.md](../../AGENTS.md) (L0)；可勾选风格清单已提炼至 [STYLE.md](STYLE.md) (L1)；模板骨架见 [templates/](../../templates/) (L2)。
> 本文件保留作为工程化配置、Flutter、ProGuard 混淆规则及深度领域细节的参考手册。

---

## 0. 必读源文件与风格锚点

### 必读文档

1. [comments.md](comments.md) — 注释规范
2. [scope.md](scope.md) — 交付范围与改动纪律（禁止附赠 / 禁止复盘式交付）
3. [conventions.md](conventions.md) — 模块 / 路由 / 基类 / 示例页 / 改动范围 / 文档同步
4. [design.md](design.md) — 尺寸与资源
5. [git.md](git.md) — 提交与钩子
6. [../02-engineering/engineering.md](../02-engineering/engineering.md) — 工程化总览与门禁
7. [../02-engineering/testing.md](../02-engineering/testing.md) — 测试与 Lint 命名
8. 仓库根 `AGENTS.md` — 架构目录与不变量

### 风格锚点（模仿结构，禁止发明新模板）

| 场景 | 锚点文件 |
|------|----------|
| 示例页类头 KDoc | `modules/module_http/.../okhttp/OkHttpActivity.kt` |
| 示例页交互与日志 | `basic/basic_shared/.../activity/BasicResponseActivity.kt` |
| 库类短注释 | `libs/lib_okhttp/.../interceptor/InterceptorLogging.kt` |
| 多方案对比页 | `modules/module_jetpack/.../ViewModelActivity.kt` |
| Convention Plugin | `build-logic/.../AndroidFeatureConventionPlugin.kt` |
| Git 钩子 | `tools/pre-push` |
| Python 工具 | `tools/benchmark-report.py` |
| CI 设计说明 | `.github/workflows/build.yml` |
| Lint 规则 | `lint/.../TestNamingDetector.kt` |
| 混淆规则 | `libs/lib_okhttp/consumer-rules.pro` |
| 全局属性 | `gradle.properties`、`gradle/libs.versions.toml` |
| 路由常量 | `basic/basic_shared/.../RouterPath.kt` |
| Flutter 文档注释 | `flutter/flutter_libs/lib_mqtt/.../mqtt_client_listener.dart` |

---

## 1. 工程与文件底线

- 换行符 **LF**、缩进 **4 空格**、编码 UTF-8；**禁止文件头 BOM**（若见 `﻿package` 必须去掉）。
- 每个 Activity 必须有 `@Route(path = RouterPath.Xxx.Yyy)`，路由定义在 `basic_shared/.../RouterPath.kt`。
- 资源前缀：`<模块名>_`；非传递 R 类，跨模块资源写全 R 类路径。
- 禁止「可能出错」式防御校验、magic number、`postDelayed` 瞎猜、空 catch 兜底（详见 `conventions.md` 根因治理）。
- 改代码必须按 `conventions.md`「文档同步」表更新相关文档。
- 改完执行：`./gradlew :<模块路径>:spotlessApply`，再 `spotlessCheck`。

---

## 2. Kotlin 命名与结构

### 2.1 命名

| 场景 | 规则 | 正例 | 反例 |
|------|------|------|------|
| 属性 / 变量 / ViewBinding | **禁止**匈牙利 `m` 前缀，使用语义小驼峰 | `binding`、`log`、`isScanning` | `mBinding`、`mLog`、`mIsScanning` |
| 可变状态内部收敛 | 内部私有加下划线，外部暴露只读契约 | `_uiState` / `uiState` | 外部直接读写 MutableState |
| Helper / View / ViewModel 的**公开 API** | 禁止 `m` 前缀；布尔查询用 `isXxx()` / `hasXxx()` | `isRecording()`、`isCanvasEmpty()` | `mIsRecording()`、`mIsCanvasEmpty()` |
| 集合与列表 | 明确类型或复数名词 | `articles`、`actionItems` | `articleList`、`mList` |
| 函数 | 动词/动宾，小驼峰 | `postingForm`、`showDescription` | `PostForm`、`doLogin`、`mIsRecording()` |
| 操作列表文案 | 英文或简洁中文、可读、带序号/描述 | `"1. 发送表单请求 (FormBody)"` | `"点击1"` |
| 常量 | 全大写下划线，就近收敛到 `companion object` / 单例 | `SCAN_MODE_LOW_LATENCY` | 页面内魔法字符串 |
| 测试类 / 方法 | 见 §5.6；类名 `*Test`，方法 `被测对象_场景_预期结果` | `mapFlow_lowercaseInput_emitsUppercase` | 反引号中文方法名 |

### 2.2 示例页结构

- 一个 `buildList()` 项 ↔ 一个命名明确的示例方法；在 `onRecyclerClick(position, string)` 中通过 `when (position)` 直接转发（详见 [templates.md](templates.md)）。
- 库调用、回调、状态尽量收在同一方法附近；禁止在页面自造任务编排包装层。
- 导入由 Spotless 统一；不写未使用 import；trailing comma 与 Spotless 对齐。

---

## 3. 注释总原则

- 注释默认**中文**；标识符、类型名、专有术语除外。
- 写**职责、约束、非显而易见的原因**，不复述代码字面意思。
- **保护既有注释**：严禁无故删除/替换/精简已有技术选型、方案对比、设计约束。
- 修改用**最小切片**；新增说明只做**增量追加**，禁止推倒重写未变更注释块。
- 交付前自检 `git diff`：出现非预期的注释减号行必须还原。

---

## 4. 业务 / 库代码注释

### 4.1 示例 Activity 类头（价值导向模板）

```kotlin
/**
 * <技术/组件名> — <职责定位一句话>
 *
 * 核心机制与避坑点：
 * 1. <机制要点 1>：<关键说明，聚焦参数/时序/线程>
 * 2. <避坑要点 2>：<生命周期/资源释放/内存防范等关键点>
 *
 * 官方参考：
 * <官方文档或权威链接，裸 URL 一行>
 */
```

硬性约束：

- 标题行必须是 `名 — 定位`。
- 「核心机制与避坑点」2–4 条，聚焦非显而易见的设计要点与底层坑位，**禁止在类头重复抄写类内已有的伪代码**。
- 末行链接：三方技术 / 框架 / Jetpack 组件必须给出官方或权威裸 URL；**本仓自研控件、无外部文档的封装**可省略外链，或改写为源码相对路径一行。
- 小节名白名单：主小节固定「核心机制与避坑点」；可并列「方案对比」或「演进说明」；禁止【】装饰风。

页型变体（不强制套完整模板）：

| 页型 | 判定 | 类头要求 |
|------|------|----------|
| 完整示例页 | 自身演示库 API / 技术点 | 价值导向模板（名—定位 + 核心机制与避坑点 + 链接） |
| 宿主容器页 | Activity 仅挂载 Fragment，自身不演示 API | 名—定位 / 概述 / 承载子页；可省略外链 |
| 模块入口页 | `*MainActivity` 仅挂示例列表 | `*模块入口 — 导航到…` + 一句列表说明；不强制外链 |

Fragment 示例页与 Activity 完整示例页共用同一套类头模板；以承载关系为主的容器 Fragment 可按宿主容器页从简。

废弃 API 示例页（`conventions.md`「历史/废弃示例内容」）：

- 标题：`名 — 定位（已废弃）`
- 概述后固定一行：`⚠️ 历史参考：<废弃原因>，生产代码应使用 <现代替代>。`
- 允许用 `@see` 指向本仓现代方案页（如 `RenderScriptActivity` → `RenderEffectActivity`）

### 4.2 库层 / 基类

短类注释：

```kotlin
/**
 * 格式化日志拦截器
 *
 * 可解析的请求/响应体（JSON、XML 等）会格式化输出，
 * 不可解析或不适合安全读取的内容只输出 URL 和 Header。
 */
```

有调用约定、生命周期、注意点时追加「用法」「注意」；错误用法会改变行为且无法从 API 推断时附最短代码示例。禁止为篇幅堆砌。

### 4.3 成员 / 方法

- 关键成员（状态、回调、线程/默认值约定）写单段 KDoc。
- 示例方法写清「做什么 + 关键流程/约束」。
- 显而易见的空 override / 简单字段不写注释。
- 行内 `//` 只解释「为什么」。
- **移除不必要的添加**：禁止与下一行代码同义反复的叙述注释（如 `// 创建 API 接口实例` 后紧跟 `retrofit.create(...)`、`// 初始化 FastBle 单例` 后紧跟 `init(application)`）；改代码时若发现此类注释，应删除而非保留或扩写。保留解释约束、时序、坑位、默认值含义的注释。

### 4.4 日志文案（`showDescription` / `appendLog` / `updateLog`）

- 中文为主，专有名词保留英文。
- 离散结果前缀统一：成功 `✓ `，失败/错误/拒绝 `✗ `，中断/取消 `→ `。
- **禁止**用 `【成功】`/`【完成】`/`【失败】`/`【错误】`/`【异常】`/`【拒绝】`/`【中断】` 表示结果；`【连接】`/`【发送】` 等上下文标签可保留。
- 新增页默认不用装饰性 emoji；与同类已有页保持一致时才可保留。
- 高频进度必须 `updateLog(key, ...)`，禁止狂刷 `appendLog`。

### 4.5 Compose 示例

- **容器层与展示层分离**：Stateful 负责接入 ViewModel、收集 Flow 状态；Stateless 负责纯 UI 渲染并提供 `@Preview`（模板见 [templates.md](templates.md)）。
- `@Composable` 注释聚焦「演示的原语与可调参数」，禁止教程式长文。
- 交互控件仅纯粹驱动 UI 参数，不引入假业务（见 `conventions.md` Compose 准则）。

### 4.6 现代架构（UDF / MVI）

- 契约规范：`UiState`（单一可信源不可变快照）+ `UiIntent`（用户动作意图）+ `UiEffect`（一次性消费副作用，使用 Channel）。
- ViewModel 状态暴露：必须使用 `_uiState.asStateFlow()`，禁止向 UI 层直接暴露 Mutable 状态；协程异常统一通过 `runCatching` 或异常处理器捕获（模板见 [templates.md](templates.md)）。

### 4.7 契约与数据层（Room / DTO / `basic_model` / `basic_repo`）

- 类头：表或契约含义、主键/索引原因、远程字段映射约定。
- 字段：仅在命名不自解释或与服务端不一致时注释；禁止「用户名」式同义反复。
- 严禁网络层 DTO 直接穿透到 UI 界面层，必须经由 Domain / Repository 层转换。

### 4.8 基础设施门面（`basic_sync` / `basic_server` 等）

- 门面写定位与调用时机（如「Application 启动时 `initialize`」）。
- Worker / Server 写生命周期、取消与重启语义、与三方库契约。
- 可标注「对齐 Now in Android 的 Xxx」，禁止整段翻译 NiA 文档。

---

## 5. 工程化代码注释

工程化读者是「后续改构建/门禁/规则的人」。注释必须优先写清**设计意图、边界条件、绕过方式与变更风险**，而不是复述配置项。

### 5.1 总原则

1. **Why > What**：非显然的权衡、不这样会怎样、何时可以改；显而易见的 `apply` / `uses` 不写。
2. **入口文件必有文件头**：插件、钩子、CI、脚本缺少「职责 + 设计要点 + 用法/安装/跳过」视为不合格。
3. **保留许可证头**：NiA 衍生文件不得删除或改写 Apache License 头；自有新插件不编造 NiA 版权。
4. **分区注释**：长脚本/Workflow 用 `# ----` 或 YAML 注释分节。
5. **禁止逐步翻译**：不给每个字段/步骤写一行注释。
6. **文档联动**：Convention / Lint / 钩子 / CI 变更必须回写 `docs/02-engineering/*` 与必要时的 `AGENTS.md`。

### 5.2 Convention Plugin / Gradle（build-logic、settings、properties）

插件类：

```kotlin
/**
 * 功能模块约定插件
 *
 * 组合 library、Hilt、ARouter 插件并注入基础依赖。
 */
```

配置函数说明作用域与非显然约束（如 androidTest 依赖仅在存在目录时注入）。

模块 `build.gradle.kts`：

- 默认不加冗长注释；Convention 已统一的配置不在模块重复解释。
- 仅反直觉依赖选择写 1–3 行 `//`。
- 版本一律走 `libs.versions.toml`，禁止 kts 内硬编码版本再写说明。

`settings.gradle.kts` / `gradle.properties`：

- 非默认配置写 why（类型安全访问器、隔离、缓存、为何关 configuration-cache、`enableFlutter` 门禁）。
- 保持现有分区横线风格；新属性必须跟一行 why。
- **禁止**只写属性名复述（如「开启并行」紧贴 `org.gradle.parallel=true` 却不说明收益/代价）；why 写清「解决什么 / 不开会怎样 / 何时可改」。

可粘贴骨架（对齐 `gradle.properties` 既有分区）：

```properties
# ------------------------------------------------------------------------------
# N. <主题，与既有横线分组一致>
# ------------------------------------------------------------------------------
# <一句话 why：解决什么问题；不这样配会怎样；可选对齐来源（如对齐 NiA）>
some.new.property=true
```

```kotlin
// settings.gradle.kts：非默认项在语句上方写 why，禁止逐步翻译每个 include/apply
// 性能优化基线与基准测试 (Macrobenchmark & Baseline Profile)
include(":benchmarks")
// 自定义 Lint 规则模块（测试命名规范等），通过 build-logic 的 lintChecks 注入各 Android 模块
include(":lint")

// enableFlutter=false 时跳过 Flutter 工程接入，便于纯 Android 本地门禁与 CI
val enableFlutter = providers.gradleProperty("enableFlutter")
    .orElse("true")
    .get()
    .toBoolean()
```

`libs.versions.toml`：

- 分区 `# ==== N. 主题 ====`；每库一句中文职责 + 官方文档/GitHub。
- 有耦合或实测结论的追加「本仓基线」。

### 5.3 Git Hooks / Shell（`tools/`）

文件头必须包含：职责、执行步骤、设计要点、安装、临时跳过、输入约定与退出码（对齐 `tools/pre-push`）。

- 中文注释；关键分支旁写 why（全 0 SHA = 新分支/删分支等）。
- 函数上方一行说明输入/输出；禁止给每行 `local` 赋值写注释。
- 与 `install-git-hooks.sh`、工程化文档中的跳过方式三处同步。

### 5.4 Python 工具脚本

模块 docstring：职责、默认行为、CLI 示例、关键输出约定（对齐 `tools/benchmark-report.py`）。

- 函数仅在语义不直观时写 docstring；优先类型注解 + 清晰命名。
- 中文 docstring；标识符仍用英文。

### 5.5 CI Workflow

文件头写清矩阵设计与职责切分（本地门禁 vs CI、缓存前提、Flutter 独立流水线、未纳入项）。Job 名可读（如 `Spotless 格式检查`）。改并行结构或职责边界时回写文件头与 `docs/02-engineering/engineering.md`。

可粘贴文件头骨架（对齐 `.github/workflows/build.yml`）：

```yaml
name: <流水线显示名>

# <一句话：本流水线做什么、给谁用>
#
# 设计要点：
#   1. <矩阵/并行结构：拆了哪几条 Job，为何拆>
#   2. <缓存前提：不缓存时拆 Job 是否反而更慢>
#   3. <与本地门禁分工：本地拦什么，CI 兜底什么>
#   4. <独立流水线：如 Flutter / 其他被隔离的构建线>
#
# 未纳入本矩阵（演进规划）：<明确不做的检查项，避免后人误以为遗漏>

on:
  push:
    branches: [ main ]
  pull_request:
```

### 5.6 自定义 Lint 与测试

Lint 类 KDoc：规则职责、判定标准、避免误报的边界、重要取舍。伴生常量说明匹配语义。报错 message 中文且可操作（给出建议改名）。规则变更同步 `testing.md` 与单测。

测试类 KDoc：测什么、用什么替身/框架、验证什么不变量。方法名强制下划线式（Lint 校验）；中文语义进 KDoc 或断言消息。Fake 说明预置行为与可观测记录。禁止 Mock 框架注释口径。

### 5.7 混淆规则（`consumer-rules.pro` / `proguard-rules.pro`）

- 文件头 `# ==== 库名混淆规则 (模块名) ====`；分主题 `# ----`。
- 每条非平凡 keep/dontwarn 上方写原因（反射、动态代理、R8 Full Mode）。
- 禁止无注释堆规则。

---

## 6. 其他落点注释

### 6.1 Flutter / Dart

- 使用 `///` 文档注释；类头写职责与**与 Android 侧对齐关系**；成员写 `[param]` 语义。
- 回调线程 / isolate 约定必须写明。
- 禁止把 Kotlin `/** */` 风格搬到 Dart。

### 6.2 XML 资源

- `dimens` / `colors` / `attrs` / `styles`：保持分组头；说明用途与规范依据，不复述数值。
- `layout`：仅标注功能区块与非显然约束（预览层、为何 TextureView / 4:3）；**删除大段注释掉的死代码**。
- `styles`：去掉 Template 英文残留，统一中文。

### 6.3 AndroidManifest

- 权限/组件分组注释说明「为何需要」。
- `exported` / `tools:node` / FileProvider 路径等特殊声明必须写原因。
- 禁止注释掉整段组件却不删除。

### 6.4 模块 / 库 `README.md`

- `<!--region graph-->` 内 Mermaid **禁止手改**（`generateModulesGraph` 生成）。
- region 外可写模块一句话职责；不要手写第二份依赖图。

### 6.5 路由 / 分类常量

- `RouterPath` 对象级横线分组与一级技术分类对齐；路径规则写在对象 KDoc。
- 常量旁仅注释权限/特殊语义（如 `PERMISSION_LOGIN`），不给每个 path 重复写「这是 xx 页路由」。

---

## 7. 一律保留、禁止「统一风格」误伤

- Apache / 其他 License 头
- `@Suppress`、`// ignore:`、Lint suppress、`suppress DeprecatedClassUsageInspection`
- Graph region、wrapper 时间戳、`local.properties`、`gradle-daemon-jvm.properties` 等生成物
- 已有技术选型、方案对比、设计约束类注释（只允许增量追加）

---

## 8. 交付检查清单

### 8.1 通用

- [ ] 注释符合对应章节模板；未删除既有技术决策注释（`git diff` 无非预期 `-` 注释行）
- [ ] 无 BOM、无 CRLF、无未使用 import
- [ ] 资源前缀、`@Route`、Manifest、RouterPath、入口列表已同步
- [ ] 相关 `docs/` 与必要时 `README.md` / `AGENTS.md` 已按文档同步表更新
- [ ] `spotlessApply` + `spotlessCheck` 通过
- [ ] Git 提交 Conventional Commits；正文写「为什么」

### 8.2 业务 / 示例页追加

- [ ] 类头 KDoc 符合 §4.1
- [ ] 操作项 ↔ 方法一一对应，无页面级编排器
- [ ] 日志：`showDescription` / `appendLog` / `updateLog` 用法正确

### 8.3 工程化追加

- [ ] 文件头 / 类 KDoc 说清职责与设计要点
- [ ] 未删除 License 头
- [ ] 门禁/跳过方式与 `tools/`、`docs/02-engineering/` 一致
- [ ] Convention / Lint / CI / 钩子变更已回写文档
- [ ] 测试名符合 Lint 规则
- [ ] 模块 kts 无重复解释 Convention 事项

---

## 9. 风格收敛任务指引

当你被要求「统一风格」时：

1. **只做风格对齐，不改行为**：不重命名公开 API、不改路由、不改业务逻辑。
2. 建议优先级（可按模块拆分多次提交）：
   1. 补齐/对齐示例 Activity 类头 KDoc → §4.1
   2. 去掉 BOM、统一空白（优先交给 Spotless）
   3. 统一成员 `m` 前缀策略（与同类页面一致）
   4. 统一日志符号与文案语气
   5. 清理冗余行内注释，保护有信息量的注释
   6. 删除 layout 中注释掉的死代码；统一 styles/Manifest 中英混排
   7. 补齐 `libs.versions.toml` 库说明；审查 `consumer-rules.pro`
   8. 对齐 `tools/*` 与 CI 文件头
   9. 对齐 build-logic 插件类与配置函数 KDoc
   10. 对齐 lint 规则与测试类 KDoc
   11. 清理模块 `build.gradle.kts` 中与 Convention 重复的注释
   12. 同步 `docs/02-engineering/*`，避免文档与代码注释打架
   13. Flutter `///`：只补缺失，不重写已对齐段落
3. 一次只碰一个模块或一个关注点，单独提交：
   - 业务：`style(<模块名>): 统一代码与注释风格`
   - 构建：`build(...)`
   - CI：`ci(...)`
   - 测试基建：`test(...)`
4. 禁止全仓一把梭格式化污染 blame；确需大范围格式化时单独 `style` 提交并写入 `.git-blame-ignore-revs`。

---

## 10. 给 Agent 的粘贴用提示（任务级 · 可直接扫描示例与工程化）

写新代码、改代码或被要求「按约束扫描全仓」时，将下列全文视为系统指令。覆盖示例页与工程化配置两条线，冲突时以更具体条目为准，原则性条款以 `comments.md` / `conventions.md` / [scope.md](scope.md) 为准。

```text
你在修改 MyApplication（E:\StudioProjects\MyApplication）——个人 Android 技术栈沉淀示例工程。

【必读源】
STYLE.md、scope.md、templates.md、comments.md、conventions.md、design.md、
docs/02-engineering/engineering.md、engineering-build.md、testing.md、仓库根 AGENTS.md、本文件。
风格锚点模仿，禁止发明新模板（见 §0 风格锚点表）。

【示例页与代码硬约束】
1. 类头模板（完整示例页必须）：
   标题行「名 — 定位」；核心机制与避坑点 2–4 条（关键词：说明，聚焦非显而易见的设计要点/时序/线程/内存防范）；
   末行官方/权威裸 URL；禁止在类头重复抄写类内已有的伪代码。
2. 页型变体：
   - 完整示例页（自身演示库 API）→ 价值导向模板（名—定位 + 核心机制与避坑点 + 链接）；
   - 宿主容器页（仅挂 Fragment）→ 名—定位/概述/承载子页，可省略外链；
   - 模块入口页（*MainActivity）→ 名—定位 + 一句列表说明；
   - 废弃 API 页标题加「（已废弃）」，概述后固定「⚠️ 历史参考：…，生产代码应使用 …」。
3. 结构与交互：
   - 优先继承 BasicResponse/Control/Image/Layout/Recycler/BaseVB 等基类；
   - 一个 buildList 项 ↔ 一个命名明确的示例方法，在 onRecyclerClick 中通过 when(position) 直接转发；
   - 页面只保留最小状态，禁止在页面级自造任务编排包装层。
4. 命名与状态约束：
   - **禁止**匈牙利 `m` 前缀（对齐 Google 官方规范），属性与变量统一语义小驼峰（`binding`、`log`、`isScanning`）；
   - 内部可变状态私有收敛（`_uiState`），对外仅暴露只读契约（`uiState: StateFlow`）；
   - 测试类 `*Test`，测试方法 `被测对象_场景_预期结果`。
5. Compose / UDF 规范（详见 templates.md）：
   - Compose 必须遵循 Stateful 容器与 Stateless 展示层分离，展示层必须支持独立 `@Preview`；
   - 现代架构必须基于 UDF（`UiState` 快照 + `UiIntent` 意图 + `UiEffect` 一次性副作用 + `ViewModel`）；
   - Flow 测试优先使用 Turbine，数据层测试优先使用手写 Fake 替身。
6. 日志：showDescription / appendLog / updateLog；成功「✓ 」失败「✗ 」中断「→ 」；
   禁止【成功】【失败】等结果装饰；高频进度用 updateLog。
7. 每个 Activity 必须 @Route(path = RouterPath.Xxx.Yyy)；资源前缀 <模块名>_；非传递 R 类写全路径。

【工程化配置硬约束】
1. 入口文件必有文件头：职责 + 设计要点 + 用法/安装/跳过。
   覆盖：Convention Plugin、tools/* 钩子、CI workflow、Python 脚本、自定义 Lint。
2. Why > What：非显然权衡、不这样会怎样、何时可改；禁止逐步翻译每个字段。
3. Apache/License 头、@Suppress、// ignore、Graph region、wrapper 时间戳一律保留。
4. Convention Plugin：组合逻辑写清注入了什么；模块 kts 不重复解释 Convention 已统一事项。
5. gradle.properties / settings：非默认配置跟一行 why（解决什么/不配会怎样/何时可改）。
6. libs.versions.toml：分区注释；每库一句中文职责 + 官方链接；耦合处写本仓基线。
7. Lint 规则：判定标准、避免误报边界、中文可操作 message；变更同步 testing.md 与单测。
8. 混淆规则：非平凡 keep/dontwarn 上方写原因；禁止无注释堆规则。

【一律禁止】
防御性魔法数、postDelayed 瞎猜、空 catch 兜底、magic number 偏移；
页面级 pending action / operationId / runAfter 包装层；
强制使用 m 前缀的旧时代匈牙利命名法；
无故删除或改写既有技术选型、方案对比、设计约束注释（git diff 无非预期「-」注释行）；
全仓一把梭格式化污染 blame；未变更注释块划入替换范围。

【扫描与修改流程】
1. 按模块抽样/遍历 modules/**/*Activity.kt 与 libs/、basic/、build-logic/、lint/、tools/、.github/：
   - 完整示例页是否具备核心机制与避坑点 + 链接；
   - 操作列表是否规范使用 buildList 与 onRecyclerClick；
   - 属性命名是否去除了 m 前缀；
   - 入口/容器页是否按变体从简且不缺定位；
   - 工程化入口是否有文件头与 why 注释。
2. 只修违规点，最小切片；保护既有有效注释（只允许增量追加）。
3. 按 conventions.md 文档同步表回写 docs/ 与 README（如有结构/职责变化）。
4. 对触碰文件执行 ./gradlew :<模块路径>:spotlessApply，再 spotlessCheck。
5. 禁止 git commit / git push，除非用户明确要求。
```
