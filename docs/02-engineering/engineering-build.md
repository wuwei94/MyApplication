# 构建系统与代码质量治理

> 本文为《现代 Android 工程化实践》的分册之一(由原单篇长文按主题拆分而来),完整收录**构建系统与依赖治理**、**代码规范与静态治理**两章实践;工程化全景、各维度导航与命令速查见 [engineering.md](engineering.md),Convention Plugin 具体插件细则见 [build-logic.md](build-logic.md),测试落地细则见 [testing.md](testing.md)。

---

## 一、构建系统与依赖治理

### 1. Version Catalog（版本目录集中治理）

#### 为什么弃用传统 Groovy `ext` / Kotlin `buildSrc`
* **Groovy `ext`**：无类型提示、拼写错误只能在运行期报错、依赖坐标散落不可控；
* **传统 `buildSrc`**：任何配置微调都会导致全局构建缓存全部失效，触发全量重新编译；
* **Gradle Version Catalog (`gradle/libs.versions.toml`)**：Gradle 官方原生支持，声明式 TOML，支持类型安全补全，且不破坏 Gradle 构建缓存。

#### 分层设计规范
在 `gradle/libs.versions.toml` 中，按照组件职责建立严格的 14 分类分层体系：

```toml
[versions]
# 0. 语义化版本 (SemVer)
# 1. 核心运行时与 Kotlin 基础设施 (Coroutines, Turbine, Roborazzi, Robolectric)
# 2. Google 核心基础库与 Material Design (Gson, Guava, Protobuf, Material3)
# 3. AndroidX 核心组件与基础 UI 控件 (Core-KTX, Activity, Fragment, RecyclerView)
# 4. Jetpack 架构组件 (Lifecycle, ViewModel, Navigation, Room, DataStore, WorkManager)
# 5. Jetpack Compose 现代化 UI 工具包 (Compose BOM, Compiler, Foundation, Material3)
# 6. 网络通信与流式传输 (OkHttp, Retrofit, Ktor, Netty, MQTT, SSE)
# 7. 响应式编程与异步数据流 (RxJava 3, RxAndroid)
# 8. 依赖注入与组件化路由 (Hilt, Koin, ARouter)
# 9. 图片加载与媒体处理 (Glide, Coil, CameraX)
# 10. 常用第三方实用工具库 (MMKV, BaseRecyclerViewAdapterHelper, SmartRefreshLayout)
# 11. 性能优化与基准测试 (Macrobenchmark, ProfileInstaller, JankStats, Tracing)
# 12. 代码质量与工程规范 (Android Lint, Spotless, ktlint)
# 13. Gradle 构建工具与核心插件 (AGP, Kotlin, KSP, R8, Dependency-Guard)

[libraries]
# 统一遵循 <group>-<artifact> 命名映射，禁止模糊命名

[bundles]
# 原子化依赖包聚合，减少模块 build.gradle 样板代码
testing-unit = ["junit", "kotlinx-coroutines-test", "turbine"]
testing-screenshot = ["robolectric", "roborazzi", "roborazzi-compose", "roborazzi-rule"]
compose-core = ["androidx-compose-ui", "androidx-compose-material3", "androidx-compose-ui-tooling-preview"]
```

---

### 2. 复合构建与 Convention Plugins (`build-logic`)

#### 约定优于配置（Convention over Configuration）
多模块项目中如果每个模块都复制几十行 `android { ... }` 配置，升级版本或调整编译选项将是灾难。现代工程化采用 `build-logic` 复合构建（Composite Build），将通用构建逻辑抽取为 **Convention Plugin**。

当前工程 `build-logic/convention/src/main/kotlin/` 实际落地的 19 个构建管理插件清单：

```
build-logic/convention/src/main/kotlin/
├── AndroidLibraryConventionPlugin.kt           # Android Library 通用插件 【已落地】
├── AndroidApplicationConventionPlugin.kt       # Application 壳工程插件 【已落地】
├── AndroidFeatureConventionPlugin.kt           # 业务 Feature 模块插件（含公共依赖与基础配置） 【已落地】
├── AndroidFeatureComposeConventionPlugin.kt   # Compose 业务功能模块插件 【已落地】
├── AndroidLibraryComposeConventionPlugin.kt    # Compose UI 库插件 【已落地】
├── AndroidApplicationComposeConventionPlugin.kt # Compose 壳工程插件 【已落地】
├── AndroidHiltConventionPlugin.kt              # Hilt 依赖注入插件 【已落地】
├── AndroidRoomConventionPlugin.kt              # Room 数据库与 Schema 导出插件 【已落地】
├── AndroidLintConventionPlugin.kt              # Lint 检查与配置插件 【已落地】
├── AndroidTestConventionPlugin.kt              # 测试与基准模块插件 【已落地】
├── AndroidARouterConventionPlugin.kt           # ARouter 路由组件化插件 【已落地】
├── AndroidEventBusConventionPlugin.kt          # EventBus 事件总线插件 【已落地】
├── AndroidGreenDaoConventionPlugin.kt          # GreenDAO ORM 插件 【已落地】
├── AndroidObjectBoxConventionPlugin.kt         # ObjectBox 数据库插件 【已落地】
├── AndroidProtobufConventionPlugin.kt          # Protocol Buffers 插件 【已落地】
├── AndroidKspConventionPlugin.kt               # KSP 注解处理插件 【已落地】
├── AndroidKaptConventionPlugin.kt              # Kapt 遗留注解处理插件 【已落地】
├── JvmLibraryConventionPlugin.kt               # 纯 Kotlin/JVM 模块通用插件 【已落地】
└── RootPlugin.kt                               # 根工程全局管理插件（Mermaid 拓扑与 Spotless） 【已落地】
```

#### 架构演进规划插件【演进规划 - 待落地】
为实现彻底的业务物理隔离，规划在后续演进中补充成对的 API/Impl 契约隔离插件：
* `AndroidFeatureApiConventionPlugin.kt`：轻量对外接口契约层（零业务实现、仅含 Model 与 Service 接口定义）；
* `AndroidFeatureImplConventionPlugin.kt`：具体业务实现层（内部封装私有逻辑并通过 DI 装配，对外完全黑盒不可见）。

#### 模块接入极简契约
业务模块的 `build.gradle.kts` 仅需声明业务依赖与自身特有的插件别名，所有公共配置自动继承：

```kotlin
// 业务功能模块声明示例
plugins {
    alias(libs.plugins.nowinandroid.android.feature)
    alias(libs.plugins.nowinandroid.android.arouter)
}

android {
    namespace = "com.example.william.my.module.reactive"
}

dependencies {
    // 仅需声明该模块特定的业务依赖，通用依赖已由 feature 插件统一注入
    implementation(libs.rxjava3)
    implementation(libs.rxandroid3)
}
```

---

### 3. 类型安全项目访问器 (Typesafe Project Accessors) 【已落地】

在 `settings.gradle.kts` 中开启：

```kotlin
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
```

#### 收益对比
* **传统方式**：`implementation(project(":basic:basic_lib"))`。字符串路径一旦重构拼错，只能在 Gradle Sync 失败甚至编译报错时暴露，IDE 无法智能感知，无法安全重命名；
* **类型安全方式**：`implementation(projects.basic.basicLib)`。Gradle 自动根据项目目录生成强类型 DSL 访问器，输入即补全，拼错在编辑期高亮报错，支持 IDE 安全重构。

---

### 4. 现代 Gradle / AGP 深度优化与极速构建 【已落地】

#### 配置缓存（Configuration Cache）与并行配置
在 `gradle.properties` 中开启：
```properties
# 开启配置缓存，在构建脚本未变更时完全跳过 Gradle 配置期
org.gradle.configuration-cache=true
# 开启配置缓存并行执行
org.gradle.configuration-cache.parallel=true
# 遇到配置缓存违规时直接判定失败，确保构建逻辑纯洁性
org.gradle.configuration-cache.problems=fail
```
* **核心原理**：Gradle 将 Task 图的计算结果序列化到磁盘，后续构建跳过所有 `build.gradle.kts` 的执行，秒级直达 Task 执行期；
* **约束规范**：任何自定义 Task 严禁在运行期引用 `project`、`gradle` 等动态对象，所有入参和出参必须使用 `Property<T>`、`Provider<T>`、`RegularFileProperty` 显式声明。

#### 项目隔离（Isolated Projects）与 KSP 隔离
```properties
# 开启项目隔离配置，实现各个模块配置期的完全物理隔离与并发计算
org.gradle.isolated-projects=true
# 开启 KSP 项目隔离模式
ksp.project.isolation.enabled=true
```
在超多模块工程中，项目隔离禁止跨 Project 间直接访问状态，解绑各个子项目的配置计算依赖，带来巨大的配置并发提速。

#### 编译器守护进程独立内存配置
```properties
# Kotlin 守护进程只继承 Gradle 的 -Xmx，其余参数需单独声明，防止大型多模块编译 OOM
kotlin.daemon.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=1g -XX:+UseParallelGC
```

#### 现代 AGP 特性适配（BuiltIn Kotlin 与 New DSL）
现代 Android 构建工具链已深度融合 Kotlin：
* **`android.builtInKotlin=true`**：由 AGP 原生内置 Kotlin 编译配置，直接基于 Kotlin 现代编译器选项，无需单独声明旧版 Kotlin 插件配置；
* **`android.newDsl=true`**：切换至新版 Variant API 与扩展契约，淘汰旧版 `BaseExtension` 转型，保障与最新 Gradle 及 AGP 的前向兼容；
* **非传递性 R 类与编译时 R 类**：
  ```properties
  android.nonTransitiveRClass=true
  android.enableAppCompileTimeRClass=true
  ```
  每个模块仅生成自身声明的 R 类符号，避免上游模块的资源 ID 级联穿透，极大缩短多模块增量编译耗时。

---

### 5. 依赖守卫、拓扑可视化与空测试优化 【部分已落地】

* **Dependency Guard（依赖漂移防护）`【部分已落地】`**：
  接入 `com.dropbox.dependency-guard` 插件。目前壳工程已生成运行时依赖基线快照（`app/dependencies/prodDebugRuntimeClasspath.txt`）。依赖树发生非预期变更（如三方库隐式引入冲突版本或风险许可证库）时，构建直接中断并给出 diff。下一步规划在 CI 门禁中全面铺开至全部 Library 模块。
* **依赖拓扑图自动生成 `【已落地】`**：
  在根工程运行：
  ```bash
  ./gradlew generateModulesGraph
  ```
  自动遍历所有子模块依赖关系，生成可视化 Mermaid 图形（`build/mermaid/graph.txt`），实时掌握模块依赖边界。
* **空测试模块任务剔除（`AndroidInstrumentedTests.kt`）`【已落地】`**：
  在多模块工程中，多数模块未编写插桩测试。Convention 插件自动检测各模块 `src/androidTest` 目录，若无测试源码则自动关闭该模块的 `connected*AndroidTest` 任务，免除空容器构建与分发的无效开销。

---

### 6. Kotlin Multiplatform (KMP) 跨平台架构演进指南 【演进规划 - 待落地】

#### 演化驱动与定位
Google 官方 NiA 正在持续将其纯领域模型（`:core:model`）与算法工具平滑向 Kotlin Multiplatform (KMP) 演进。
当前项目中，[`basic/basic_model`](file:///e:/StudioProjects/MyApplication/basic/basic_model) 已经是**零 Android 依赖的纯 Kotlin/JVM 库**（由 `JvmLibraryConventionPlugin.kt` 配置），具备无缝向 KMP 演化的天然优势。

#### 迁移落地路径
将 `basic_model` 从纯 JVM 插件升级为 KMP 插件，使其不仅能在 Android 端运行，还能编译为 Desktop (JVM) 与 iOS (Native) 目标架构：

```kotlin
// basic/basic_model/build.gradle.kts (规划演进目标)
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    // 1. Android Target
    androidTarget()
    
    // 2. JVM / Desktop Target
    jvm("desktop")
    
    // 3. iOS Targets
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime) // 替代 java.util.Date
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
```

* **收益**：模型层与版本游标契约彻底成为跨平台资产，为后续引入 Compose Multiplatform（全平台共享 UI）奠定坚实的领域层基础设施。

---

## 二、代码规范与静态治理

### 1. Spotless + ktlint 代码格式自动化治理 【已落地】

* **工具组合**：[Spotless](https://github.com/diffplug/spotless) + [ktlint](https://pinterest.github.io/ktlint/)。
* **设计原则**：**代码格式零争论，完全交由工具自动化**。开发人员无需在 PR 中 review 缩进、空格、换行等格式细节。
* **配置范围**：
  * 全工程 `.kt` 文件：遵循官方 Kotlin 代码规范与 ktlint 规则（单行长度限制、禁止星号通配符导入、空行规则）；
  * 全工程 `.kts` 脚本文件：统一 Gradle 构建脚本书写格式；
  * `build-logic` 自身源码受同等格式约束。

#### 常用命令
```bash
# 检查全工程代码格式规范（不合规直接退出并标红）
./gradlew spotlessCheck

# 自动修复全工程所有格式违规
./gradlew spotlessApply
```

---

### 2. 自定义 Lint 规则体系（独立纯 JVM 模块） 【已落地】

#### 为什么需要自定义 Lint
架构设计规范（如间距、测试命名规范、设计系统组件使用）如果只写在 Markdown 里，极易随时间腐化。现代工程化采用**独立 Lint 模块在编译期刚性拦截违规**。

#### 落地结构
在项目中设立独立纯 Kotlin/JVM 模块 `:lint`：
```
lint/
├── src/main/kotlin/com/example/william/my/lint/
│   ├── TestNamingDetector.kt          # 测试命名规范探测器（基于 UAST 分析） 【已落地】
│   ├── DesignSystemDetector.kt        # 设计系统规范探测器（拦截裸组件与硬编码） 【已落地】
│   └── IssueRegistry.kt               # 规则注册表（通过 META-INF/services 导出） 【已落地】
└── src/test/kotlin/com/example/william/my/lint/
    ├── TestNamingDetectorTest.kt      # 测试命名 Lint 规则单测
    └── DesignSystemDetectorTest.kt    # 设计系统 Lint 规则单测
```

#### 检查规则与拦截标准
1. **测试规范检查（`TestNamingDetector`）`【已落地】`**：
   * **`TestClassName` 规则**：类中包含 `@Test` 方法时，类名必须以 `Test` 结尾（避免测试类被测试套件遗漏）；
   * **`TestMethodName` 规则**：测试方法名必须为 `被测对象_场景_预期结果` 格式（如 `fetchUser_networkError_emitsErrorState`），严禁使用反引号中文或无意义命名。
2. **设计系统规范检查（`DesignSystemDetector`）`【已落地】`**：
   * **禁止裸调 Material3 组件**：扫描 Compose 代码，拦截对 `androidx.compose.material3.Button`、`Text`、`TopAppBar` 等原生组件的直接调用，强制走工程设计系统封装；
   * **禁止硬编码 Magic Number**：拦截在 Modifier 中直接书写硬编码 dp/sp（如 `.padding(16.dp)`），强制引用设计规范中定义的语义化间距。
3. **全局生效接线**：
   在 `build-logic` 中通过 `AndroidDeps.kt` 为全工程 Android 模块统一注入：
   ```kotlin
   dependencies {
       "lintChecks"(projects.lint)
   }
   ```

---

### 3. 项目级 Lint 基线与多模块边界设计 【已落地】

* **`lint.xml` 项目级规则配置**：统一配置各个 Issue 的严重级别（Severity），严禁模块各自为政；
* **`lint-baseline.xml` 增量治理基线**：对于历史代码中的既有告警生成基线快照，**老代码不报错，新提交新增的任何违规直接中断构建**，确保技术债务不再新增；
* **严格门禁**：在 CI 中开启 `abortOnError = true` 与 `warningsAsErrors = true`，杜绝警告带病上线。

#### 多模块 Lint 边界设计与 `checkDependencies` 深度实践

在 AGP（Android Gradle Plugin）中，`lint.checkDependencies` 用于控制静态代码分析是否递归深入扫描当前模块所依赖的所有 Gradle 子工程源码。

##### 1. NiA 官方机制与适用前提
Google 官方开源项目 Now in Android (NiA) 在 `AndroidLintConventionPlugin` 中配置了 `checkDependencies = true`。NiA 能够这样配置的前提是：
* **100% 第一方原生工程**：所有子模块（`core:*`、`feature:*`）均由 Google 团队自行编写，外部依赖全走 Maven 远程 AAR 二进制；
* **CI 单一根节点驱动**：NiA 在 GitHub Actions CI 中只执行一次全量命令 `./gradlew :app:lintProdRelease`，依赖 `:app` 自顶向下穿透分析所有子模块代码。

##### 2. 本项目（大型多模块 + Flutter 混编）为什么必须设为 `false`
本项目在落地现代工程化时，明确将 `checkDependencies` 关闭（设为 `false`），根因在于解决真实工业界场景下的三大冲突：
1. **隔离混编外部源码子工程**：
   Flutter Add-to-App 机制（`include_flutter.groovy`）会将所有 Pub 插件（如 `geolocator_android`、`flutter_blue_plus_android`）以**本地 Gradle 源码子工程**形式挂载到根工程中。若开启 `checkDependencies = true`，Lint 会将这些不可控的第三方插件 Java 源码当成第一方代码进行严苛扫描，因第三方插件未在 Java 层书写 `checkSelfPermission` 而触发数十处 `MissingPermission` 错误，导致合法构建被异常阻断；
2. **避免多模块重复交叉扫描的算力黑洞**：
   本项目包含 30+ 功能模块，若每个模块都开启穿透扫描且均依赖 `basic_lib`，全量执行时底层基础库会被重复分析 30+ 次，构建耗时呈指数级膨胀；
3. **与本地增量门禁（`tools/pre-push`）完美自洽**：
   工程引入了客户端 `pre-push` 增量门禁，通过 `git diff` 逆向映射变更文件，仅对受影响的子模块独立触发 `:<module>:lintProdDebug`。各模块“自扫门前雪”，确保本地推送前在 10~20 秒内极速完成静态审查，杜绝等待焦虑。

##### 3. `checkDependencies` 选型矩阵与决策建议

| 场景 | 推荐配置 | 核心决策理由 |
| :--- | :--- | :--- |
| **日常增量门禁 / 本地提交（`pre-push`）** | `checkDependencies = false` | 保证单模块静态检查边界清晰、轻量极速，在本地秒级闭环。 |
| **所有 Library 子模块（基础层、功能模块）** | `checkDependencies = false` | 杜绝各模块重复穿透基础依赖，避免构建算力浪费。 |
| **混合开发工程（含 Flutter / RN 等源码子工程）** | `checkDependencies = false` | 物理隔离第三方未抑制的代码告警，避免外部不可控代码阻断构建。 |
| **纯原生单一入口 CI 门禁（如 NiA）** | 仅 `:app` 开启 `true` | 在不跑子模块 Lint 任务的前提下，由 `:app` 统一穿透覆盖全工程第一方代码。 |
| **发版前全局审计（`UnusedResources` / Merged Manifest）** | 仅 `:app` 发版流水线开启 `true` | 站在整包最终合并产物的“上帝视角”进行全量无用资源清理与清单冲突排查。 |

---

### 4. Compose 编译器稳定性配置与性能指标监控 【已落地】

#### 稳定性声明与强跳过模式
在根目录提供 `compose_compiler_config.conf` 配置文件，并在 `AndroidCompose.kt` 中注入：
```ini
// 声明外部不可变类或标准模型为稳定类型
java.time.Instant
java.time.LocalDate
kotlinx.datetime.Instant
```
配合 Compose 编译器的 Strong Skipping 模式，最大限度减少非必要重组开销。

#### 编译器指标与报告（Metrics & Reports）
通过开关开启 Compose 编译器诊断生成：
```bash
./gradlew assembleRelease -PenableComposeCompilerReports=true -PenableComposeCompilerMetrics=true
```
输出位于 `build/compose-reports`，精确报告：
* 哪些 Composable 函数是可跳过的（`restartable skippable`）；
* 哪些数据类的字段被推断为不稳定（`unstable class`），为 UI 性能重构提供准确数据源。

---

### 5. NiA 进阶 Lint 规则规划与拦截准则 【演进规划 - 待落地】

参考 Google 官方 NiA 最佳实践，规划在 `:lint` 模块后续拓展以下 3 项刚性静态拦截规则：

#### 1. 生命周期安全流收集检查（`CollectAsStateWithLifecycleDetector`）
* **违规场景**：在 Compose UI 树中直接调用 `Flow.collectAsState()`；
* **危害**：当 Activity/Fragment 切入后台（STOPPED 状态）时，`collectAsState()` 依然持续监听上游冷流发射，无法释放计算资源甚至导致内存泄漏与后台异常；
* **拦截与修复建议**：拦截 `collectAsState()`，强制要求使用 `androidx.lifecycle.compose` 提供的 `collectAsStateWithLifecycle()`，确保当页面生命周期低于 `Lifecycle.State.STARTED` 时自动取消协程收集。

#### 2. 现代时间 API 强制检查（`DateTimeApiDetector`）
* **违规场景**：在数据实体模型与网络层直接使用老旧易错的 `java.util.Date`、`java.util.Calendar` 或 `SimpleDateFormat`；
* **危害**：非线程安全、不支持时区不可变性、且无法支持 Kotlin Multiplatform 跨平台序列化；
* **拦截与修复建议**：强制迁移至 Java 8+ `java.time.Instant` / `LocalDateTime` 或全平台通用的 `kotlinx.datetime.Instant`。

#### 3. ViewModel 作用域声明检查（`ViewModelScopeDetector`）
* **违规场景**：在可复用的小粒度子 Composable 内部，无条件调用 `viewModel()` 默认工厂获取全局 ViewModel；
* **危害**：破坏组件的可复用性与可测试性，使子组件无法独立 Preview，且在复杂回退栈或 LazyColumn 中极易引发状态错乱与实例泄漏；
* **拦截与修复建议**：强制要求 ViewModel 仅在顶层 Screen/Route Composable 中获取，子组件只接收不可变 `UiState` 数据类与 Lambdas 回调事件（状态提升 State Hoisting）。
