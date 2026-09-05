# Now in Android 落地评估报告

> 本文档基于 [android/nowinandroid](https://github.com/android/nowinandroid) 最新主干源码，逐项评估哪些实践可以落实到 MyApplication，并给出落地方案与优先级。
>
> 评估日期：2026-09-04

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

**唯一落后的构建工具版本**：AGP 本项目 `9.1.0`，NiA 已到 `9.3.2`。可在后续常规升级中跟进，无紧迫性。

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

| NiA 实践 | 不建议的原因 |
|----------|--------------|
| `OfflineFirst*Repository` + `Synchronizer` + `SyncWorker` | 离线优先同步需要真实网络数据源。本项目无后端，照搬只会得到一堆空壳代码 |
| `demo` / `prod` product flavor | NiA 用 flavor 区分"本地静态数据"与"真实后端"。本项目不需要数据源切换，该维度无意义 |
| Firebase Analytics / Crashlytics | 无后端，且会引入 `google-services.json` 配置负担 |
| `app-nia-catalog` | NiA 把组件目录独立成 App。**本项目已有 `module_sample` + `DirectoryActivity` 目录入口**，职能等价 |
| **全量 API-Impl 拆分** | NiA 按业务特性拆 `feature:x:api` / `feature:x:impl`。本项目模块按**技术主题**划分（`module_bluetooth`、`module_http`），彼此本就无依赖，全量拆分**只增加模块数与构建负担，无解耦收益** |
| KMP 化 | 不在本项目技术栈定位内 |

**关于 API-Impl 的折中建议**：`docs/modularization.md` 已详细论证 API-Impl 方案 B，但代码里尚未落地（`settings.gradle.kts` 中无任何 `:api` / `:impl` 模块）。文档与实践脱节。

建议**不要全量拆**，而是选 1~2 个**有业务属性**的模块（如 `module_feature`）做 API-Impl 示范，把文档里的方案 B 变成可运行的代码。这样既有落地实证，又不会污染整体结构。

---

## 五、建议推进顺序

### 第一批：低风险、立竿见影

1. **TYPESAFE_PROJECT_ACCESSORS** — 1 行配置 + 批量替换
2. **pre-push 门禁** — 照搬 `tools/pre-push`
3. **JankStats + runtime-tracing** — 补上 `module_performance` 的监控闭环，同时提升 benchmarks 报告可读性

### 第二批：补齐测试体系（本项目最大欠账）

4. **Turbine + 手写测试替身** — 从 `module_reactive` 切入，天然契合
5. **测试命名 Lint 规则** — 趁测试数为 0，现在建立规范成本最低
6. **Roborazzi 截图测试** — 先覆盖 `module_widget_custom` / `module_compose`

### 第三批：架构示范补全

7. **Navigation 3 示例** — 与 ARouter 形成对照，不触碰现有链路
8. **API-Impl 示范拆分** — 让 `docs/modularization.md` 的方案 B 有代码实证
9. **可插拔能力接口模式** — 抽象进 `basic_lib` 或 `module_di`

### 第四批：配套完善

10. **Gradle 托管设备** — 配合插桩测试
11. **JaCoCo 覆盖率** — 待测试有基础后引入
12. **AGP 9.1.0 → 9.3.x** — 常规版本跟进

---

## 六、参考源码索引

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
