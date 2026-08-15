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

## 全局依赖（build-logic/convention）

`AndroidDeps.kt` 中配置了所有模块的公共依赖：

- **基础依赖**：Coroutines、Gson、Guava、AndroidX（Core、Activity、Fragment、AppCompat、ConstraintLayout、RecyclerView、ViewPager2）、BRVAH、SmartRefresh
- **测试依赖**：JUnit、AndroidX Test、Espresso
- **功能模块依赖**：每个功能模块自动依赖其他所有功能模块（通过 `configureFeatureAndroid`）
- **Convention Plugin**：13 个插件统一管理构建配置，详见 `docs/build-logic.md`

## Activity 基类

- `BasicResponseActivity` — RecyclerView 操作列表与内联日志区；页面初始说明使用 `showDescription` 居中展示，离散事件使用 `appendLog`，高频状态使用 `updateLog(key, message)` 原位更新
- `BasicRecyclerActivity` — RecyclerView 操作列表；`showResponse` 与 `showFailure` 弹窗接口已废弃，新页面应提供内联状态或日志区域
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

## 构建

- `./gradlew assembleDebug` — 全量构建
- `./gradlew :modules:<模块名>:assembleDebug` — 单模块构建
- 使用 `--configure-on-demand` 加速构建

## 快速查找

- **新增 Activity**：复制目标模块中已有的 Activity，更新 `@Route`，在 `AndroidManifest.xml` 注册，在入口 Activity 的 `buildRouter()` 中添加路由项
- **新增模块**：复制已有模块结构，在 `settings.gradle.kts` 注册，在 `RouterPath.kt` 添加路由，在 `ModuleActivity.kt` 添加入口
- **路由路径**：`basic/basic_shared/.../RouterPath.kt`
- **主入口列表**：`basic/basic_shared/.../ModuleActivity.kt`
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
