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
├── AndroidARouterConventionPlugin.kt           # ARouter 路由插件
├── AndroidEventBusConventionPlugin.kt          # EventBus 事件总线插件
├── AndroidHiltConventionPlugin.kt              # Hilt 依赖注入插件
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
- 工具库：`google-gson`、`google-guava`、`google-material`
- AndroidX：`core-ktx`、`activity-ktx`、`fragment-ktx`、`appCompat`、`constraintLayout`、`recyclerView`、`viewPager2`
- UI 库：`brvah`、`smartrefresh-layout`、`smartrefresh-header`、`smartrefresh-footer`
- 测试：`junit`、`androidx-test-ext`、`androidx-test-espresso`

**功能模块依赖**（通过 `configureFeatureAndroid`）：
- App 模块自动依赖所有功能模块（module_ui、module_anim、module_widget、module_rx_retrofit 等 18 个）

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
- 构建配置：`compileSdk = 36`、`minSdk = 24`
- 调用：`configureKotlinAndroid`、`configureFlavors`、`configureDepsAndroid`
- 资源前缀：根据模块路径自动生成

### AndroidApplicationConventionPlugin.kt

为 Application 模块提供统一配置：
- 应用插件：`com.android.application`、`kotlin-android`、`kotlin-kapt`、`kotlin-parcelize`、`lint`
- 构建配置：`compileSdk = 36`、`minSdk = 24`、`targetSdk = 36`
- 调用：`configureKotlinAndroid`、`configureFlavors`、`configureDepsAndroid`、`configureFeatureAndroid`

---

## 功能插件

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
