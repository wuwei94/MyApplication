# 构建逻辑（build-logic/convention）

> Convention Plugin 为所有模块提供统一的构建配置，避免在每个模块的 build.gradle.kts 中重复配置。

## 目录结构

```
build-logic/convention/src/main/kotlin/
├── com/google/samples/apps/nowinandroid/
│   ├── AndroidDeps.kt                  # 全局依赖配置
│   ├── AndroidCompose.kt               # Compose 编译选项和依赖
│   ├── AndroidKotlin.kt                # Kotlin 编译器选项
│   ├── NiaFlavor.kt                    # 产品风味（demo/prod）
│   ├── NiaBuildType.kt                 # 构建类型（debug/release）
│   ├── ProjectExtensions.kt            # Project 扩展属性
│   ├── PrintTestApks.kt                # 打印测试 APK 路径
│   └── AndroidInstrumentedTests.kt     # 禁用空测试模块
├── AndroidLibraryConventionPlugin.kt           # Library 模块插件
├── AndroidApplicationConventionPlugin.kt       # Application 模块插件
├── AndroidFeatureConventionPlugin.kt           # Feature 业务功能模块插件
├── AndroidFeatureComposeConventionPlugin.kt   # Feature Compose 业务功能模块插件
├── JvmLibraryConventionPlugin.kt               # 纯 Kotlin/JVM 模块插件
├── AndroidARouterConventionPlugin.kt           # ARouter 路由插件
├── AndroidEventBusConventionPlugin.kt          # EventBus 事件总线插件
├── AndroidHiltConventionPlugin.kt              # Hilt 依赖注入插件
├── AndroidKspConventionPlugin.kt               # KSP 注解处理插件
├── AndroidLintConventionPlugin.kt              # Lint 静态分析插件
├── AndroidRoomConventionPlugin.kt              # Room 数据库插件
├── AndroidGreenDaoConventionPlugin.kt          # GreenDao ORM 插件（已禁用）
├── AndroidProtobufConventionPlugin.kt          # Protocol Buffers 插件
├── AndroidObjectBoxConventionPlugin.kt         # ObjectBox 数据库插件
├── AndroidLibraryComposeConventionPlugin.kt    # Library Compose 插件
├── AndroidApplicationComposeConventionPlugin.kt # Application Compose 插件
└── AndroidTestConventionPlugin.kt              # 测试模块插件
```

---

## 核心配置文件

### AndroidDeps.kt

配置所有模块的公共依赖。

**基础依赖**（通过 `configureDepsAndroid`）：
- Kotlin 协程：`kotlinx-coroutines-core`、`kotlinx-coroutines-android`
- 基础测试：`junit`、`androidx-test-ext`、`androidx-test-espresso`
- *(UI 与 AndroidX 组件下沉至 `basic_lib` 按需以 `api` 导出，避免底层模块依赖污染)*

**功能模块依赖**（通过 `configureFeatureAndroid`）：
- 动态扫描 `rootProject.subprojects` 自动依赖 `:modules:module_*` 所有功能模块（零硬编码维护成本）

### AndroidKotlin.kt

配置 Kotlin 编译器选项：
- Java 兼容性：`sourceCompatibility = Java 11`、`targetCompatibility = Java 11`
- 启用核心库脱糖：`android.desugarJdkLibs`
- 编译器参数：`-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi`（启用 Flow 实验性 API）
- 可选启用：`allWarningsAsErrors`（通过 `gradle.properties` 中 `warningsAsErrors=true`）

### AndroidCompose.kt

配置 Jetpack Compose：
- 启用 Compose 编译：`compose = true`
- 使用 Compose BOM 统一版本管理
- 核心依赖：Material、Material3、Activity Compose、Navigation Compose、LiveData Compose、ConstraintLayout Compose
- 调试工具：`compose-ui-tooling`、`compose-ui-test-manifest`
- Material Icons：`material-icons-core`、`material-icons-extended`
- 可选启用：Compose 编译器指标和报告

### NiaFlavor.kt

定义产品风味：
- 维度：`contentType`
- `demo`：带 `.demo` 后缀，用于本地演示
- `prod`：无后缀，用于生产环境

---

## 模块级插件

### AndroidLibraryConventionPlugin.kt

为 Library 模块提供统一配置：
- 应用插件：`com.android.library`、`kotlin-android`、`kotlin-kapt`、`kotlin-parcelize`、`lint`
- 构建配置：`compileSdk = 37`、`minSdk = 24`
- 调用：`configureKotlinAndroid`、`configureFlavors`、`configureDepsAndroid`
- 资源前缀：根据模块路径自动生成

### AndroidApplicationConventionPlugin.kt

为 Application 模块提供统一配置：
- 应用插件：`com.android.application`、`kotlin-android`、`kotlin-kapt`、`kotlin-parcelize`、`lint`
- 构建配置：`compileSdk = 37`、`minSdk = 24`、`targetSdk = 37`
- 调用：`configureKotlinAndroid`、`configureFlavors`、`configureDepsAndroid`、`configureFeatureAndroid`

### AndroidFeatureConventionPlugin.kt

为 Feature 业务功能模块提供聚合配置：
- 应用插件：`nowinandroid.android.library`、`nowinandroid.android.hilt`、`nowinandroid.android.arouter`
- 自动依赖：`basic_lib`、`basic_shared`、`lifecycle-runtime-ktx`、`lifecycle-viewmodel-ktx`
- 测试配置：禁用测试动画

### AndroidFeatureComposeConventionPlugin.kt

为包含 Jetpack Compose 的 Feature 功能模块提供聚合配置：
- 应用插件：`nowinandroid.android.feature`、`nowinandroid.android.library.compose`

### JvmLibraryConventionPlugin.kt

为纯 Kotlin/JVM 模块提供配置（无 Android SDK 开销）：
- 应用插件：`org.jetbrains.kotlin.jvm`、`nowinandroid.android.lint`
- 编译目标：Java 17 / JVM 17
- 基础测试依赖：`junit`、`kotlinx-coroutines-core`

---

## 功能插件

### AndroidKspConventionPlugin.kt

添加 KSP（Kotlin Symbol Processing）注解处理支持：
- 应用插件：`com.google.devtools.ksp`

### AndroidARouterConventionPlugin.kt

添加 ARouter 路由支持：
- 依赖：`arouter`（implementation）、`arouter.compiler`（kapt）
- Kapt 参数：`AROUTER_MODULE_NAME` = 当前模块名

### AndroidEventBusConventionPlugin.kt

添加 EventBus 事件总线支持：
- 依赖：`eventbus`（implementation）、`eventbus.processor`（kapt）
- Kapt 参数：`eventBusIndex` = `My<模块名>EventBusIndex`

### AndroidHiltConventionPlugin.kt

添加 Hilt 依赖注入支持：
- 依赖：`androidx.hilt.android`（implementation）、`androidx.hilt.compiler`（kapt）
- Kapt 配置：`correctErrorTypes = true`
- Hilt 配置：`enableAggregatingTask = false`（兼容 ARouter）

### AndroidRoomConventionPlugin.kt

添加 Room 数据库支持：
- 依赖：`androidx.room`、`room-ktx`、`room-rxjava3`、`room-paging`（implementation）、`room-compiler`（kapt）
- Schema 目录：`$projectDir/schemas`（用于自动迁移）

### AndroidProtobufConventionPlugin.kt

添加 Protocol Buffers 支持：
- 插件：`com.google.protobuf`
- 编译器：`protoc:3.24.0`（lite 模式）
- 依赖：`google.protobuf.javalite`

### AndroidObjectBoxConventionPlugin.kt

添加 ObjectBox 数据库支持：
- 插件：`io.objectbox`
- Kapt 参数：`objectbox.debug = true`、`objectbox.modelPath` = 模型文件路径

### AndroidLintConventionPlugin.kt

配置 Lint 静态分析：
- 生成 XML 和 SARIF 报告
- 检查依赖模块
- 禁用 `GradleDependency` 检查

---

## 工具函数

### ProjectExtensions.kt

提供 `Project.libs` 扩展属性，便捷访问 Version Catalog。

### PrintTestApks.kt

注册打印测试 APK 路径的 Gradle 任务，便于定位测试 APK。

### AndroidInstrumentedTests.kt

禁用没有 `androidTest` 源文件的模块的仪器化测试，避免空测试编译。

### AndroidGreenDaoConventionPlugin.kt

GreenDao ORM 配置（当前已禁用，所有代码已注释）。

---

## 工程构建优化与依赖规范（对齐 Now in Android）

### 1. `gradle.properties` 性能调优体系

参考 Google `nowinandroid` 最佳实践，构建环境划分为 7 大板块，核心优化项包括：
- `org.gradle.parallel=true`：开启多模块并行构建，充分利用多核 CPU 并发编译无依赖的子模块；
- `org.gradle.caching=true`：开启本地任务缓存，避免重复执行代码未改动的编译任务；
- `org.gradle.vfs.watch=true`：开启文件系统监视，跳过增量构建时的全量文件比对；
- `android.enableJetifier=true`：因 ARouter Compiler 1.5.2 内部保留旧 `support.v4.app.Fragment` 类签名校验，保留 Jetifier 对注解处理器字节码进行平滑转换，保障 `@Route` 在 Fragment 上的稳定性。

### 2. Version Catalogs `[bundles]` 依赖成组规范

在 `gradle/libs.versions.toml` 中通过 `[bundles]` 组织高频强关联依赖：
- `androidx-compose`：包含 `ui`、`ui-graphics`、`ui-tooling-preview`、`material3`；
- `androidx-lifecycle`：包含 `runtime`、`viewmodel`；
- `smartrefresh`：包含 `layout`、`header`、`footer`；
- `testing-unit`：包含 `junit`、`kotlinx-coroutines-core`；
- `testing-android`：包含 `androidx-test-ext`、`androidx-test-espresso`。

子模块中可直接声明：
```kotlin
dependencies {
    implementation(libs.bundles.smartrefresh)
}
```

### 3. Compose 稳定性配置（`compose_compiler_config.conf`）

根目录下的 `compose_compiler_config.conf` 由 `AndroidCompose.kt` 自动注入 Compose 编译器扩展：
- 将常用 Java 时间类型（`java.time.*`）、Kotlin 集合类型以及通用基础模型（如 `RouterItem`）显式标记为 `@Stable`；
- 避免 Compose 编译器因无法推断外部类稳定性而产生无意义的重组，提升列表与复杂页面的滑动流畅度。

