# 工程化注释与落点

> **职责边界**：
> - 运行时硬契约 → [AGENTS.md](../../AGENTS.md) (L0)
> - 示例页 / 平行实现 / 日志 → [showcase.md](showcase.md)
> - 通用命名与库类注释 → [style.md](style.md)
> - 代码模板 → [templates.md](templates.md)
> - 本文只覆盖 **工程化配置、Flutter、XML/Manifest、混淆、检查清单与风格收敛**；禁止二次抄写示例页模板。

---

## 0. 风格锚点（模仿结构，禁止发明新模板）

| 场景 | 锚点文件 |
|------|----------|
| 示例页类头 / 交互 / 日志 | 见 [showcase.md](showcase.md)（锚点 `OkHttpActivity`） |
| 库类短注释 | 见 [style.md](style.md)（锚点 `InterceptorLogging`） |
| Convention Plugin | `build-logic/.../AndroidFeatureConventionPlugin.kt` |
| Git 钩子 | `tools/pre-push` |
| Python 工具 | `tools/benchmark-report.py` |
| CI 设计说明 | `.github/workflows/build.yml` |
| Lint 规则 | `lint/.../TestNamingDetector.kt` |
| 混淆规则 | `libs/lib_okhttp/consumer-rules.pro` |
| 全局属性 | `gradle.properties`、`gradle/libs.versions.toml` |
| 路由常量 | `basic/basic_shared/.../RouterPath.kt` |
| Flutter 文档注释 | `flutter/flutter_libs/lib_mqtt/.../mqtt_client_listener.dart` |

必读：[README.md](README.md)、showcase.md、style.md、delivery.md、structure.md、templates.md、design.md、git.md、`docs/02-engineering/*`、仓库根 AGENTS.md。

---

## 1. 工程与文件底线

- 换行符 **LF**、缩进 **4 空格**、编码 UTF-8；**禁止文件头 BOM**（若见 `﻿package` 必须去掉）。
- 每个 Activity 必须有 `@Route(path = RouterPath.Xxx.Yyy)`，路由定义在 `basic_shared/.../RouterPath.kt`。
- 资源前缀：见 [structure.md](structure.md)「modules 资源前缀」（新建用域名前缀；`ui_`/`demo_` 为历史例外）；非传递 R 类，跨模块资源写全 R 类路径。
- 禁止「可能出错」式防御校验、magic number、`postDelayed` 瞎猜、空 catch 兜底（详见 [structure.md](structure.md) 根因治理）。
- 改代码必须按 [structure.md](structure.md)「文档同步」表更新相关文档。
- 改完执行：`./gradlew :<模块路径>:spotlessApply`，再 `spotlessCheck`。

---

## 2. 工程化代码注释

工程化读者是「后续改构建/门禁/规则的人」。注释必须优先写清**设计意图、边界条件、绕过方式与变更风险**，而不是复述配置项。

### 2.1 总原则

1. **Why > What**：非显然的权衡、不这样会怎样、何时可以改；显而易见的 `apply` / `uses` 不写。
2. **入口文件必有文件头**：插件、钩子、CI、脚本缺少「职责 + 设计要点 + 用法/安装/跳过」视为不合格。
3. **保留许可证头**：NiA 衍生文件不得删除或改写 Apache License 头；自有新插件不编造 NiA 版权。
4. **分区注释**：长脚本/Workflow 用 `# ----` 或 YAML 注释分节。
5. **禁止逐步翻译**：不给每个字段/步骤写一行注释。
6. **文档联动**：Convention / Lint / 钩子 / CI 变更必须回写 `docs/02-engineering/*` 与必要时的 `AGENTS.md`。

### 2.2 Convention Plugin / Gradle（build-logic、settings、properties）

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

`libs.versions.toml`：

- 分区 `# ==== N. 主题 ====`；每库一句中文职责 + 官方文档/GitHub。
- 有耦合或实测结论的追加「本仓基线」。

### 2.3 Git Hooks / Shell（`tools/`）

文件头必须包含：职责、执行步骤、设计要点、安装、临时跳过、输入约定与退出码（对齐 `tools/pre-push`）。

- 中文注释；关键分支旁写 why。
- 函数上方一行说明输入/输出；禁止给每行 `local` 赋值写注释。
- 与 `install-git-hooks.sh`、工程化文档中的跳过方式三处同步。

### 2.4 Python 工具脚本

模块 docstring：职责、默认行为、CLI 示例、关键输出约定（对齐 `tools/benchmark-report.py`）。

- 函数仅在语义不直观时写 docstring；优先类型注解 + 清晰命名。
- 中文 docstring；标识符仍用英文。

### 2.5 CI Workflow

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

### 2.6 自定义 Lint 与测试

Lint 类 KDoc：规则职责、判定标准、避免误报的边界、重要取舍。伴生常量说明匹配语义。报错 message 中文且可操作（给出建议改名）。规则变更同步 `testing.md` 与单测。

测试类 KDoc：测什么、用什么替身/框架、验证什么不变量。方法名强制下划线式（Lint 校验）；中文语义进 KDoc 或断言消息。Fake 说明预置行为与可观测记录。禁止 Mock 框架注释口径。

### 2.7 混淆规则（`consumer-rules.pro` / `proguard-rules.pro`）

- 文件头 `# ==== 库名混淆规则 (模块名) ====`；分主题 `# ----`。
- 每条非平凡 keep/dontwarn 上方写原因（反射、动态代理、R8 Full Mode）。
- 禁止无注释堆规则。

---

## 3. 其他落点注释

### 3.1 Flutter / Dart

- 使用 `///` 文档注释；类头写职责与**与 Android 侧对齐关系**；成员写 `[param]` 语义。
- 回调线程 / isolate 约定必须写明。
- 禁止把 Kotlin `/** */` 风格搬到 Dart。

### 3.2 XML 资源

- `dimens` / `colors` / `attrs` / `styles`：保持分组头；说明用途与规范依据，不复述数值。
- `layout`：仅标注功能区块与非显然约束（预览层、为何 TextureView / 4:3）；**删除大段注释掉的死代码**。
- `styles`：去掉 Template 英文残留，统一中文。

### 3.3 AndroidManifest

- 权限/组件分组注释说明「为何需要」。
- `exported` / `tools:node` / FileProvider 路径等特殊声明必须写原因。
- 禁止注释掉整段组件却不删除。

### 3.4 模块 / 库 `README.md`

- `<!--region graph-->` 内 Mermaid **禁止手改**（`generateModulesGraph` 生成）。
- region 外可写模块一句话职责；不要手写第二份依赖图。

### 3.5 路由 / 分类常量

- `RouterPath` 对象级横线分组与一级技术分类对齐；路径格式与大小写规则见 [structure.md](structure.md)「路由」。
- 常量旁仅注释权限/特殊语义（如 `PERMISSION_LOGIN`），不给每个 path 重复写「这是 xx 页路由」。

---

## 4. 一律保留、禁止「统一风格」误伤

- Apache / 其他 License 头
- `@Suppress`、`// ignore:`、Lint suppress、`suppress DeprecatedClassUsageInspection`
- Graph region、wrapper 时间戳、`local.properties`、`gradle-daemon-jvm.properties` 等生成物
- 已有技术选型、方案对比、设计约束类注释（只允许增量追加）

---

## 5. 交付检查清单

### 5.1 通用

- [ ] 注释符合 showcase.md / style.md 与本文对应章节；未删除既有技术决策注释（`git diff` 无非预期 `-` 注释行）
- [ ] 无 BOM、无 CRLF、无未使用 import
- [ ] 资源前缀、`@Route`、Manifest、RouterPath、入口列表已同步
- [ ] 相关 `docs/` 与必要时根 `README.md` 已按 [structure.md](structure.md)「文档同步」表更新
- [ ] 触碰 Kotlin/Java/Gradle 源码时：`spotlessApply`（限本次文件）+ `spotlessCheck` 通过；纯文档任务不强制跑 Gradle spotless
- [ ] Git 提交 Conventional Commits；正文写「为什么」

### 5.2 业务 / 示例页

- [ ] 类头 KDoc 符合 showcase.md §2（官方参考边界、页型、平行注释）
- [ ] 操作项 ↔ 方法一一对应，无页面级编排器；buildList 为 `"N. 动词短语"`
- [ ] 日志符合 showcase.md §3；上下文标签用半角 `[]`
- [ ] 若属平行组：兄弟页同构、版本与实际 API 一致（showcase.md「平行实现」）

### 5.3 工程化

- [ ] 文件头 / 类 KDoc 说清职责与设计要点
- [ ] 未删除 License 头
- [ ] 门禁/跳过方式与 `tools/`、`docs/02-engineering/` 一致
- [ ] Convention / Lint / CI / 钩子变更已回写文档
- [ ] 测试名符合 Lint 规则
- [ ] 模块 kts 无重复解释 Convention 事项

---

## 6. 风格收敛任务指引

当你被要求「统一风格」时：

1. **只做风格对齐，不改行为**：不重命名公开 API、不改路由、不改业务逻辑。
2. 建议优先级（可按模块拆分多次提交）：
   1. 对齐示例 Activity 类头 → showcase.md §2
   2. 去掉 BOM、统一空白（优先交给 Spotless）
   3. 去除自有成员的 `m` 前缀（对齐 [style.md](style.md)；反射目标字段、第三方库 Java 字段名不得改写）
   4. 统一日志符号与文案语气 → showcase.md §3
   5. 清理冗余行内注释，保护有信息量的注释 → style.md
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

## 7. 给 Agent 的粘贴用提示（任务级精简版）

```text
你在修改 MyApplication（E:\StudioProjects\MyApplication）——个人 Android 技术栈沉淀示例工程。

【必读】AGENTS.md（L0 硬契约，冲突时最高优先）
→ docs/01-rules/README.md（冲突优先级 + 按任务导航）
→ showcase.md（示例页/平行）→ style.md（命名/库注释）→ structure.md / delivery.md
→ templates.md → 本文件（工程化落点）→ docs/02-engineering/*
→ 具体事项以 README.md「冲突优先级」所列真相源为准，本文不得放宽其禁止项。

【示例页】严格按 showcase.md：三段式类头、官方参考边界、页型变体、平行注释、
Showcase 结构、日志 ✓/✗/→、禁止卖点避坑与【】结果装饰。
禁止在工程化文档或注释中另立一套示例页模板。

【工程化】入口文件必有文件头（职责+设计要点+用法/安装/跳过）；Why > What；
License 头与 @Suppress 一律保留；gradle/settings/toml/Lint/混淆按本文 §2。

【一律禁止】防御性魔法数、postDelayed 瞎猜、空 catch、m 前缀匈牙利命名、
页面级任务编排包装层、无故删改既有技术决策注释、全仓一把梭格式化。

【流程】最小切片；spotlessApply + spotlessCheck；按 structure.md「文档同步」表回写；
禁止 git commit / git push，除非用户明确要求。
```
