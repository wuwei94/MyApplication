# Now in Android 落地评估报告

> 本文档基于 [android/nowinandroid](https://github.com/android/nowinandroid) 最新主干源码，逐项评估哪些实践可以落实到 MyApplication，并给出落地方案与优先级。
>
> 首次评估：2026-09-04 ｜ **主干复核：2026-09-07**

---

## 零、落地进度与复核结论（2026-09-07）

首轮评估（09-04）列出的第一批、第二批已基本落地，本轮复核聚焦**新增变化**。

### 已落地（对照第三、五节）

| 建议项 | 状态 | 证据 |
|--------|------|------|
| TYPESAFE_PROJECT_ACCESSORS | ✅ | `settings.gradle.kts:5`（`e4aec8e3`） |
| pre-push 门禁 | ✅ | `tools/pre-push`（`e4aec8e3`） |
| JankStats 掉帧监控 | ✅ | `module_performance/.../JankStatsActivity.kt` |
| 自定义 Lint 规则 | ✅ | `lint` 模块 + `TestNamingDetectorTest`（`b991689b`） |
| Turbine + 测试替身 | ✅ | `module_reactive` 4 个测试文件（`9febfeec`、`ed1211d7`） |
| Roborazzi 截图测试 | ✅ | `1.73.0` + `module_compose` 截图测试（`9febfeec`） |

### 未落地

Navigation 3 示例、API-Impl 示范拆分、可插拔能力接口、Gradle 托管设备、JaCoCo 覆盖率。

### 本轮复核的核心结论

> **首轮报告对 AGP 的判断需要修正。** 原文写"AGP 9.1.0 → 9.3.x 可在后续常规升级中跟进，**无紧迫性**"——这个判断只看版本号，漏掉了真正的风险源：本项目 `gradle.properties` 中的 `android.newDsl=false` 与 `android.builtInKotlin=false` 是 AGP 9 的**临时逃生舱**，官方明确将在 **AGP 10.0（2026 年中）移除**。NiA 已于 2026-01 完成迁移。
>
> 这是当前唯一带**硬性截止日期**的技术债，详见下面新增的第六节。

---

## 一、评估前提：两个项目的定位差异

**在照搬任何东西之前，必须先认清一件事**：Now in Android（下称 NiA）是一个**生产级内容型 App**，而 MyApplication 是一个**技术栈沉淀 / 示例集**（30 个模块，每个模块展示一类技术能力）。

这个差异决定了取舍标准：

| 维度 | NiA | MyApplication |
|------|-----|---------------|
| 模块划分依据 | 业务特性（foryou / bookmarks / search） | 技术主题（bluetooth / http / database） |
| 数据来源 | 真实后端 + 本地缓存 | 无（纯本地示例） |
| 核心价值 | 交付可用的产品 | 沉淀可复用的技术样板 |

因此判断标准是：**这项实践能否提升本项目作为「技术沉淀样本」的含金量？** 而不是「NiA 有没有」。

---

## 二、已对齐项（无需重复投入）

对照后确认，以下能力本项目**已经到位，甚至部分领先于 NiA**，不必再做：

| 能力 | 本项目 | NiA | 备注 |
|------|--------|-----|------|
| Version Catalog | `libs.versions.toml` 分 14 类 | 同等 | 本项目分类更细，且每条带中文注释 |
| Convention Plugin | 22 个 | 17 个 | 本项目多出 ARouter / EventBus / Protobuf / ObjectBox / GreenDAO 等 |
| Compose 稳定性配置 | `compose_compiler_config.conf` | 同名文件 | 一致 |
| Baseline Profile | `benchmarks` + `baselineprofile` 插件 | 同等 | 一致 |
| Spotless + ktlint | 已配置 | 同等 | 一致 |
| dependency-guard | 已配置 | 同等 | 一致 |
| 依赖 Bundles | `[bundles]` 5 组 | 同等 | 一致 |
| 调度器注入 | `basic_lib/.../di/DispatchersModule.kt` | `core:common` | 已具备 |
| Compose BOM | `2026.01.01` | `2025.09.01-alpha` | **本项目更新** |
| 启动性能示例 | `StartupActivity`、`BaselineProfilesActivity` | 无对应页面 | **本项目独有** |

**版本差异**：AGP 本项目 `9.1.0`，NiA 已到 `9.3.2`；Kotlin 本项目 `2.2.20`，NiA `2.3.0`。版本差本身不紧急，但见第六节——**AGP 迁移存在硬性截止日期**。

**本项目领先于 NiA 的部分**：Roborazzi `1.73.0` vs NiA `1.56.0`；Compose BOM `2026.01.01` vs NiA `2025.09.01`。

---

## 三、高优先级落地项

### 1. 类型安全的项目访问器（TYPESAFE_PROJECT_ACCESSORS）

**收益最高、成本最低，建议第一个做。**

- **NiA 怎么做**：`settings.gradle.kts` 中启用 `enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")`，之后所有模块依赖写成 `implementation(projects.core.designsystem)` 而非 `implementation(project(":core:designsystem"))`。
- **本项目现状**：未启用，40+ 个模块全部使用字符串路径。
- **价值**：字符串 `"basic:basic_lib"` 拼错只能在 sync 阶段报错，且 IDE 无法重构重命名。本项目模块数（40+）**远超 NiA（约 30）**，收益更大。

**落地方案**：

```kotlin
// settings.gradle.kts，在 rootProject.name 之后、include 之前
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
```

然后全局替换 `project(":basic:basic_lib")` → `projects.basic.basicLib`，`project(":modules:module_http")` → `projects.modules.moduleHttp` 等。

- **改动量**：1 行配置 + 批量替换（约 40 处）
- **风险**：极低，纯语法糖，不改变依赖图

> ⚠️ 注意：Kebab-case 路径 `:basic:basic-lib` 会被转换为 `projects.basic.basicLib`。本项目使用下划线命名（`:basic:basic_lib`），需验证下划线在转换后的实际访问器名称，建议先在一个模块上试点。

---

### 2. 测试体系（本项目最大短板：全项目仅 2 个测试文件）

NiA 的测试基建是最值得抄的部分，而且**恰好能补齐本项目最薄弱的一环**。当前测试数为 0 附近，反而是好事——没有历史包袱，可以直接建立规范。

#### 2.1 Turbine + 手写测试替身

- **NiA 怎么做**：
  - 用 [Turbine](https://github.com/cashapp/turbine) `1.2.0` 测试 Flow；
  - **明确不使用 MockK 等 Mock 框架**，改为手写测试替身（Test Double）：`core:testing` 提供 `TestNewsRepository`、`TestUserDataRepository`、`TestAnalyticsHelper`、`TestNetworkMonitor`、`TestSyncManager` 等；
  - 通过 Hilt 的 `@TestInstallIn` 替换生产实现。
- **本项目契合点**：`module_reactive` 是 Flow / RxJava 操作符对照示例，**天然适合配套 Turbine 断言示例**，能把这个模块从"演示操作符"升级为"演示如何测试操作符"。
- **落地方案**：
  1. 版本目录新增 `turbine = "1.2.0"`、`truth = "1.4.4"`；
  2. 新增 `basic/basic_repo_test`（对应 NiA 的 `core:data-test`），为 `basic_repo` 的仓库接口提供手写测试替身；
  3. 在 `module_reactive` 的 `src/test` 下写 Flow 操作符的 Turbine 测试。

#### 2.2 Roborazzi 截图测试

- **NiA 怎么做**：`core:screenshot-testing` 模块封装 Roborazzi `1.56.0`，提供 `captureMultiDevice()`，一次跑三种设备规格：
  ```kotlin
  enum class DefaultTestDevices(val description: String, val spec: String) {
      PHONE("phone", "spec:shape=Normal,width=640,height=360,unit=dp,dpi=480"),
      FOLDABLE("foldable", "spec:shape=Normal,width=673,height=841,unit=dp,dpi=480"),
      TABLET("tablet", "spec:shape=Normal,width=1280,height=800,unit=dp,dpi=480"),
  }
  ```
  并在截图时**同步执行无障碍检查**（`checkRoboAccessibility` + ATF `AccessibilityCheckPreset.LATEST`）。
- **本项目契合点（非常强）**：`module_widget`、`module_widget_custom`、`module_widget_thirdparty`、`module_compose` 都是控件展示模块，**截图测试 = 组件视觉回归基线**，正好是这类模块最缺的东西。且 NiA 的 phone / foldable / tablet 三档与自适应布局展示目标一致。
- **落地方案**：新增 `libs/lib_screenshot_test`（或放在 `basic` 层）封装 Roborazzi 与 `captureMultiDevice`，先在 `module_widget_custom` 上试点。
- **改动量**：中等。需引入 Robolectric `4.16` + Roborazzi 插件。

#### 2.3 测试基建配套

| NiA 模块 | 对应实现 | 说明 |
|----------|----------|------|
| `core:testing` | `basic/basic_test`（待建） | `TestRunner`、`MainDispatcherRule`、`TestDispatchersModule` |
| `ui-test-hilt-manifest` | 可选 | 为 Hilt 插桩测试提供空 `HiltComponentActivity` |
| `NiaTestRunner` | — | 自定义 AndroidJUnitRunner |

---

### 3. 自定义 Lint 规则（把设计规范变成编译期强制）

- **NiA 怎么做**：独立 `lint` 模块，注册自定义检查：
  - `DesignSystemDetector`：禁止直接使用 Material 原始组件（`Button`、`Text`），强制走设计系统封装（`NiaButton`、`NiaText`）；
  - `TestMethodNameDetector`：强制测试方法命名规范（禁止反引号 `` ` `` 语法）。
- **本项目契合点**：项目已有 `docs/design.md` 设计规范，但**只写在文档里，没有任何强制手段**。自定义 Lint 可以把规范从"约定"升级为"编译期报错"。
- **额外时机价值**：本项目当前测试数接近 0，**现在正是建立测试命名规范的唯一低成本窗口**——等测试写多了再回头统一，成本会高一个数量级。
- **落地方案**：
  1. 新增 `lint` 模块，应用 `com.android.lint` 插件；
  2. 注册 `IssueRegistry`（通过 `META-INF/services`）；
  3. 先实现 `TestMethodNameDetector`（规则简单、见效快），再实现设计规范检查器（可对齐 `docs/design.md` 的间距 / 圆角 / 字号体系）。
- **改动量**：中等，但两个 Detector NiA 都有完整实现可参照（含对应单测）。

---

### 4. JankStats 掉帧监控 + Compose 运行时追踪

- **NiA 怎么做**：
  - `app/src/main/.../di/JankStatsModule.kt` 注入 `JankStats`，在 `MainActivity` 中 `JankStats.createAndTrack(window, frameListener)`，只记录 janky frames；
  - 引入 `androidx.tracing:tracing-ktx` 与 `androidx.compose.runtime:runtime-tracing`，用 `trace("section") { }` 标记关键区段。
- **本项目契合点**：`module_performance` 已有 `BaselineProfilesActivity`、`StartupActivity` 等性能页面，**但全部停留在"原理讲解"层面，没有一个真实的性能监控闭环**。JankStats 能提供真实的掉帧率数据，正好补上这个缺口。
- **额外收益**：`runtime-tracing` 会让 Macrobenchmark 报告里显示可读的方法名（而非混淆后的符号），**直接提升已有 `benchmarks` 模块的可读性**，属于低成本高回报。
- **落地方案**：`module_performance` 新增 `JankStatsActivity`，实时显示当前页面帧率与掉帧计数。
- **改动量**：小，两个依赖 + 一个页面。

---

### 5. Navigation 3 示例

- **NiA 怎么做**：**已经从 Navigation 2 全面迁移到 Navigation 3**（`androidx.navigation3:navigation3-runtime` / `navigation3-ui`），核心抽象从"路由字符串"变为"类型化的 `NavKey`"：
  ```kotlin
  // core/navigation/NavigationState.kt
  val topLevelStack = rememberNavBackStack(startKey)
  val subStacks = topLevelKeys.associateWith { key -> rememberNavBackStack(key) }
  ```
  即用 `topLevelStack` + `subStacks` 实现**每个 Tab 独立的多返回栈**，并搭配 `androidx.lifecycle:lifecycle-viewmodel-navigation3` 与 `androidx.compose.material3.adaptive:adaptive-navigation3`。
- **本项目现状**：`module_compose` 依赖的是 `nowinandroid.android.arouter` 约定插件（全局 36 个模块使用 ARouter），Navigation 版本 `2.9.7`。
- **重要判断**：这不是"要不要换掉 ARouter"的问题。本项目的定位是技术沉淀，**ARouter 与 Navigation 3 应当作为并列的两种方案共存**——`docs/modularization.md` 已经论证了 API-Impl + DI 是长期方向，Navigation 3 正是这条路线上的最新官方答案。
- **落地方案**：在 `module_compose` 内新增 Navigation 3 示例页面组，展示 `NavKey` 定义、`NavDisplay`、多返回栈、`ViewModel` 集成，与既有 ARouter 示例形成对照。
- **改动量**：中等（新库 + 示例页面），但不触碰现有 ARouter 链路，零回归风险。

> 📌 附带观察：NiA 的 `AGENTS.md` 至今仍写着 "Navigation is handled by Jetpack Navigation 2 for Compose"，而源码早已是 Navigation 3。**文档滞后于代码是常态**。本项目 `docs/conventions.md` 不变量第 7 条已要求代码与文档同步，建议每次升级技术栈后例行复核。

---

### 6. 可插拔能力接口（AnalyticsHelper 模式）

- **NiA 怎么做**：`core:analytics` 定义单一接口 + **三套实现**：

  | 实现 | 用途 |
  |------|------|
  | `FirebaseAnalyticsHelper`（`src/prod`） | 生产环境真实上报 |
  | `StubAnalyticsHelper`（`src/demo`） | 只打 logcat |
  | `NoOpAnalyticsHelper`（`src/main`） | 空实现，供测试与 Preview 使用 |

  并通过 CompositionLocal 提供安全默认值：
  ```kotlin
  val LocalAnalyticsHelper = staticCompositionLocalOf<AnalyticsHelper> {
      // Provide a default AnalyticsHelper which does nothing.
      // This is so that tests and previews don't have to provide one.
      NoOpAnalyticsHelper()
  }
  ```

- **本项目价值**：这是一个**通用的「可插拔能力」架构样板**，与埋点本身无关。项目里任何"有多种实现、且调试/生产需要切换"的能力都可以套用，例如日志、崩溃上报、图片加载引擎（Glide/Coil 已有对比）、推送通道。
- **落地方案**：不必建 `analytics` 模块，可抽象为 `basic_lib` 中的一个通用模式示例，或直接在现有 `module_di` 中展示"接口 + 多实现 + 空对象默认值"这套组合。
- **改动量**：小。

---

### 7. 工程门禁：pre-push 钩子 + 托管设备 + 覆盖率

| 项 | NiA 做法 | 本项目落地建议 |
|----|----------|----------------|
| **pre-push** | `tools/pre-push` 脚本，push 前跑格式与静态检查 | **成本最低的门禁**，直接照搬，把 Spotless / Lint 卡在本地 |
| **Gradle 托管设备** | `GradleManagedDevices.kt`，CI 直接 `./gradlew pixel6api31aospDebugAndroidTest` 自动管理 AVD | 配合第 2 项测试体系引入；新增约定插件 `GradleManagedDevices.kt` |
| **JaCoCo 覆盖率** | `AndroidLibraryJacocoConventionPlugin` + `AndroidApplicationJacocoConventionPlugin` | 建议**等测试有一定基础后再加**，否则 0% 报告无意义 |
| **禁用测试动画** | `testOptions.animationsDisabled = true` | 引入插桩测试时同步加上，一行配置 |
| **Badging** | `Badging.kt` 记录 APK 体积 / 权限基线 | 可选，对示例项目价值有限 |

---

## 四、明确不建议照搬的部分

以下实践在 NiA 中很重要，但**落到本项目是负收益**，列出以避免误判：

| NiA 实践 | 不建议的原因 / 落地现状 |
|----------|--------------------------|
| 全量 `Synchronizer` + `SyncWorker` 后台轮询调度 | NiA 针对多业务特性的 WorkManager 全局同步机制较重。本项目已在 `basic_repo` 与 `module_arch`（`OfflineFirstActivity`）落地了核心的 **Room 响应式流 + 网络写同步（SSOT / Offline-First）架构模式**，满足教学沉淀需求，无需照搬全套 WorkManager 轮询骨架 |
| `demo` / `prod` product flavor | NiA 用 flavor 区分"本地静态数据"与"真实后端"。本项目不需要数据源切换，该维度无意义 |
| Firebase Analytics / Crashlytics | 无后端，且会引入 `google-services.json` 配置负担 |
| `app-nia-catalog` | NiA 把组件目录独立成 App。**本项目已有 `module_sample` + `DirectoryActivity` 目录入口**，职能等价 |
| **全量 API-Impl 拆分** | NiA 按业务特性拆 `feature:x:api` / `feature:x:impl`。本项目模块按**技术主题**划分（`module_bluetooth`、`module_http`），彼此本就无依赖，全量拆分**只增加模块数与构建负担，无解耦收益** |
| KMP 化 | 不在本项目技术栈定位内 |

**关于 API-Impl 的折中建议**：`docs/modularization.md` 已详细论证 API-Impl 方案 B，但代码里尚未落地（`settings.gradle.kts` 中无任何 `:api` / `:impl` 模块）。文档与实践脱节。

建议**不要全量拆**，而是选 1~2 个**有业务属性**的模块（如 `module_feature`）做 API-Impl 示范，把文档里的方案 B 变成可运行的代码。这样既有落地实证，又不会污染整体结构。

---

## 五、建议推进顺序

> **2026-09-07 更新**：第一批、第二批已全部落地（见第零节），当前推进顺序重排如下。**AGP 10.0 迁移因其硬性截止日期被提至最前。**

### ✅ 已完成（第一批 / 第二批）

1. TYPESAFE_PROJECT_ACCESSORS
2. pre-push 门禁
3. JankStats 掉帧监控
4. Turbine + 手写测试替身
5. 测试命名 Lint 规则
6. Roborazzi 截图测试（1.73.0）

### 第零批：有截止日期，立即启动（新增）

**0. AGP 10.0 迁移** — 见第六节
- 0.1 **探测**：翻转两个 flag 收集报错清单（1 天内可完成）
- 0.2 **kapt → KSP**：✅ 已完成（2026-09-07）。Glide 已迁 KSP；但 ARouter/EventBus/Hilt/Room 仍走 kapt（见第六节更正）
- 0.3 **分批迁移**：用 `android.newDsl.optOut` 逐模块推进
- 0.4 **Flutter 链路单独评估**：✅ 已完成（2026-09-07）。5 个 Flutter 插件全部已适配 built-in Kotlin，见第六节「Flutter 上游摸底」

### 第三批：架构示范补全

7. **Navigation 3 示例** — 已 GA（1.0.0），与 ARouter 形成对照，不触碰现有链路
8. **API-Impl 示范拆分** — 让 `docs/modularization.md` 的方案 B 有代码实证（NiA 也在收敛，仅做示范）
9. **可插拔能力接口模式** — 抽象进 `basic_lib` 或 `module_di`

### 第四批：配套完善

10. **Gradle 托管设备** — 配合插桩测试
11. **JaCoCo 覆盖率** — 待测试有基础后引入
12. **configuration-cache / isolated-projects** — 见第七节，需试点，不能无脑开
13. **AGP 9.1.0 → 9.3.x、Kotlin 2.2.20 → 2.3.0** — 随第零批迁移一并跟进

---

## 六、AGP 10.0 迁移倒计时（本轮新增，最高优先级）

> 首轮报告将 AGP 升级归入"第四批：配套完善"并标注"无紧迫性"。**这个判断需要修正**——紧迫性不来自版本号，而来自 opt-out 的移除期限。

### 背景：AGP 9.0 的两个破坏性变更

AGP 9.0（2026-01 发布）引入两项默认启用的变更：

| 变更 | 内容 | 退出开关 |
|------|------|----------|
| **newDsl** | 旧 DSL 类型（`BaseExtension` 等）与旧 Variant API 被新接口取代，旧类型标记弃用 | `android.newDsl=false` |
| **builtInKotlin** | AGP 内置 Kotlin 支持，不再需要应用 `org.jetbrains.kotlin.android`；AGP 运行时依赖 KGP `2.2.10+` | `android.builtInKotlin=false` |

**官方明确：两个退出开关将在 AGP 10.0（2026 年中）移除。** 届时无法再退回旧行为。

### 本项目的真实处境

`gradle.properties` 第 6 节写着：

```properties
android.builtInKotlin=false
android.newDsl=false
```

注释将其描述为"Gradle 9.0+ / AGP 现代构建属性"——**这两行不是资产，是账单**。

对照 NiA：2026-01-27 完成 "Finalize bump agp 9.0 (enable newDsl)"（含 protobuf 插件升级、Android library → Jvm library 改造），当前 `gradle.properties` 中**已无任何 opt-out flag**。

### 迁移风险点（按风险排序）

**1. Flutter add-to-app —— 最高风险，且不完全可控**

本项目 `enableFlutter=true`，`module_flutter` 走 add-to-app 集成。

- `flutter build` 时 Flutter 工具会自动向 `/android/gradle.properties` 写入两个 opt-out flag；但 **add-to-app 宿主是纯原生工程，Flutter 工具不参与其构建，不会写，只能手工维护**；
- 更关键的是：Flutter 插件依赖树中**任意一个**未迁移的传递插件都足以阻塞整个构建（上游跟踪 issue #181383）。这部分**依赖生态进度，本项目无法自行解决**。

**2. kapt —— 风险低，但必须处理**

> **2026-09-07 更新（迁移进度）**：直接 `kapt()` 只有 `lib_image_loader`（Glide），但 ARouter / EventBus / Hilt / Room 四个约定插件均注入注解处理器。**Glide / Hilt / Room 已迁 KSP ✅**；**ARouter / EventBus 无官方 KSP，是剩余 blocker**。
>
> **KSP 生态结论**：Hilt（Dagger 2.48+）、Room（2.5+）官方支持 KSP，同一编译器工件 `ksp`/`kapt` 双用；Glide 用独立 `glide:ksp` 工件（不生成 `GlideApp`，需 `GlideApp`→`Glide`）；**ARouter 1.5.2 停更无 KSP**（第三方 `JailedBird:ArouterKspCompiler` 可无侵入替代）；**EventBus 3.3.1 停更**，`eventbus-annotation-processor` 仅支持 kapt/annotationProcessor，无 KSP。

**3. 约定插件的 Kotlin 配置**

`build-logic/.../nowinandroid/AndroidKotlin.kt` 通过 `KotlinAndroidProjectExtension` / `KotlinJvmProjectExtension` 配置 `compilerOptions`（jvmTarget、freeCompilerArgs）。builtInKotlin 下扩展获取方式变化，需验证。涉及 3 个约定插件：`AndroidApplicationConventionPlugin`、`AndroidLibraryConventionPlugin`、`AndroidTestConventionPlugin`。

**4. 第三方老插件兼容性**

ARouter、GreenDAO、ObjectBox 均为历史较久的插件，`newDsl` 下可能触发 `ClassCastException: ApplicationExtensionImpl$AgpDecorated_Decorated cannot be cast to BaseExtension`。

> **2026-09-07 探测修正**：GreenDAO 的 `apply(plugin = "org.greenrobot.greendao")` 在约定插件里已被注释、无模块实际使用，**风险消除**；ARouter 未使用 `register` 插件（`gradlePluginArouter`），风险集中在 `arouter-compiler` 的 kapt（已归入风险点 2）；**ObjectBox**（`io.objectbox`，仅 `module_database` 使用）已升级 5.4.2 并**实测确认 newDsl 兼容**（详见「Flutter 上游摸底」小节保留项 B）。

### 探测结果（2026-09-07 实测，翻转两 flag 收集报错）

按翻转顺序实测，报错链如下（均为**配置阶段**报错，`help` 任务即可复现，无需完整编译）：

| 顺序 | 触发条件 | 报错 | 位置 | 结论 |
|------|----------|------|------|------|
| 1 | `newDsl=true` | `dev.flutter.flutter-gradle-plugin` 应用失败 `NullPointerException` | `flutter_demo/.android/Flutter/build.gradle` | Flutter 链路阻塞，依赖上游（0.4） |
| 2 | `newDsl=true`（单独） | `ApplicationExtensionImpl$AgpDecorated_Decorated cannot be cast to BaseExtension` | `kotlin-android` 插件 | KGP 2.2.20 仍在用旧 `BaseExtension` API |
| 3 | `builtInKotlin=true` | `'org.jetbrains.kotlin.android' plugin is no longer required since AGP 9.0` | `kotlin-android` 插件 | 必须移除 `kotlin-android` |
| 4 | 移除 `kotlin-android` 后 | `'org.jetbrains.kotlin.kapt' plugin is not compatible with built-in Kotlin support` | `kotlin-kapt` 插件 | kapt 必须全部迁 KSP |

**关键结论**：

1. **builtInKotlin 强制 kapt 清零**——ARouter / EventBus / Hilt / Room 四类 kapt 处理器都必须迁 KSP，范围远大于 0.2 的 Glide（Hilt/Room 官方支持 KSP；ARouter 已停更、EventBus 需核实）。
2. **`kotlin-android` 必须从 3 个约定插件移除**（Application / Library / Test），而非仅在 newDsl 下报 ClassCastException 的表象。
3. **Flutter 是最先暴露的阻塞点**，与 builtInKotlin 无关，纯 `newDsl=true` 即触发。
4. 风险点修正：GreenDAO 插件已被注释（无风险）；ARouter 未用 register 插件（风险集中在 `arouter-compiler` 的 kapt）；ObjectBox（`io.objectbox`，仅 `module_database` 使用）当时被 kapt 报错掩盖、未探测到——**现已升级 5.4.2 并单独实测确认 newDsl 兼容（🟢）**。

### Flutter 上游摸底（0.4，2026-09-07 逐项查证）

针对 `module_flutter`（add-to-app）依赖树中会应用 KGP、进而受 builtInKotlin / newDsl 影响的 5 个插件，逐个查证上游是否已发布 built-in Kotlin 适配版本：

| 插件 | 本项目版本（pubspec.lock） | 依赖方式 | 适配状态 | 依据 |
|------|---------------------------|----------|----------|------|
| `android_id` | 0.5.2+1 | 直接 | ✅ 已适配 | 0.5.2 changelog「Support AGP 9 and Flutter's built-in Kotlin mode…Stop declaring plugin-owned AGP/KGP classpaths」，README 明确支持 |
| `flutter_image_compress_common` | 1.1.1 | 传递（来自 `flutter_image_compress` 2.5.1） | ✅ 已适配 | 2.5.1 changelog「handle Gradle 9 Kotlin modes safely (#401)」 |
| `flutter_udid` | 4.1.6 | 直接 | ✅ 已适配 | 4.1.3「Migrate AGP 9 Kotlin compatibility」→ 4.1.4「preserving Kotlin compilation when `android.builtInKotlin=false`」→ 4.1.5「use host app AGP」 |
| `objectbox_flutter_libs` | 5.3.2 | 直接 | ✅ 无需适配 | 纯运行时 `.so` 库，不含 Kotlin 编译逻辑；真正的适配点在宿主工程的 `io.objectbox` 插件（见下） |
| `photo_manager` | 3.12.0 | 传递 | ✅ 已适配 | 3.10.0「Migrate to Flutter's built-in Kotlin integration with `kotlin-android` fallback」→ 3.11.0「Fix Gradle 9 configuration failures…correctly select built-in Kotlin when `android.builtInKotlin` is unset」 |

**结论：5 个插件全部已发布 built-in Kotlin 适配版本，且本项目锁定的版本均已覆盖。** Flutter 插件生态这一环不再阻塞 AGP 10.0 的 builtInKotlin 迁移。

**但仍有两个保留项，不能因此放松节奏：**

**保留项 A —— Flutter 框架自身的 newDsl 迁移未完成（与插件生态无关）**

探测结果第 1 行：`dev.flutter.flutter-gradle-plugin` 在 `newDsl=true` 下报 `NullPointerException`。这是 Flutter 框架层面（tracking issue #180137），非任何第三方插件导致。当前缓解是 Flutter 3.44 通过 #184838 把 AGP DSL 配置为兼容旧类型（等价默认 `android.newDsl=false`）。**AGP 10.0 移除开关后，必须等 Flutter 框架完成 newDsl 迁移**，本项目无法自行解决——这是比插件生态更硬的约束。

**保留项 B —— ObjectBox（`io.objectbox`）newDsl 兼容性（纯原生插件，非 Flutter）✅ 已实测确认（2026-09-07）**

仅 `module_database` 使用的 `io.objectbox` 是字节码级 Gradle 插件。**原生侧版本已从 5.1.0 升级到 5.4.2**（`objectbox` + `gradlePluginObjectBox` 两处版本号），`flag=false` 下 `:modules:module_database:compileDemoDebugKotlin` BUILD SUCCESSFUL（实体 `ObjectBoxNote` 正常解析生成）。两 flag 状态如下：

- **builtInKotlin：✅ 官方已支持。** ObjectBox 官方 Getting Started 明确给出两套配置——AGP 9.0+ 用 `com.android.legacy-kapt`（AGP 自带，替代 `org.jetbrains.kotlin.kapt`）；AGP 8.13- 用 `kotlin-kapt` + `kotlin-android`。即 builtInKotlin 下 ObjectBox 的注解处理器（`objectbox-processor`，仅 kapt、无 KSP）改走 `legacy-kapt` 即可继续工作。
- **newDsl：✅ 已实测确认兼容。** 三层证据链：
  1. **字节码**：主插件 `objectbox-gradle-plugin-5.4.2` 只引用新 Variant API（`AndroidComponentsExtension` + `android/build/api`），零引用 `BaseExtension`/`AppExtension`/`LibraryExtension`；唯一的旧类型引用集中在 `agp-wrapper-7-2` 的 `AndroidPlugin72`（该 wrapper 官方定位「仅用于字节码转换」）。
  2. **源码 + 反编译**：`AndroidPlugin72.getFirstApplicationId()` 用 `ExtensionContainer.findByType(BaseExtension)` **安全查找**（查不到返回 null → Kotlin `when(null)` 走 `else -> null`，`checkcast null` 不抛异常），而非 kotlin-android 的 `cast to BaseExtension` 强转（后者才是探测报错的 `ClassCastException` 来源）；源码注释明确「as of 9.0 the extension types are deprecated」。核心 `registerTransform` 完全走新 Variant API。
  3. **隔离实测**：独立纯 Java 模块 + `io.objectbox 5.4.2` + `newDsl=true`（AGP 9 默认即为 true），`help` 配置阶段与 `assembleDebug`（含 `transformDebugClassesWithAsm` 字节码转换）均 BUILD SUCCESSFUL。

**结论：ObjectBox 不再阻塞 newDsl 迁移。** 唯一遗留：builtInKotlin=true 时需把 `kotlin-kapt` 换成 `com.android.legacy-kapt`（归属风险点 2 的 kapt 迁移范畴）。

**对整体节奏的影响（三色灯）**

| 环节 | 状态 | 说明 |
|------|------|------|
| Flutter 插件生态（5 个） | 🟢 绿灯 | 全部已适配，不再阻塞 |
| Flutter 框架 newDsl | 🟡 黄灯 | #180137 未完成，靠 #184838 垫片维持 `newDsl=false`；AGP 10.0 前需 Flutter 完成迁移 |
| ObjectBox newDsl | 🟢 绿灯 | 5.4.2 已实测确认（findByType 安全查找 + 新 Variant API），仅 builtInKotlin 需换 legacy-kapt |

### 推进方式

AGP 9.4+ 支持 `android.newDsl.optOut=:module` **逐模块退出**，可渐进迁移，无需一次性切换：

1. **先探测**：将两 flag 改为 `true` 后 sync / `assembleDebug --dry-run`，收集报错清单——这是获取完整问题清单的唯一手段；
2. **分批迁移**：按模块推进，用 `optOut` 隔离未就绪模块；
3. **单独评估 Flutter**：确认上游插件迁移状态后再决定整体节奏；
4. **kapt → KSP 先行**：独立、无风险、可立即做。

**建议 2026 Q4 内启动探测**，不要等 AGP 10.0 发布后被动迁移。

---

## 七、NiA 有而本项目未开的构建配置（本轮新增）

| 配置 | NiA | 本项目 | 说明 |
|------|-----|--------|------|
| `org.gradle.configuration-cache=true` | ✅ 且 `problems=fail` | **注释掉** | 跳过配置阶段。本项目插件栈杂，需试点 |
| `org.gradle.configuration-cache.parallel` | ✅ | **注释掉** | 同上 |
| `org.gradle.isolated-projects=true` | ✅ | **无** | 隔离项目 / 并行配置。65 模块收益显著，但兼容性要求最高 |
| `ksp.project.isolation.enabled=true` | ✅ | **无** | KSP 项目隔离 |
| `kotlin.daemon.jvmargs` | ✅ 独立配置 | **无** | Kotlin 守护进程只继承 Gradle 的 `-Xmx`，其余需单独声明，否则大模块编译易 OOM |
| `roborazzi.test.verify=true` | ✅ | **无** | 截图测试自动挂进 `test` 任务，避免"写完忘了 verify" |
| `android.injected.androidTest.leaveApksInstalledAfterRun` | ✅ | **无** | 插桩测试后保留 APK，配合托管设备 |

> ⚠️ `configuration-cache` 与 `isolated-projects` **不能无脑照搬**。NiA 能开是因为插件栈干净（Hilt / KSP / Room 均为官方维护）。本项目含 ARouter、GreenDAO、ObjectBox、Flutter，建议先开 configuration-cache 试跑并逐个排除违规插件，`isolated-projects` 放到最后。

---

## 八、API-Impl 收敛信号（印证首轮判断）

NiA 于 2025-12 执行 "Refactor settings feature from api to impl"——当前 `settings.gradle.kts` 中 `feature:settings` **只有 `:impl`、没有 `:api`**，而其余 feature 仍是 api + impl 双模块。

即 NiA 自身也在**收敛过度拆分**。首轮报告"不要全量 API-Impl 拆分，只选 1~2 个有业务属性的模块做示范"的判断得到佐证，维持原建议。

---

## 九、Navigation 3 已 GA（时机更新）

NiA 版本目录中 `androidxNavigation3 = "1.0.0"`，**已正式发布**（首轮评估时仍为预发布版）。作为技术沉淀项目，示例应基于稳定版搭建——当前的版本风险已消除，Navigation 3 示例可随时启动。

> 📌 附带观察：NiA 的 `AGENTS.md` 至今仍写着 "Navigation is handled by Jetpack Navigation 2 for Compose"，而源码早已是 Navigation 3。**文档滞后于代码是常态**，本项目 `docs/conventions.md` 不变量第 7 条已要求代码与文档同步，建议每次升级技术栈后例行复核。

---

## 十、参考源码索引

| 关注点 | NiA 路径 |
|--------|----------|
| 类型安全访问器 | `settings.gradle.kts`（`enableFeaturePreview`） |
| API / Impl 约定插件 | `build-logic/convention/.../AndroidFeatureApiConventionPlugin.kt`、`AndroidFeatureImplConventionPlugin.kt` |
| 截图测试 | `core/screenshot-testing/.../ScreenshotHelper.kt` |
| 测试替身 | `core/testing/src/main/.../repository/`、`core/data-test`、`core/datastore-test` |
| 自定义 Lint | `lint/src/main/.../designsystem/DesignSystemDetector.kt`、`TestMethodNameDetector.kt` |
| Navigation 3 | `core/navigation/.../NavigationState.kt`、`feature/*/impl/.../navigation/` |
| JankStats | `app/src/main/.../di/JankStatsModule.kt`、`MainActivity.kt` |
| 可插拔埋点 | `core/analytics/.../AnalyticsHelper.kt`、`StubAnalyticsHelper.kt`、`NoOpAnalyticsHelper.kt` |
| 覆盖率插件 | `build-logic/convention/.../AndroidLibraryJacocoConventionPlugin.kt` |
| 托管设备 | `build-logic/convention/.../GradleManagedDevices.kt` |
| 构建命令速查 | `AGENTS.md` |
