# 现代架构范式（对标 NiA 的工程落地）

> 本文为《现代 Android 工程化实践》的分册之一（由原单篇长文按主题拆分而来），梳理以 Google Now in Android (NiA) 为参照、已在或规划在本工程落地的**现代架构解耦与通信范式**：Offline-First、环境监视器、后台同步契约、API-Impl 模块隔离、Navigation 3、Material 3 Adaptive 与可插拔能力接口。
>
> **与本组其他文档的分工**：架构**模式**的演进与选型决策树见 [architecture.md](architecture.md)（基于 `module_arch` 的六种范式教学）；组件化**路由演进与模块解耦选型**见 [modularization.md](modularization.md)；本文聚焦「具体范式如何在当前工程代码中落地」。

---

## 1. 离线优先（Offline-First）与单一事实源（SSOT）架构 【已落地】

现代 Android 架构坚决摒弃“网络直接驱动 UI”的脆弱模式，采用本地数据库驱动 UI 的响应式闭环：

```mermaid
flowchart LR
    A["Remote API 网络后端"] -->|增量拉取 Sync| B["Room 本地数据库 (SSOT)"]
    B -->|响应式 Flow 流| C["Repository / UseCase"]
    C -->|UIState 流| D["UI (Compose / Activity)"]
    D -.->|用户写操作 (乐观更新)| B
    B -.->|后台异步同步写| A
```
* **核心原则**：UI 永远只观察 Room 数据库吐出的冷流（Flow），网络请求成功后仅写入数据库，由数据库的变动天然触发 UI 响应；
* **离线可用**：即使断网，App 依然立即可用并展现最新的持久化数据，零加载白屏。在 [`basic/basic_repo`](file:///e:/StudioProjects/MyApplication/basic/basic_repo) 与 [`modules/module_arch:ssot`](file:///e:/StudioProjects/MyApplication/modules/module_arch) 已完整落地。

---

## 2. 响应式系统环境监视器体系（Environment Monitors）

现代应用 UI 不仅需要感知业务数据，还需要对操作系统外部环境（网络连接、系统时区、电量状态）保持实时敏锐的响应式观察。

#### 网络状态监视器（`NetworkMonitor`）`【已落地】`
在 [`basic/basic_lib`](file:///e:/StudioProjects/MyApplication/basic/basic_lib) 提供统一的网络感知接口：
```kotlin
interface NetworkMonitor {
    val isOnline: Flow<Boolean>
}
```
基于系统 `ConnectivityManager.NetworkCallback` 实现，向上层暴露热状态流。UI 结合 `repeatOnLifecycle` 收集状态，离线时顶部显示离线横幅提示，在线时自动唤醒数据重试与后台同步。

#### 系统时区监视器（`TimeZoneMonitor`）`【演进规划 - 待落地】`
* **问题痛点**：跨国用户飞行、系统时区切换或夏令时切换时，若 App 未重启，UI 上的格式化时间戳（如“发布于 2 小时前”、“2026-09-08 13:00”）将出现时区漂移或计算偏差；在截图测试时，不同机器的默认时区差异也会导致断言失败；
* **设计契约**：
  ```kotlin
  interface TimeZoneMonitor {
      val currentTimeZone: Flow<TimeZone>
  }
  ```
* **落地机理**：
  1. **生产实现（`LiveTimeZoneMonitor`）**：注册广播接收器监听 `Intent.ACTION_TIMEZONE_CHANGED`，配合 `callbackFlow` 在时区变更时发射最新 `TimeZone.currentSystemDefault()`；
  2. **测试替身（`TestTimeZoneMonitor`）**：在截图测试与单测中固定发射 UTC 或特定时区，保障时间相关的 UI 渲染绝对确定；
  3. **UI 消费联动**：通过 CompositionLocal 注入当前时区，所有涉及时间格式化的 Composable 自动响应重组。

---

## 3. 声明式后台增量数据同步（`Synchronizer` + `SyncWorker`） 【已落地】

针对后台数据同步，抽取高阶同步契约（在 [`basic/basic_sync`](file:///e:/StudioProjects/MyApplication/basic/basic_sync) 落地）：
```kotlin
interface Synchronizer {
    suspend fun getChangeListVersions(): ChangeListVersions
    suspend fun updateChangeListVersions(update: ChangeListVersions.() -> ChangeListVersions)
    suspend fun Syncable.sync(): Boolean = syncWith(this@Synchronizer)
}
```
* **WorkManager 定时调度**：通过 `SyncWorker` 声明约束（仅在有网络、充电或空闲时运行）；
* **增量变更同步（ChangeList）**：每次只拉取版本号高于本地游标的增量数据，极大节省流量与设备电量。

---

## 4. 模块化 API-Impl 契约隔离架构（模块物理防腐） 【演进规划 - 待落地】

为防止多模块架构下模块间发生循环依赖或横向业务耦合，将 Feature 拆分为成对的两个模块：

```
modules/
├── module_user/
│   ├── api/      # :modules:module_user:api （对外暴露的轻量接口契约）
│   │   ├── IUserService.kt        # 服务接口
│   │   └── UserModel.kt           # 对外数据载荷
│   └── impl/     # :modules:module_user:impl （具体实现，对外部不可见）
│       ├── UserServiceImpl.kt    # 业务逻辑实现
│       └── UserModuleDi.kt        # Hilt 装配绑定
```
* **构建隔离**：调用方模块（如 `module_order`）仅依赖 `:module_user:api`，无法调用到实现类中的私有逻辑；
* **DI 自动装配**：`UserServiceImpl` 通过 Hilt `@Binds` 绑定到接口，壳工程在打包时拉取所有 `:impl`，在编译期实现无反射、强类型的依赖注入。

---

## 5. Jetpack Navigation 3 声明式导航体系 【部分落地】

现代 Android 官方导航已经从基于 URL 字符串跳转全面演进至 **Navigation 3**（`androidx.navigation3`）。已在 [`modules/module_compose`](file:///e:/StudioProjects/MyApplication/modules/module_compose) 落地 `Nav3Activity` 基础样板。

#### 核心机制
* **类型安全路由模型 (`NavKey`)**：放弃拼接 URL，导航键（NavKey）由强类型 `data class` / `@Serializable` 承载：
  ```kotlin
  @Serializable
  data class ArticleDetailKey(val articleId: String)
  ```
* **多返回栈（Multi-BackStack）原生支持**：通过 `rememberNavBackStack` 分别为底部主导航的每个 Tab 独立维护导航栈，切换 Tab 时状态完美保留；
* **解耦与测试友好**：`NavDisplay` 接收当前栈顶的 `key`，并由各 Feature 模块提供的独立 Content 映射函数渲染 UI，彻底消除全局黑盒胖路由表的编译耦合。

---

## 6. Material 3 Adaptive 大屏与折叠屏自适应布局 【部分落地】

针对手机、折叠屏、平板及桌面设备的多样化屏幕尺寸，现代工程化要求 UI 原生具备自适应响应式能力。已在 `module_compose` 落地基础 `AdaptiveActivity`。

#### 列表-详情自适应脚手架 (`ListDetailPaneScaffold`)
```kotlin
val navigator = rememberListDetailPaneScaffoldNavigator()

ListDetailPaneScaffold(
    directive = navigator.scaffoldDirective,
    value = navigator.scaffoldValue,
    listPane = {
        ArticleListPane(onArticleClick = { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, it) })
    },
    detailPane = {
        ArticleDetailPane(article = navigator.currentDestination?.content)
    }
)
```
* **自适应表现**：
  * **紧凑屏幕（标准手机）**：单栏展示，点击列表项平滑入栈推进详情页；
  * **展开屏 / 平板 / 横屏**：自动平铺为左右双栏，左侧列表常驻，右侧同步展现选中详情，无需书写任何多套 Activity 或适配碎片代码。

---

## 7. 可插拔能力接口模式（Null-Object 安全兜底） 【已落地】

对于日志、埋点、设备探针、动态能力等外部基础设施，采用三套实现范式：

| 实现类型 | 命名约定 | 职责 |
| :--- | :--- | :--- |
| **真实生产实现** | `ProdXxx` | 打包生产环境，执行真实业务上报与设备调用 |
| **本地调试桩** | `StubXxx` | 本地开发使用，仅向 Logcat 输出结构化参数 |
| **安全空实现** | `NoOpXxx` | 纯空操作，供单元测试与 Compose Preview 消费 |

配合 CompositionLocal 注入默认值：
```kotlin
val LocalAppAnalytics = staticCompositionLocalOf<AnalyticsHelper> {
    // 默认空实现，保证任何 Compose 组件在 Preview 中直接渲染，绝不抛出依赖缺失异常
    NoOpAnalyticsHelper()
}
```
