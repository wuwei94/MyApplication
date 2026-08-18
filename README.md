# MyApplication — Android 个人技术栈沉淀工程

一个用于沉淀 Android 技术栈、源码阅读与跨端实践的个人学习工程。工程上采用 **Kotlin DSL + Version Catalogs + Convention Plugin**（参考 [Now in Android](https://github.com/android/nowinandroid) 结构）进行统一版本管理和组件化拆分；能力层覆盖 **MVP / MVVM / MVI / Mavericks**，并接入 **Jetpack Compose / Flutter** 双栈视图层，方便横向对比不同架构与 UI 方案。

---

## Highlights

- **工程化**：Kotlin DSL + Version Catalogs + `build-logic` Convention Plugin，ARouter 模块通信，Hilt 多模块初始化，GitHub Actions CI（lint + assemble）。
- **架构层**：MVP / MVVM / MVI / Mavericks 全覆盖，配套 `UseCase` + `Repository` + `ServiceLocator` 脚手架。
- **网络层**：Volley / OkHttp / Retrofit / Retrofit Rx / Ktor / Flutter Dio / Flutter http / WebSocket / Netty / NanoHTTPD。Ktor 固定使用 OkHttp Engine，覆盖异常、超时、Cookie、缓存、安全日志与扩展插件；Flutter Dio/http 的普通请求与 Retrofit 统一业务响应、`ServerResultException` 业务失败原因和 `1000–1004` 网络错误码。
- **持久层**：Room / ObjectBox + DataStore（Preferences / Proto）。
- **消息总线**：EventBus / RxEventBus / LiveEventBus / FlowEventBus 四种方案对比实现。
- **跨端**：Android Native + Flutter 双栈落地，Flutter 内覆盖 Dio / http / Provider / GetX / BloC。
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
| Messaging   | EventBus · RxEventBus · LiveEventBus · FlowEventBus |
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
├── docs                        # 文档（modules / libs / network / transfer / build-logic / conventions）
├── basic                       # 基础设施层
│   ├── basic_lib               # BaseActivity / Fragment / ViewModel / 通用工具
│   ├── basic_shared            # 通用 Bus、Router、内联日志 UI 脚手架与 JSON 格式化
│   ├── basic_repo              # 通用数据源 / OkHttp / Retrofit 基础封装 / Repository 基类、Room、依赖装配
│   ├── basic_server            # 服务端基础模块
├── flutter                     # Flutter 层（add-to-app 集成）
│   ├── flutter_demo            # Flutter Demo Catalog 子工程（Flutter module）
│   └── flutter_libs            # Flutter 本地库（lib_network_dio / lib_network_http 独立封装、lib_image_loader 内核可切换封装、lib_event_bus 事件总线封装、lib_storage 键值存储封装）
├── libs                        # 可复用的业务能力库
│   ├── lib_okhttp              # OkHttp 封装（DSL、多实例及缓存生命周期、安全日志、OkHttp 控制 Header、上传下载进度）
│   ├── lib_retrofit            # Retrofit 封装（DSL、Gson 响应转换、可空 Parcelable 响应、加载状态 View）
│   ├── lib_retrofit_rx         # RxJava3 + Retrofit（注解接口、默认调度与 Rx 回调）
│   ├── lib_rx_request          # 分层的 RxRequest 动态 Retrofit 请求 Builder
│   ├── lib_ktor                # Ktor 项目级封装（固定 OkHttp Engine、Plugin 配置、可空 Parcelable 响应与常用请求）
│   ├── lib_volley              # Volley 封装（轻量级 HTTP）
│   ├── lib_websocket_okhttp    # OkHttp WebSocket 封装
│   ├── lib_websocket_java      # Java-WebSocket 封装
│   ├── lib_rx_download         # Retrofit + Rx 下载（统一回调、条件续传、物理终止并发屏障与聚合进度）
│   ├── lib_rx_upload           # Retrofit + Rx 链式 POST Multipart 上传（单/多文件、UploadResult 与进度）
│   ├── lib_netty               # Netty TCP 封装
│   ├── lib_nanohttpd           # NanoHTTPD 服务器封装
│   ├── lib_eventbus            # EventBus 事件总线封装
│   ├── lib_imageloader         # Glide / Coil 图片加载封装（IImageLoader 接口 + 内核可切换）
│   ├── lib_ninepatch           # NinePatch 图片处理工具
│   └── lib_widget              # 自定义 Widget 控件集合
└── modules                     # Feature 模块
    ├── module_ui               # 系统 UI 组件（Dialog / FlexBox / RecyclerView / ViewPager / ViewFlipper / WebView）
    ├── module_tab              # Tab 导航（TabLayout / FragmentTabHost / RadioGroup / ViewPager / ViewPager2 联动）
    ├── module_anim             # 动画（ObjectAnimator / AnimatorSet / ValueAnimator / Keyframe / RenderEffect / RenderScript / Transition）
    ├── module_widget           # 自定义控件（AlertDialog / BlurView / NinePatch / 跑马灯 / 验证码）
    ├── module_async            # 异步处理（AsyncTask / HandlerThread / JobScheduler）
    ├── module_component        # 组件交互（Broadcast / Service / Messenger / ActivityResult / OnBackPressed）
    ├── module_system           # 系统能力（Notification / Permission / SecureKey）
    ├── module_sample           # 技术示例（Hook / Typeface / FloatWindow）
    ├── module_feature          # 业务功能（转盘 / 麦位动画 / 相机 / 裁剪）
    ├── module_network          # 网络库（OkHttp / Retrofit / Ktor / Volley / WebSocket / Socket）
    ├── module_okhttp           # OkHttp / Retrofit / 标准 Retrofit Rx 示例
    ├── module_rx_retrofit      # Rx 动态 Retrofit 请求与上传下载示例
    ├── module_websocket        # WebSocket 专项示例
    ├── module_utils            # 工具库示例（AdaptScreenUtils / FileIOUtils / PermissionUtils / ThreadUtils）
    ├── module_event            # 事件总线（EventBus / RxEventBus / LiveEventBus / FlowEventBus）
    ├── module_open_source      # 第三方库（Lottie / MMKV / Banner / PhotoView / RxJava / ObjectBox）
    ├── module_kotlin           # Kotlin 语言特性（Coroutines / Flow）
    ├── module_jetpack          # Jetpack 组件（Room / DataStore / WorkManager / Paging / Hilt）
    ├── module_arch             # 架构模式（MVP / MVVM / MVI / Mavericks）
    ├── module_compose          # Compose 示例（Navigation / 手势 / 拖拽 / SmartRefresh）
    └── module_flutter          # Flutter 子工程
```

---

## Modules Detail

### module_ui（系统 UI 组件）

演示 Android 标准 UI 组件。

- AppBar / Dialog / FlexBox
- RecyclerView（基础 / 嵌套滚动）
- ViewPager / ViewPager2
- ViewFlipper / WebView

### module_tab（Tab 导航）

演示 Fragment + Tab 导航的多种实现方式。

- FragmentTabHost + TabWidget
- RadioGroup + FrameLayout Tab
- ViewPager + RadioGroup 联动
- BottomNavigationView + Fragment（底部导航栏）

### module_anim（动画）

演示 Android 原生动画机制。

- ObjectAnimator 属性动画（透明度/旋转/缩放/平移）
- AnimatorSet 动画组合（顺序/同时/Builder 编排）
- ValueAnimator 差值动画 + 插值器对比 + ViewPropertyAnimator
- Keyframe 关键帧动画 + PropertyValuesHolder
- 视图过渡动画（ChangeBounds / Fade / Slide / AutoTransition）
- RenderEffect 渲染效果（Android 12+）
- RenderScript 图像处理（已废弃）
- Activity 过渡动画（分解 / 滑动 / 淡入 / 共享元素）

### module_widget（自定义控件）

演示项目自定义实现的 UI 控件。

- 高斯模糊（BlurView）
- 裸眼 3D 效果（Sensor3D）
- 跑马灯（MarqueeView）
- 无限滚动 ImageView
- 验证码控件
- BottomSheetDialog / AlertDialog
- Spinner / TitleBar
- 九宫格拉伸图片（NinePatch）

### module_async（异步处理）

演示 Android 异步/后台处理机制。

- AsyncTask 异步任务
- HandlerThread 线程间通信
- JobScheduler 定时任务调度

### module_component（组件交互）

演示 Android 四大组件间的交互机制。

- BroadcastReceiver 广播注册与发送
- ActivityResultContracts 新版结果回调 API
- OnBackPressedDispatcher 返回键拦截
- Service 绑定（bindService）与前台服务
- Messenger 跨进程通信（IPC）

### module_system（系统能力）

演示 Android 系统级能力。

- NotificationChannel 通知渠道创建与通知发送
- 运行时权限批量申请
- Android Keystore 安全密钥创建与签名

### module_sample（技术示例）

演示零散的技术技巧，不属于完整业务场景。

- View Hook 反射技术
- 自定义字体加载（Typeface.createFromAsset）
- 悬浮窗（WindowManager + 拖拽 + 贴边动画）

### module_feature（业务功能）

业务功能 Demo。

- 转盘抽奖（旋转动画）
- 麦位动画（自定义 LayoutManager）
- CameraX 相机拍照/录像
- 图片裁剪（Intent 调用系统裁剪）

### module_network（网络库全栈对比）

HttpURLConnection、Volley 与 Ktor 对比 Demo。请求页统一使用 `BasicResponseActivity` 居中展示初始说明，并在运行后内联追加成功、HTTP/业务失败和传输异常；各网络封装的职责、生命周期与差异详见 [Android 网络请求封装](docs/network.md)。

- HttpURLConnection / Volley 基础请求
- Ktor 原生客户端与项目级 Ktor Client 封装

### module_okhttp（OkHttp / Retrofit / Retrofit Rx）

按封装库组织网络请求示例。所有实际请求页使用 `BasicResponseActivity` 累积展示响应与错误。

- `okhttp`：`lib_okhttp` DSL 与 OkHttp 原生请求
- `retrofit`：Retrofit 原生 `Call` 与 `lib_retrofit` DSL
- `retrofit_coroutine`：Retrofit 协程挂起函数原生调用与 `lib_retrofit` DSL
- `retrofit_rx`：RxJava 原生订阅与默认网络策略

### module_rx_retrofit（Rx 动态请求与文件传输）

- `request`：`RxRequestActivity` 基于 `BasicResponseActivity` 展示 `lib_rx_request` Form、JSON 与 Multipart 动态请求
- `download`：`RxDownloadActivity` 复用页面级 Rx Retrofit 和统一 `RxDownloadCallback`，通过根包的 `RxDownload` / `RxDownloadManager` 展示条件断点续传与单/多文件并发下载
- `upload`：`RxUploadActivity` 复用页面级 Rx Retrofit 和统一 `RxUploadCallback`，展示单/多文件 POST Multipart 上传

上传、下载、断点续传和并发约定详见 [文件上传与下载](docs/transfer.md)。

### module_websocket（WebSocket 专项示例）

WebSocket 专项功能演示。

- OkHttp WebSocket 实现
- Java WebSocket 实现
- Netty WebSocket 实现

### module_utils（工具库示例）

演示 BlankJ utilcode 工具库的各种工具类。

- 屏幕适配（AdaptScreenUtils）
- 文件 IO（FileIOUtils）
- 权限申请（PermissionUtils）
- 线程工具（ThreadUtils）

### module_event（事件总线）

从各架构/框架中抽离出的**消息总线专项模块**，统一展示四种 EventBus 实现的差异。

| Bus          | Delayed | Ordered | Sticky | Lifecycle | Cross-process | Thread dispatch |
|--------------|---------|---------|--------|-----------|---------------|-----------------|
| EventBus     | ❌      | ✅      | ✅     | ❌        | ❌            | ✅              |
| RxEventBus   | ❌      | ✅      | ✅     | ❌        | ❌            | ✅              |
| LiveEventBus | ✅      | ✅      | ✅     | ✅        | ✅            | ❌              |
| FlowEventBus | ✅      | ✅      | ✅     | ✅        | ❌            | ✅              |

### module_open_source（第三方库）

第三方开源框架集成 Demo。

- 动画：PAG / Lottie / SVGAPlayer
- 数据库：ObjectBox
- UI：FlycoTabLayout / SwipeLayout / PhotoView / Banner / EasyFloat / RealtimeBlurView / ShadowLayout
- 选择器：PictureSelector / CityPicker / PickerView
- 工具：RxJava / LoadSir / MMKV / PermissionX
- 图片加载：Coil / Glide

### module_kotlin（Kotlin 语言特性）

Kotlin 语言特性在 Android 上的实践。

- Coroutines 协程（结构化并发、异常处理、线程切换）
- Flow 数据流（StateFlow / SharedFlow / 操作符链）

### module_jetpack（Jetpack 组件库）

Jetpack 组件库 Demo。

- **Hilt**：依赖注入基础配置
- **Room**：数据库 CRUD + DAO
- **Paging 3**：分页加载（含 RemoteMediator + RemoteKey 方案）
- **DataStore**：Preferences / Proto 两种存储
- **WorkManager**：后台任务（普通 + expedited）

### module_arch（架构模式）

架构模式对比 Demo，覆盖 Android 开发中主流的架构方案。

| 模式 | 说明 |
|------|------|
| MVP | Presenter 持有 View 引用，手动桥接 |
| MVVM | LiveData + ViewModel，UseCase 封装单一业务逻辑 |
| MVI | 单向数据流：State → UI → Intent → ViewModel → State |
| Mavericks | 基于 Mavericks 框架的 MVI 实现，含 Counter 示例 |

### module_compose（Compose 示例）

Jetpack Compose 示例，覆盖声明式 UI 核心能力。

- 基础组件：Text / Image / Button / Canvas / ConstraintLayout / CompositionLocal / LazyColumn
- Navigation：NavHost / BottomNavigation / NavigationBar
- 手势：Draggable / DragGestureDetector / AnchoredDraggable / GuaguaCard
- 状态：remember / rememberSaveable / SmartRefresh（下拉刷新）
- 布局：ScrollableTab / HorizontalPager / CoordinatorLayout
- 其他：BackHandler

### module_flutter（Flutter 子工程）

Flutter 子工程，覆盖 Flutter 核心组件与状态管理。

- **布局类**：Row / Column / Flex / Wrap / Flow / Stack
- **容器类**：Container / Padding / Align / Center / ConstrainedBox / DecoratedBox / SizedBox
- **可滚动**：ListView / GridView / SingleChildScrollView / PageView / TabBarView / AnimatedList / CustomScrollView / NestedScrollView
- **功能型**：LayoutBuilder / GestureDetector / PopScope / InheritedWidget / FutureBuilder / StreamBuilder
- **其他**：Animation / Dialog / Isolate
- **网络请求**：`lib_network_dio`（DioClient）与 `lib_network_http`（HttpClient）两个独立本地 package，与 Retrofit 共享 `code/message/data` 业务响应和 `code/message/cause` 异常契约，并统一常用 HTTP 方法、请求体和请求取消；日志沿用各自实现且不做脱敏
- **图片加载**：`lib_image_loader` 本地 package（`IImageLoader` 接口 + `ImageLoader` 门面），默认内核 cached_network_image，切换内核调用方零改动，与 Android `lib_imageloader` 结构对齐
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
