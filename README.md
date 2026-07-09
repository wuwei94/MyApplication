# MyApplication — Android 个人技术栈沉淀工程

一个用于沉淀 Android 技术栈、源码阅读与跨端实践的个人学习工程。工程上采用 **Kotlin DSL + Version Catalogs + Convention Plugin**（参考 [Now in Android](https://github.com/android/nowinandroid) 结构）进行统一版本管理和组件化拆分；能力层覆盖 **MVP / MVVM / MVI / Mavericks**，并接入 **Jetpack Compose / Flutter** 双栈视图层，方便横向对比不同架构与 UI 方案。

---

## Highlights

- **工程化**：Kotlin DSL + Version Catalogs + `build-logic` Convention Plugin，ARouter 模块通信，Hilt 多模块初始化，GitHub Actions CI（lint + assemble）。
- **架构层**：MVP / MVVM / MVI / Mavericks 全覆盖，配套 `UseCase` + `Repository` + `ServiceLocator` 脚手架。
- **网络层**：Volley / OkHttp / Retrofit / Ktor / WebSocket / Netty / NanoHTTPD，含拦截器、下载库、点九图解析。
- **持久层**：Room / ObjectBox + DataStore（Preferences / Proto）。
- **消息总线**：RxBus / LiveDataBus / FlowBus / EventBus 四种方案对比实现。
- **跨端**：Android Native + Flutter 双栈落地，Flutter 内覆盖 Dio / Provider / GetX / BloC。
- **Coroutines + Flow**：配合 `repeatOnLifecycle`、`DataStore`、`Paging`、`WorkManager` 等 Jetpack 组件实践。
- **自定义 View**：高斯模糊、裸眼 3D、跑马灯、无限滚动 ImageView、验证码控件等。
- **Compose**：Navigation、BackHandler、手势 / 拖拽 / `rememberSaveable`、SmartRefresh 等原生能力示例。

---

## Tech Stack

| Layer       | Tech |
|-------------|------|
| Language    | Kotlin |
| Build       | Gradle Kotlin DSL · Version Catalogs · Convention Plugin |
| UI          | Android Views · Jetpack Compose · Material3 · Flutter |
| Architecture| MVP · MVVM · MVI · Mavericks |
| DI          | Hilt |
| Navigation  | ARouter · Navigation Component |
| Network     | OkHttp · Retrofit · Ktor · Volley · WebSocket · Netty · NanoHTTPD |
| Persistence | Room · ObjectBox · DataStore |
| Image       | Glide · Coil |
| Reactive    | Coroutines · Flow · RxJava 3 · LiveData |
| Messaging   | EventBus · RxBus · LiveDataBus · FlowEventBus |
| Others      | WorkManager · Paging 3 · SplashScreen · MMKV |
| CI/CD       | GitHub Actions（lint + assemble） |

> 各库版本详见 `gradle/libs.versions.toml`。

---

## Project Structure

```
MyApplication/
├── app                         # 壳工程（Hilt + ARouter 入口）
├── build-logic                 # Convention Plugin，统一插件配置
├── gradle/libs.versions.toml   # 统一版本目录
├── basic                       # 基础设施层
│   ├── basic_lib               # BaseActivity / Fragment / ViewModel / 通用工具
│   ├── basic_module            # 通用 Bus、Router、UI 脚手架
│   ├── basic_repository        # 通用数据源 / OkHttp / Retrofit 基础封装 / Repository 基类、Room、依赖装配
│   └── basic_flutter           # Flutter 基础设施（FVM / 资源 / 平台桥接）
├── libs                        # 可复用的业务能力库
│   ├── lib_okhttp / lib_retrofit / lib_ktor / lib_volley / lib_websocket / lib_download
│   ├── lib_eventbus / lib_ninepatch / lib_imageloader / lib_widget
│   └── ...
└── modules                     # Feature 模块
    ├── module_arch             # 架构 Demo（MVP / MVVM / MVI / Mavericks）
    ├── module_event            # 消息总线（EventBus / RxBus / LiveEventBus / FlowEventBus）
    ├── module_kotlin           # Kotlin 语言特性（Coroutines / Flow / Inline / Delegate）
    ├── module_jetpack          # Jetpack 组件（Hilt / Room / Paging / DataStore / WorkManager）
    ├── module_network          # 网络 Demo（OkHttp / Retrofit / Ktor / WebSocket / Netty）
    ├── module_sample           # 系统能力示例（UI 组件 / 动画 / 后台任务 / IPC 通信）
    ├── module_features         # 业务功能（Hook / 悬浮窗 / 裁剪 / 相机 / 转盘）
    ├── module_opensource       # 开源框架（PAG / Lottie / SVGA / ObjectBox / MMKV / RxJava）
    ├── module_compose          # Compose 示例（Navigation / 手势 / 拖拽 / SmartRefresh）
    ├── module_widget           # 自定义控件（高斯模糊 / 裸眼3D / 跑马灯 / 验证码）
    ├── module_utils            # 工具库示例（权限 / 文件 IO / 线程 / 适配）
    └── module_flutter          # Flutter 子工程
```

---

## Modules Detail

### module_arch

架构模式对比 Demo，覆盖 Android 开发中主流的架构方案。

| 模式 | 说明 |
|------|------|
| MVP | Presenter 持有 View 引用，手动桥接 |
| MVVM | LiveData + ViewModel，UseCase 封装单一业务逻辑 |
| MVI | 单向数据流：State → UI → Intent → ViewModel → State |
| Mavericks | 基于 Mavericks 框架的 MVI 实现，含 Counter 示例 |

### module_event

从各架构/框架中抽离出的**消息总线专项模块**，统一展示四种 EventBus 实现的差异。

| Bus          | Delayed | Ordered | Sticky | Lifecycle | Cross-process | Thread dispatch |
|--------------|---------|---------|--------|-----------|---------------|-----------------|
| EventBus     | ❌      | ✅      | ✅     | ❌        | ❌            | ✅              |
| RxBus        | ❌      | ✅      | ✅     | ❌        | ❌            | ✅              |
| LiveEventBus | ✅      | ✅      | ✅     | ✅        | ✅            | ❌              |
| FlowEventBus | ✅      | ✅      | ✅     | ✅        | ❌            | ✅              |

### module_kotlin

Kotlin 语言特性在 Android 上的实践。

- Coroutines 协程（结构化并发、异常处理、线程切换）
- Flow 数据流（StateFlow / SharedFlow / 操作符链）
- Inline 函数与委托（属性委托、`by lazy`、自定义委托）
- Repository 模式 + NetworkResult 封装

### module_jetpack

Jetpack 组件库 Demo。

- **Hilt**：依赖注入基础配置
- **Room**：数据库 CRUD + DAO
- **Paging 3**：分页加载（含 RemoteMediator + RemoteKey 方案）
- **DataStore**：Preferences / Proto 两种存储
- **WorkManager**：后台任务（普通 + expedited）
- **OAuth**：Room 数据库存储 OAuth Token

### module_network

网络库全栈对比 Demo。

- HttpURL / Volley / OkHttp / Retrofit 基础用法
- Retrofit + RxJava / Kotlin Coroutines 两种异步方案
- OkHttp 拦截器（自定义 Interceptor）
- 下载库（OkHttp / Retrofit / RxDownload）
- WebSocket（OkHttp 实现）
- Ktor 客户端
- 服务端：NanoHTTPD / Android WebServer / Netty Socket

### module_sample

Android 系统能力与常用组件示例。

- **UI 组件**：ViewPager / ViewPager2 / RecyclerView / WebView / Dialog / FlexBox / AppBar
- **Fragment**：多 Tab 容器、Fragment 嵌套、FragmentTabHost
- **动画**：Animator / Transition / RenderScript / RenderEffect
- **后台任务**：HandlerThread / AsyncTask / JobScheduler
- **IPC 通信**：Broadcast / Service / Messenger / AIDL
- **系统能力**：Notification / Typeface / OnBackPressed / ActivityResult

### module_features

业务功能 Demo。

- Hook / 反射机制（HookManager）
- 悬浮窗（FloatWindow）
- 裁剪 / 相机
- 安全密钥（SecureKey）
- 麦克风动画 / 转盘

### module_opensource

第三方开源框架集成 Demo。

- 动画：PAG / Lottie / SVGAPlayer
- 数据库：ObjectBox
- UI：FlycoTabLayout / SwipeLayout / PhotoView / Banner / EasyFloat / RealtimeBlurView
- 选择器：PictureSelector / CityPicker / PickerView
- 工具：RxJava / LoadSir / MMKV / PermissionX

### module_compose

Jetpack Compose 示例，覆盖声明式 UI 核心能力。

- 基础组件：Text / Image / Button / Canvas / ConstraintLayout / CompositionLocal
- Navigation：NavHost / BottomNavigation / NavigationBar
- 手势：Draggable / DragGestureDetector / AnchoredDraggable / GuaguaCard
- 状态：remember / rememberSaveable / SmartRefresh（下拉刷新）
- 布局：ScrollableTab / HorizontalPager / CoordinatorLayout / LazyColumn
- 其他：BackHandler / ComposeView 混合使用

### module_widget

自定义 View 控件 Demo。

- 高斯模糊（BlurView）
- 裸眼 3D 效果（Sensor3D）
- 跑马灯（MarqueeView）
- 无限滚动 ImageView
- 验证码控件
- BottomSheetDialog / AlertDialog
- Spinner / TitleBar

### module_utils

工具库 Demo。

- 权限申请（PermissionX）
- 文件 IO 操作
- 线程工具
- 屏幕适配（AdaptScreenUtils）
- ARouter Service 实现（图片 / 资源 / 文件 IO）

### module_flutter

Flutter 子工程，覆盖 Flutter 核心组件与状态管理。

- **布局类**：Row / Column / Flex / Wrap / Flow / Stack
- **容器类**：Container / Padding / Align / Center / ConstrainedBox / DecoratedBox / SizedBox
- **可滚动**：ListView / GridView / SingleChildScrollView / PageView / TabBarView / AnimatedList / CustomScrollView / NestedScrollView
- **功能型**：LayoutBuilder / GestureDetector / PopScope / InheritedWidget / FutureBuilder / StreamBuilder
- **其他**：Animation / Dialog / Isolate
- **网络请求**：[Dio](https://pub.dev/packages/dio)
- **状态管理**：[Provider](https://pub.dev/packages/provider) / [GetX](https://pub.dev/packages/get) / [BloC](https://pub.dev/packages/flutter_bloc)
- **三方框架**：Toast / Notification / SharedPreferences / ScreenUtil

---

## 源码笔记

常见组件与开源框架的源码阅读笔记沉淀在个人知识库（Flowus），与本工程配套的核心笔记：

### Android 基础

* [so库适配简单总结](https://flowus.cn/williamwu/share/6f18d2cd-9df9-4637-bdea-1d4e89919876)
* [RecyclerView 绘制流程](https://flowus.cn/williamwu/share/a20dab7f-b70c-4272-9353-b60dd832c7b2)
* [RecyclerView 缓存机制](https://flowus.cn/williamwu/share/36be3fd1-6c8d-4164-bd9a-bd8df49e7557)

### Jetpack 源码

* [Lifecycle 源码解析](https://flowus.cn/williamwu/share/fab8b772-2374-4687-ac80-746ec914dda8)
* [LiveData 源码解析](https://flowus.cn/williamwu/share/6e97c045-679e-4f16-93bf-3f5b0b35d8b4)
* [ViewModel 源码解析](https://flowus.cn/williamwu/share/a8af4987-102c-436d-89a9-4a42a483c019)

### 开源框架源码

* [OkHttp 源码解析](https://flowus.cn/williamwu/share/1fb573d6-a924-4441-a20d-2f7de2f9d195)
* [Retrofit 源码解析](https://flowus.cn/williamwu/share/c8e3e04c-d24b-49af-847d-4a8be6646e2e)
* [Glide 执行流程](https://flowus.cn/williamwu/share/fd31dd11-4bcb-45d9-8f94-772a8628b69e)
* [Glide 缓存机制](https://flowus.cn/williamwu/share/2ce6971d-2566-4fe6-ab5a-d24e98fffe80)
* [ARouter 源码解析](https://flowus.cn/williamwu/share/bd1b9ab3-c881-42aa-a209-07336402660e)
* [EventBus 源码解析](https://flowus.cn/williamwu/share/55d699dd-2728-4e68-b2c8-b3d3db9d89a1)
* [LeakCanary 源码解析](https://flowus.cn/williamwu/share/3dab5ccc-2d12-45f7-83a4-828ce87504e)

---

## License

个人学习项目，欢迎参考 / fork / 提 Issue。
