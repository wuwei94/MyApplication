# 测试体系

> 本项目测试三大支柱：Turbine + 手写测试替身（单测）、自定义 Lint 测试命名规则（规范）、Roborazzi 截图测试（UI 回归）。

## 总览

| 支柱 | 定位 | 落点 | 验证命令 |
|------|------|------|----------|
| Turbine + Fake | Flow / RxJava 等数据流的单测断言 | `modules/module_reactive`（样板） | `./gradlew :modules:module_reactive:testDemoDebugUnitTest` |
| 测试命名 Lint | 全工程测试命名的机械校验 | `lint` 模块（自定义规则） | `./gradlew :<模块>:lintDemoDebug` |
| Roborazzi 截图 | Compose UI 像素级回归 | `modules/module_compose`（样板） | `./gradlew :modules:module_compose:verifyRoborazziDemoDebug` |

## 一、Turbine + 手写测试替身

### 原则

1. **不引入 MockK / Mockito**。所有测试替身手写（Fake），是一个真实可用的内存实现：
   - 用例按需预置发射序列与失败时机；
   - 替身自带记录（如 `subscriptionCount`）供断言，无需 `verify { }` 桩语句；
   - 不会因为签名变化而静默失效（Mock 桩失配会在运行期才暴露）。
2. **Turbine 断言必须消费完整事件流**（`awaitComplete` / `awaitError`），否则报 Unconsumed events——这保证了「冷流是否终止」也被验证。
3. **示例代码与被测代码同构**：Activity 里的操作符管道抽取为 `samples` 包下的顶层纯函数（返回 Flow / Observable），页面只做收集与渲染；同一份管道直接被单测断言，不需要为测试复制逻辑。

### 结构约定（以 module_reactive 为例）

```
samples/
  NumberSource.kt           # 数据源接口（错误/重试类示例的测试接缝）
  FlowOperatorSamples.kt    # Flow 操作符管道（顶层纯函数）
  RxJavaOperatorSamples.kt  # RxJava 操作符管道（顶层纯函数）
src/test/
  samples/
    FakeNumberSource.kt         # 手写测试替身：注入失败时机 + 订阅计数
    FlowOperatorSamplesTest.kt  # Turbine 逐项断言
    NumberSourcePipelineTest.kt # 错误恢复 / 重试场景（Fake 驱动）
    RxJavaOperatorSamplesTest.kt # Observable 经 asFlow() 复用 Turbine 断言
```

### 关键取舍

- **错误恢复 / 重试必须走数据源接缝**：`catch` / `retry` 的语义依赖上游「可重复订阅」，字面量 Flow 无法表达失败时机与订阅次数，因此以 `NumberSource` 函数接口为入参。
- **RxJava 测试复用 Turbine**：通过 `kotlinx-coroutines-rx3` 的 `asFlow()` 把 Observable 转 Flow，两个框架共用一套断言风格，便于横向对照。
- **依赖集中在版本目录**：`testing-unit` bundle（JUnit + coroutines-test + Turbine）已由 `AndroidDeps.kt` 注入所有 Android 模块，模块无需单独声明。

## 二、测试命名 Lint 规则

### 规则（`lint` 模块，`TestNamingDetector`）

| 规则 ID | 检查内容 | 级别 |
|---------|----------|------|
| `TestClassName` | 含 `@Test` 方法的类必须以 `Test` 结尾 | WARNING |
| `TestMethodName` | `@Test` 方法必须为 `被测对象_场景_预期结果` 下划线式 | WARNING |

命名格式：小写字母开头、下划线分段（至少两段），段内允许驼峰。例：`mapFlow_lowercaseInput_emitsUppercase`。

**为什么不用反引号中文方法名**：规则需可被 Lint 机械校验；中文语义写进 KDoc 与断言消息，方法名保持 CI 日志可直接读出「哪个对象的什么场景期望什么」。

### 生效方式

- `AndroidDeps.kt` 已为所有 Android 模块自动挂 `lintChecks(project(":lint"))`；
- `AndroidLintConventionPlugin` 开启 `checkTestSources`，`src/test` 与 `src/androidTest` 均受检；
- 判定「是不是测试类」看有无 `@Test` 方法，不依赖目录名，因此 Fake / 测试工具类不会被误报。

### 维护注意

- lint-api 版本（`androidLint` in `libs.versions.toml`）**必须与 AGP 内置 Lint 对齐**：AGP 9.1.0 → Lint 32.1.0。AGP 升级时同步调整，否则规则无法加载；
- 规则自身的单测在 `lint/src/test`（`TestLintTask` 喂源码断言，含合规 / 违规双向用例），运行 `./gradlew :lint:test`。

## 三、Roborazzi 截图测试

### 工作流

```bash
# 录制基准（首次接入或 UI 有意变更时）
./gradlew :modules:module_compose:recordRoborazziDemoDebug

# 回归校验（CI / 日常开发，差异超阈值即失败）
./gradlew :modules:module_compose:verifyRoborazziDemoDebug
```

基准图入库于 `modules/module_compose/src/roborazzi/screenshots/`，PR 中可直接 review 像素级差异。

### 约定

1. **被测 Composable 必须是顶层函数**：Activity 成员 Composable 无法被测试直接调用，UI 与页面挂载分离（见 `ui/component/TextExample.kt` 与 `TextActivity`）。
2. **确定性优先**：
   - `qualifiers` 固定（`w411dp-h891dp-420dpi`），尺寸不随环境漂移；
   - 主题关闭 `dynamicColor`（动态取色走系统资源读取，Robolectric 下不稳定）；
   - `@GraphicsMode(NATIVE)` 启用 Robolectric 原生图形，保证渲染与真机一致；
   - sdk 固定（34），避免多 SDK 下字体度量差异。
3. **按需引入**：`testing-screenshot` bundle 只在截图测试模块声明（`testImplementation(platform(compose-bom))` + bundle），不进全局公共依赖，避免拖慢其它模块编译。
4. **覆盖矩阵**：每个组件至少亮 / 暗主题两张（`MyApplicationTheme(darkTheme = ...)`）。

### 已知边界

- Robolectric 4.16 无 `RobolectricDeviceQualifiers` 常量类，用 qualifiers 字符串替代；
- Roborazzi 1.73 兼容 AGP 9（1.56.0 起修复 `Variant.unitTest` 废弃 API），升级 AGP 时注意对应关系。

## 命令速查

```bash
# 单测（以 module_reactive 为例）
./gradlew :modules:module_reactive:testDemoDebugUnitTest

# Lint（含测试命名规则）
./gradlew :modules:module_reactive:lintDemoDebug

# 截图测试
./gradlew :modules:module_compose:recordRoborazziDemoDebug   # 录制
./gradlew :modules:module_compose:verifyRoborazziDemoDebug   # 校验

# Lint 规则自身的单测
./gradlew :lint:test
```

> 注意：工程带 demo/prod 风味，测试任务名需带风味前缀（`testDemoDebugUnitTest` / `lintDemoDebug`）。
