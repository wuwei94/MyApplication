# 工程结构约定

> 分层怎么切、模块怎么摆、路由怎么挂、页面归哪类、改完同步哪些文档。
> 示例页怎么写见 [showcase.md](showcase.md)；交付边界见 [delivery.md](delivery.md)。

## 路由

- 所有路由定义在 `basic/basic_shared/.../RouterPath.kt`
- 格式：`/<模块名>/<Activity名>`
- 大小写：模块名与 Activity 名均用 PascalCase，与 `RouterPath` 对象 / 常量名对齐（例：`/Http/OkHttp`、`/Bluetooth/Main`）
- **既有例外（只读，禁止模仿）**：
  - 基础导航：`RouterPath.Directory_Main = /directory/main`、`RouterPath.Category_Main = /category/main`
  - Fragment 演示路由：`/Fragment/fragment/primary`、`/Fragment/fragment/primary_dark`、`/Fragment/fragment/basic_recycler`（snake_case 历史路径）
- 新增路由一律 PascalCase，禁止对齐上述例外；非 Activity 组件（如 Service 演示页）同样走 `/<模块>/<组件名>` Pascal 形态
- 每个模块有一个 `Main` 路由作为入口 Activity
- 每个 Activity 必须添加 `@Route` 注解

## 分层与依赖方向

本仓是 **Showcase 沉淀工程**，不是严格六边形业务仓。依赖按「谁允许引用谁」约束，而不是一条死直链：

```
app（壳）
  └─ modules（按技术域的 Showcase 模块）
       ├─ basic_lib / basic_shared（基类、路由、日志脚手架）
       ├─ 其它 basic_*（按需：repo/network/datastore/…）
       └─ libs（按需：技术封装）

basic 数据栈（model → database/network/datastore → repo → sync）
  └─ 可依赖更底层 basic_* 与所需 libs（如 basic_network → lib_okhttp / lib_retrofit）

libs（技术封装）
  └─ 第三方库；库之间可单向组合（lib_retrofit → lib_okhttp）
  └─ 不依赖 modules / app；不依赖 basic_*（保持可独立复用）

basic_model
  └─ 纯 JVM，零 Android 依赖
```

| 层 | 允许依赖 | 禁止 |
|---|---|---|
| `app` | 全部 `modules`（经 convention 自动注入）、`basic_*`、按需 `libs` | — |
| `modules/*` | `basic_lib` / `basic_shared`、按需其它 `basic_*` 与 `libs` | 依赖其它 `modules`、依赖 `app` |
| `basic` 数据栈（`basic_network` / `basic_repo` / `basic_sync` 等） | 更底层 `basic_*`、按需 `libs` | 依赖 `modules`、`app`；`basic_model` 不得依赖 Android / `libs` |
| `libs/lib_*` | 其它 `libs`、第三方库 | 依赖 `modules`、`app`、`basic_*` |
| `basic_lib` / `basic_shared` | 第三方、彼此及更底层能力 | 依赖 `modules`、`app` |

- **`modules` 互不依赖**：Showcase 页通过 `libs` / `basic_*` 复用能力，不 import 兄弟模块类型。
- **`basic_model` 保持纯 JVM**：领域实体、版本游标等，不得引入 `android.*`。
- **依赖声明**：使用 typesafe project accessors（`projects.basic.basicLib`、`projects.libs.libOkhttp`）；禁止在模块脚本手写 `project(":basic:basic_lib")` 字符串路径。
- **Isolated Projects 已开启**：配置期不得读其它 project 的状态；跨模块能力只能靠依赖与公开 API。

## 模块与包名地图

| 目录 | namespace / 源码包 | 源码落点 | 资源前缀 |
|---|---|---|---|
| `app` | `com.example.william.my.application` | `src/main/java` | `app_`（如有） |
| `modules/module_<域>` | `com.example.william.my.module.<域>` | `src/main/java` | 见下表 |
| `libs/lib_<能力>` | `com.example.william.my.core.<能力>` | `src/main/java` | 按需 |
| `basic/basic_lib` | `com.example.william.my.core.base` | `src/main/java` | `base_` |
| `basic/basic_shared` | `com.example.william.my.basic.basic_shared` | `src/main/java` | `shared_` |
| `basic/basic_server` | `com.example.william.my.basic.basic_server` | `src/main/java` | 与模块职责一致 |
| 其它 `basic_*` | `com.example.william.my.basic.<模块名>` 或对应 `core.*` | `src/main/java` | 与模块职责一致 |
| `flutter/flutter_libs/*` | 各 Flutter package 自有 `pubspec` 名 | Dart `lib/` | 不适用 Android 资源前缀 |

说明：`docs/05-catalog/libs.md` 的「库总览」会同时收录 Android `libs/lib_*` 与 Flutter `flutter/flutter_libs/*`（便于检索）；**依赖声明与包名地图仍以本表目录边界为准**，Flutter package 不得写入 Android `libs/` 的 `projects.libs.*` 访问器。

- **Kotlin 源码统一放 `src/main/java`**（与现仓一致），不要新建 `src/main/kotlin`（`build-logic` / `lint` 等 JVM 工程除外）。
- 每个模块：`build.gradle.kts` + `AndroidManifest.xml`；namespace 与包路径一致。
- 资源引用：本仓开启非传递 R 类（`android.nonTransitiveRClass=true`），`R.xxx` **只解析本模块声明的资源**；引用 `basic_shared` / `basic_lib` 或其他依赖库的资源时必须写全其 R 类。

### modules 资源前缀

默认 **`<模块名缩写或域名>_`**，与模块职责对齐；新建模块默认用去掉 `module_` 后的域名（如 `module_foo` → `foo_`）。

| 模块 | 前缀 | 备注 |
|---|---|---|
| 多数模块（`module_anim`、`module_http` 等） | `<域名>_` | 如 `anim_`、无独立 res 则可不建 |
| `module_widget` | `ui_` | 历史 UI 控件集，已定型 |
| `module_widget_custom` | `demo_` | 历史自定义控件集，已定型 |
| `module_system_service` | `systemservice_` | 域名无下划线拼接 |

新建模块不要模仿 `ui_` / `demo_` 例外；只在既有模块内追加资源时沿用该模块已定前缀。

### modules 源码分包（能力包 → 角色包）

多技术并列的 Showcase 模块，源码分包统一为：

```
module_<域>/
├── XxxMainActivity.kt          # 模块入口，仍在 namespace 包根
└── <capability>/               # 一级：技术能力（datastore / mmkv / nordic_ble / retrofit …）
    ├── activity/               # 二级：该能力的示例 Activity
    │   └── XxxActivity.kt
    └── data/                   # 二级：该能力的数据 / 实现支撑
        └── XxxSupport.kt
```

1. **一级按技术能力**：同一能力的多库、多实现、多样本页归入同一 `<capability>/`。
2. **二级按角色**：示例 Activity 进 `<capability>/activity/`；支撑实现进 `<capability>/data/`。架构样板模块可在能力包内保留既有角色子包（`viewmodel/`、`usecase/`、`contract/`、`presenter/` 等），UI 宿主仍归 `activity/`（Fragment 可用 `fragment/`）。
3. **入口 Activity** 固定在模块 namespace 包根，不进入能力包。
4. **跨能力共享代码**（通用 helper、工具、自定义 View）放模块级角色包（`helper/`、`utils/`、`view/`、`ui/`），禁止塞进单一能力包。
5. **单能力模块**（无多技术横评轴）可只有包根入口 + 模块级 `activity/`，不必空造能力包。
6. **包名与 Manifest** 与源码路径一致；`@Route` 路由字符串不随分包变化。

禁止：

- 能力包内 Activity 与支撑代码平铺混放
- 顶层 `activity/` 下再按技术分包（技术轴必须在一级，如 `retrofit/activity/` 而非 `activity/retrofit/`）
- 同一模块内示例 Activity 有的在 `activity/`、有的在能力包根

锚点：`modules/module_storage`（`datastore/activity|data`、`mmkv/activity|data`）。

## 分类判据与模块边界

示例模块的归类遵循「主题优先、来源标注、职责分明」原则：

- **一级按技术主题**：模块挂到 `Category` 定义的 10 大技术领域分组（UI 交互 / 多媒体 / 网络通信 / 数据存储 / 系统能力 / AI 与机器学习 / 架构与工程 / Kotlin & Jetpack / Compose & Flutter / Sample & Feature）。
- **二级按技术来源或主题**：同一能力的不同实现按来源分组，不同技术点按主题分组；分组承载**技术主题**、入口承载**模块**，分组下只有 1 个模块属正常粒度，不因条目少而拍平。
- **媒体域聚合**：`module_image_loader` / `module_media` / `module_graphics` / `module_gpuimage` 同属「多媒体」，按「图片加载 / 相机采集 / 图像处理」分组。
- **AI 域聚合**：`module_ml` 挂「AI 与机器学习」，不属「系统能力」。
- **底层能力 vs 第三方 UI 控件**：
  - `module_graphics`：原生 RenderEffect / RenderScript；与 `module_gpuimage`（OpenGL 滤镜）互补。
  - `module_media`：CameraX 采集；PictureSelector 等复合选择器归 `module_widget_thirdparty`。
  - `module_image_loader`：图片加载管道；PhotoView 等手势控件归 `module_widget_thirdparty`。
  - `module_widget_thirdparty`：通用可复用第三方 View/复合 UI；EasyFloat 悬浮窗按能力归 `module_system_service`。
  - `module_chart`：独立图表主题，一库一主题。
- **Jetpack 组件按主题归位**：通用 Lifecycle/Paging/ViewModel → `module_jetpack`；Room → `module_database`；DataStore → `module_storage`；CameraX → `module_media`；Hilt → `module_di`；WorkManager → `module_scheduler`；App Startup / Baseline Profiles / AsyncLayoutInflater / ConcatAdapter / DiffUtil → `module_performance`。
- **Sample & Feature**：`module_sample` 为单点技巧与底层探索；`module_feature` 为脱敏业务场景（转盘、麦位、裁剪等），按**页面叙事形态**归属，不按「公司用过」。
- **系统服务 vs 安全**：通知/权限/悬浮窗 → `module_system_service`；Keystore 密钥与签名 → `module_security`。

## 全局依赖（build-logic/convention）

- 基础依赖、测试依赖由 `AndroidDeps.kt` 统一注入。
- **`configureFeatureAndroid`**：由 app 侧 convention 把全部 `:modules:module_*` 以 `implementation` 注入壳工程，模块脚本无需再声明彼此；**这不是模块互依**，模块之间仍须解耦（见上文分层表）。
- SDK / flavor / lint / spotless / ARouter / Hilt 等只走 Convention Plugin，模块 `build.gradle.kts` 只写 `plugins { alias(...) }` + 差异依赖。
- 外部依赖版本一律进 `gradle/libs.versions.toml`，禁止在模块 kts 内硬编码版本。
- Convention Plugin 见 `docs/02-engineering/build-logic.md`。

## Application 双初始化

两种 Application 方案经 Manifest `android:name` 切换（同一时刻只有一种生效），模块若提供启动初始化必须**双轨对齐**：

| 方案 | 入口 | 模块接入 |
|---|---|---|
| 手动 | `app.App` | 提供 `XxxApp : BaseAppInit`，并在 `App.initApp()` 中 `registerAppInit(...)` |
| Hilt | `app.AppHilt` | 提供 `XxxInitImpl : IAppInit` + `@XxxInit` 限定符 + `XxxModule`，并纳入 `AppHilt.onCreate()` 调用顺序 |

只接一条链路会导致另一方案下模块初始化静默失效。已接入模块见根 `README.md`。

## Activity 基类

| 基类 | 用途 | 落点 |
|---|---|---|
| `BasicControlActivity` | 纯操作列表（`buildList` + `onRecyclerClick`） | `basic_shared/.../activity/` |
| `BasicResponseActivity` | 上日志区 + 下操作列表；`showDescription` / `appendLog` / `updateLog` | `basic_shared/.../activity/` |
| `BasicImageActivity` | 上图片 + 下操作列表 | `basic_shared/.../activity/` |
| `BasicLayoutActivity` | 上动态 View 容器 + 下操作列表 | `basic_shared/.../activity/` |
| `BasicRecyclerActivity` | 上数据列表 + 下操作列表 | `basic_shared/.../activity/` |
| `RouterRecyclerActivity` | 路由项列表（模块入口） | `basic_shared/.../activity/` |
| `BaseVBActivity<VB>` | ViewBinding | `basic_lib/.../ui/activity/` |
| `BaseFragmentActivity` | Fragment 宿主 | `basic_lib/.../ui/activity/` |

Showcase 控制台族基类在 `basic_shared`；架构基类（`BaseActivity` / `BaseMvpActivity` / `BaseVMActivity` 等）在 `basic_lib/.../ui/activity/`，二者不可混指。

## 现代 Kotlin 与命名约束

禁止 `m` 前缀、状态只读契约、就近类型安全常量等规则见 [style.md](style.md)「命名与声明」。本文件不重复维护命名表。

## 协程调度器与作用域（对齐 NiA）

1. **架构样板 / 可复用业务逻辑**（UseCase、Repository、库内调度）禁止业务类散落硬编码 `Dispatchers.IO/Default`：
   - **Hilt 方案**：经 `@Dispatcher(AppDispatchers.IO/Default/Main)` 注入 `CoroutineDispatcher`。
   - **非 Hilt / 默认参数**：允许在构造参数写 `Dispatchers.IO` 等作为**唯一默认值**（如 `FlowUseCase`），调用方仍不各自裸写。
   - 以协程 API 本身为演示主题的 Showcase 页（如 `module_kotlin`）属被演示对象，可直接使用 `Dispatchers.*` 讲调度切换。
2. **跨生命周期作用域**：
   - **Hilt**：注入 `@ApplicationScope`（`CoroutineScopesModule` 提供 `SupervisorJob + Default`）。
   - **手动 `App`**：仓内尚无对应限定符单例；若模块需要全局作用域，应在 `App`/`BaseApp` 层持有受控 `CoroutineScope(SupervisorJob() + dispatcher)`，禁止 `GlobalScope`。
3. UI 收集 Flow 必须用 `collectWithLifecycle` / `launchAndRepeatWithLifecycle(STARTED)`（`basic_lib` `LifecycleExtensions`）。

> 锚点类型：`AppDispatchers`（枚举）、`@Dispatcher` / `@ApplicationScope`（Hilt 限定符）——它们不是可直接当调度器/作用域使用的全局对象。

## 改动范围

- 默认实现正常路径与常见失败路径，不为极少数/纯理论输入加校验、兼容层或公共 API。
- 交付物形态见 [delivery.md](delivery.md)。
- 跨库对齐以普通使用契约为准，不追求畸形输入逐项一致。
- 安全、数据损坏、资源泄漏、阻断正确性的问题不受上述限制。
- 任务边界内允许规模化对齐；「最小切片」约束的是无关附赠（见 [style.md](style.md)）。

## 历史/废弃示例内容

以「已废弃 API」为**被演示主题**的页面保留并标注，属示例内容：

1. 废弃能力是演示对象，不是顺手兼容壳；
2. KDoc 按 [showcase.md](showcase.md) 废弃页型标注。

不在此列：自有库未清理兼容入口、移植残留死代码、「旧 X 已移除」式复盘。

## 问题排查与架构治理（根因治理）

- **从根因治理，拒绝表面打补丁**：分析底层机理后从架构与数据流根治；需要兜底才能运转说明设计有缺陷。
- **严禁**：magic number 偏移、`postDelayed` 瞎猜、强行截断/伪数据拼接、空 catch 绕过。

## 构建与质量命令

- **Android 侧本地验证默认带 `-PenableFlutter=false`**，与 pre-push / CI 口径一致；改 Flutter 时再打开（见 `gradle.properties` `enableFlutter`）。
- 变体：打包 / Lint 用 `prod`，单测用 `demo`（如 `lintProdDebug`、`testDemoDebugUnitTest`），不要只跑无 flavor 的 `assembleDebug` 后判定通过。
- `./gradlew :<模块路径>:assembleDebug`（或对应 flavor 变体）
- `./gradlew spotlessCheck` / `spotlessApply` / `:<模块路径>:spotlessApply`
- `./gradlew :<模块路径>:testDemoDebugUnitTest`
- `./gradlew lintProdDebug`（全量或模块级，按任务范围）
- 新增/删除依赖后跑 Dependency Guard，必要时更新 baseline
- `./gradlew generateModulesGraph`（需 `-Dorg.gradle.isolated-projects=false`）
- `./gradlew :lint:test` / `:modules:module_compose:recordRoborazziDemoDebug` 等见 `docs/02-engineering/engineering.md`
- 钩子：`./tools/install-git-hooks.sh`（详见 [git.md](git.md)）
- Configuration Cache 当前因 Flutter / ObjectBox 关闭；Isolated Projects 已开启，勿在配置期耦合其它 project。

## 快速查找

- **新增 Activity**：复制已有 Activity → 更新 `@Route` → Manifest 注册 → `RouterPath` 加常量 → 入口 `buildRouter()` 加项 → 按「文档同步」更新 catalog
- **新增模块**：复制模块 → `settings.gradle.kts` → `RouterPath.kt` → `CategoryActivity` / `Category.kt` → 核对分层依赖方向与包名地图
- **提供 AppInit**：手动 `XxxApp` + `registerAppInit` 与 Hilt `XxxInitImpl` + Module 双轨齐全
- 路由：`basic/basic_shared/.../RouterPath.kt`
- 目录/分类：`DirectoryActivity.kt` / `CategoryActivity.kt`
- Showcase 基类：`basic/basic_shared/.../activity/`
- 架构基类：`basic/basic_lib/.../ui/activity/`

## 秘密与本地配置

- API Key、调试用私密 URL 等**禁止硬编码进源码**；经 `local.properties` → BuildConfig 注入（锚点：`Constants.DeepSeek_ApiKey`）。
- `local.properties` 不入库；提供 `local.properties.example` 时保持键名一致。

## 文档同步

| 修改内容 | 需更新 |
|---------|--------|
| 新增/删除/移动 Activity | `docs/05-catalog/modules.md` + 根 `README.md` |
| 新增/删除模块 | `modules.md` + 根 `README.md` + `docs/README.md` 模块数量表述（如有） |
| 修改模块职责 | `modules.md` + 根 `README.md` |
| 修改库封装 | `libs.md` + 专题文档 + 根 `README.md` |
| 改 OkHttp / Retrofit / Ktor 等网络栈 | `docs/04-domains/network.md` + `libs.md` + 根 `README.md` |
| 改 Convention Plugin | `docs/02-engineering/build-logic.md` |
| 改 UI 尺寸 / 设计 token | [design.md](design.md) + `basic_shared` `dimens.xml` 对应说明 |
| 改本文件分类/结构约定 | 本文件 + 本目录 [README.md](README.md) |
| 改示例页/平行约定 | [showcase.md](showcase.md) |
| 改架构或技术栈 | 根 `README.md` |
| 改 Flutter 侧约定 / Demo 目录 | `flutter/flutter_demo/AGENTS.md` 及该仓 `docs/` |

说明：`AGENTS.md` 为 L0 运行时硬契约，**不**承载模块清单；模块增删不必回写 `AGENTS.md`，除非分层不变量或交付纪律本身变化。
