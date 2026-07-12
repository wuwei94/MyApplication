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

- `BasicResponseActivity` — 简单文本响应展示
- `BaseVBActivity<VB>` — ViewBinding 基类
- `BaseFragmentActivity` — Fragment 宿主
- `RouterRecyclerActivity` — RecyclerView 列表（带路由项）

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
| 修改库封装 | `docs/libs.md` + `README.md` |
| 修改 Convention Plugin | `docs/build-logic.md` |
| 修改关键约定 | `docs/conventions.md` |
| 修改架构或技术栈 | `README.md` |
