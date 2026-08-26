# 关键约定

> 所有模块和 Activity 必须遵守的规则。

## 路由

- 所有路由定义在 `basic/basic_shared/.../RouterPath.kt`
- 格式：`/<模块名>/<Activity名>`
- 每个模块有一个 `Main` 路由作为入口 Activity
- 每个 Activity 必须添加 `@Route` 注解

## 模块结构

- 每个模块：`build.gradle.kts` + `AndroidManifest.xml` + 包路径在 `modules/` 下
- 命名空间：`com.example.william.my.module.<模块名>`
- 资源前缀：`<模块名>_`
- 依赖：必须依赖 `basic_lib` 和 `basic_shared`

## 分类判据与模块边界

示例模块的归类遵循「主题优先、来源标注、职责分明」原则，新增模块或示例页前先按此判据确定归属：

- **一级按技术主题**：模块挂到 `DirectoryActivity` 的 8 大技术领域分组（UI 交互 / 网络通信 / 数据存储 / 系统能力 / 架构与工程 / Kotlin & Jetpack / Compose & Flutter / Sample & Feature）。
- **二级按技术来源**：分组内用「系统原生 / Jetpack / 第三方」标注示例来源，便于横向对比。
- **底层能力 vs 第三方 UI 控件**：
  - `module_media`：聚焦系统原生 API 与硬件能力（CameraX 拍照/录像、Intent 系统裁剪）；第三方复合 UI 选择器（如 `PictureSelector`）归入 `module_widget_thirdparty`。
  - `module_imageloader`：聚焦网络图片加载管道与引擎（Coil / Glide / `lib_imageloader` 加载、缓存与内核切换）；手势缩放/平移 View 控件（如 `PhotoView`）归入 `module_widget_thirdparty`。
  - `module_widget_thirdparty`：集中收纳第三方可复用 View/ViewGroup 控件与复合 UI 库（Banner、EasyFloat、ShadowLayout、SwipeLayout、RealtimeBlurView、CityPicker、PickerView、PictureSelector 以及页面多状态管理 `LoadSir`）。
- **Jetpack 组件按主题归位原则**：
  - `module_jetpack` 专门承载**未被具体技术领域模块吸纳的通用 Jetpack 架构与生命周期数据流组件**（如 Lifecycle、Paging、ViewModel 等），且**不包含 UI 控件**。
  - 具有明确技术领域的 Jetpack 组件必须归入对应主题模块：
    - `Room` → `module_database`（数据存储）
    - `DataStore` → `module_storage`（键值存储）
    - `CameraX` → `module_media`（多媒体硬件能力）
    - `Hilt` → `module_di`（依赖注入）
    - `WorkManager` → `module_scheduler`（后台任务调度）
    - `App Startup`、`Baseline Profiles`、`AsyncLayoutInflater`、`ConcatAdapter`、`DiffUtil` → `module_performance`（启动、布局解析与列表渲染性能优化）
- **探索与实战模块边界（Sample & Feature）**：
  - `module_sample`（技术技巧与底层探索）：收纳不依赖特定业务场景的单点技术技巧、底层 API 机制探索与实验性代码（如 Hook 反射、自定义 Typeface 等）。保持轻量独立，不污染通用架构模块。
  - `module_feature`（实战业务场景脱敏）：收纳从公司真实项目中抽离、脱敏出的典型复合业务场景（如抽奖转盘、麦位动画等）。展示端到端的真实业务落地能力（UI + 业务逻辑 + 状态联动），不追求强行抽象为纯通用控件。

## 全局依赖（build-logic/convention）

`AndroidDeps.kt` 中配置了所有模块的公共依赖：

- **基础依赖**：Coroutines、Gson、Guava、Material、AndroidX（Core、Activity、Fragment、AppCompat、ConstraintLayout、RecyclerView、ViewPager2）、BRVAH、SmartRefresh
- **测试依赖**：JUnit、AndroidX Test、Espresso
- **功能模块依赖**：每个功能模块自动依赖其他所有功能模块（通过 `configureFeatureAndroid`）
- **Convention Plugin**：13 个插件统一管理构建配置，详见 `docs/build-logic.md`

## Activity 基类

- `BasicControlActivity` — 纯操作/控制列表类示例 Activity 基类
- `BasicResponseActivity` — 上方内联日志区与下方操作控制列表；页面初始说明使用 `showDescription` 居中展示，离散事件使用 `appendLog`，高频状态使用 `updateLog(key, message)` 原位更新；统一由下方 `buildList` + `onRecyclerClick` 触发操作
- `BasicImageActivity` — 上方图片展示区与下方操作控制列表；支持 `showImage` 主线程更新展示
- `BasicLayoutActivity` — 上方空白动态 View 容器（`mContainer` / `ConstraintLayout`）与下方操作控制列表；支持 `setView` / `addView` 动态挂载、替换与展示自定义 View 或异步渲染结果
- `BasicRecyclerActivity` — 上方数据展示列表（`mContainer` / `basics_response_container` 内含 `mDataRecycler`，高度 0dp 自适应撑满）与下方操作控制列表（固定高度 300dp）
- `BaseVBActivity<VB>` — ViewBinding 基类
- `BaseFragmentActivity` — Fragment 宿主
- `RouterRecyclerActivity` — RecyclerView 列表（带路由项）

## 示例页面

示例页面的首要目标是让读者快速看清库的入口、参数、返回值和回调，而不是展示一套页面级任务编排器。

- 一个操作列表项应直接对应一个命名明确的示例方法，库调用和回调尽量放在该方法附近，可参考 `RxRequestActivity`。
- 页面只保留演示所需的最小状态。取消和生命周期管理优先使用库返回的 `Disposable`、`CompositeDisposable` 或平台标准能力。
- 不要为了串行切换操作在页面引入 `pending action`、operation ID、active 标记、重复的 `runAfter...` / `begin...` 包装层，除非该页面的示例目标就是任务编排。
- 物理 I/O 终止、资源租约、并发限制和取消一致性属于库的职责。多个示例页重复实现同类编排时，应先改进库 API，再简化页面调用。
- 单任务与批量任务可以使用不同的数据模型，但公开调用结构和回调命名应尽量一致；不能为了表面一致隐藏必要的业务差异。
- 离散的开始、成功、失败和取消事件使用 `appendLog()`；高频进度使用 `updateLog(key, message)`，不得持续追加 RecyclerView 条目或历史日志。

## 改动范围

- 默认实现正常业务路径和常见失败路径，不为极少数、违反既有协议或纯理论输入增加额外校验、分支、异常类型、兼容层或公共 API。
- 跨平台或跨库对齐以普通使用契约为准，不追求对畸形响应、越界数据和底层实现细节逐项完全一致。
- 只有用户明确要求、官方契约明确保证，或项目中已有真实使用依据时，才扩展少见场景。
- 安全问题、数据损坏、资源泄漏和会影响常规功能正确性的情况不受上述限制。

## 构建

- `./gradlew assembleDebug` — 全量构建
- `./gradlew :modules:<模块名>:assembleDebug` — 单模块构建
- 使用 `--configure-on-demand` 加速构建

## 快速查找

- **新增 Activity**：复制目标模块中已有的 Activity，更新 `@Route`，在 `AndroidManifest.xml` 注册，在入口 Activity 的 `buildRouter()` 中添加路由项
- **新增模块**：复制已有模块结构，在 `settings.gradle.kts` 注册，在 `RouterPath.kt` 添加路由，在 `CategoryActivity.kt` 对应分类中添加入口
- **路由路径**：`basic/basic_shared/.../RouterPath.kt`
- **主入口列表**：`basic/basic_shared/.../DirectoryActivity.kt`（目录）/ `CategoryActivity.kt`（分类）
- **基类**：`basic/basic_lib/.../activity/`

## 文档同步

修改代码时，必须同步更新以下文档：

| 修改内容 | 需更新的文档 |
|---------|------------|
| 新增/删除/移动 Activity | `docs/modules.md` + `README.md` |
| 新增/删除模块 | `docs/modules.md` + `README.md` + `AGENTS.md` |
| 修改模块职责 | `docs/modules.md` + `README.md` |
| 修改库封装 | `docs/libs.md` + 对应专题文档 + `README.md` |
| 修改 OkHttp、Retrofit、Retrofit Rx 或 Ktor | `docs/network.md` + `docs/libs.md` + `README.md` |
| 修改 Convention Plugin | `docs/build-logic.md` |
| 修改关键约定 | `docs/conventions.md` |
| 修改架构或技术栈 | `README.md` |
