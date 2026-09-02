# MyApplication — Android 个人技术栈沉淀工程

一个用于沉淀 Android 技术栈、源码阅读与跨端实践的个人学习工程。工程上采用 **Kotlin DSL + Version Catalogs + Convention Plugin**（参考 [Now in Android](https://github.com/android/nowinandroid) 结构）进行统一版本管理和组件化拆分；能力层覆盖 **MVP / MVVM / MVI / Mavericks**，并接入 **Jetpack Compose / Flutter** 双栈视图层，方便横向对比不同架构与 UI 方案。

---

## Highlights

- **工程化**：Kotlin DSL + Version Catalogs + `build-logic` Convention Plugin，ARouter 模块通信，Hilt 多模块初始化，GitHub Actions CI（lint + assemble）。
- **架构层**：MVP / MVVM / MVI / Mavericks 全覆盖，配套 `UseCase` + `Repository` + `ServiceLocator` 脚手架。
- **网络层**：Volley / OkHttp / Retrofit / Retrofit Rx / Ktor / Flutter Dio / Flutter http / WebSocket / Netty / MQTT / NanoHTTPD。Ktor 固定使用 OkHttp Engine，覆盖异常、超时、Cookie、缓存、安全日志与扩展插件；Flutter Dio/http 的普通请求与 Retrofit 统一业务响应、`ServerResultException` 业务失败原因和 `1000–1004` 网络错误码。
- **持久层**：Room / ObjectBox + DataStore（Preferences / Proto）。
- **消息总线**：EventBus / RxEventBus / LiveEventBus / FlowEventBus 四种方案对比实现。
- **跨端**：Android Native + Flutter 双栈落地，Flutter 内覆盖 Dio / http / SSE / Provider / GetX / BloC。
- **Coroutines + Flow**：配合 `repeatOnLifecycle`、`DataStore`、`Paging`、`WorkManager` 等 Jetpack 组件实践。
- **自定义 View & 图表**：高斯模糊、裸眼 3D、跑马灯、无限滚动 ImageView、验证码控件等；MPAndroidChart 折线/柱状/饼图/雷达图与多图表全景看板联动。
- **Compose**：Navigation、BackHandler、手势 / 拖拽 / `rememberSaveable`、SmartRefresh、Canvas 自绘贝塞尔折线/分组圆角柱状/甜甜圈/雷达图等多图表联动。
- **性能优化**：DiffUtil 差量刷新、LRU 内存缓存策略、协程 Dispatcher 调度性能对比。

---

## Tech Stack

| Layer       | Tech |
|-------------|------|
| Language    | Kotlin |
| Build       | Gradle Kotlin DSL · Version Catalogs · Convention Plugin |
| UI          | Android Views · Jetpack Compose · Material3 · Flutter |
| Charts      | MPAndroidChart · Compose Canvas · Flutter fl_chart |
| Architecture| MVP · MVVM · MVI · Mavericks |
| DI          | Hilt · Koin |
| Navigation  | ARouter · Navigation Component |
| Network     | OkHttp · Retrofit · Ktor · Volley · WebSocket · Netty · MQTT · Bluetooth BLE · NanoHTTPD |
| Persistence | Room · ObjectBox · DataStore |
| Image       | Glide · Coil |
| Reactive    | Coroutines · Flow · RxJava 3 · LiveData |
| Messaging   | EventBus · RxEventBus · LiveEventBus · FlowEventBus |
| ML / AI     | TensorFlow Lite · LiteRT · GPU Delegate |
| Others      | WorkManager · Paging 3 · SplashScreen · MMKV |
| Performance | DiffUtil · LruCache · Dispatcher 调度优化 |
| Quality     | Dependency Guard（已接入） |
| CI/CD       | GitHub Actions（lint + assemble） |

> 各库版本详见 `gradle/libs.versions.toml`。

---

## Application 初始化（两种方案可手动切换）

工程演示两种 Application 级别的初始化方案，二者通过 `AndroidManifest` 中的
`android:name` 切换（`App` ↔ `AppHilt`），同一时刻只有一种生效：

| 方案 | 机制 | 入口 | 各模块接入方式 |
|------|------|------|----------------|
| 手动方案 | 继承 `BaseAppInit`，在 `App.initApp()` 中 `registerAppInit(...)` | `app.App` | 模块提供 `XxxApp : BaseAppInit` |
| Hilt 方案 | 实现 `IAppInit`，用 `@XxxInit` 限定符经 `@Binds` 绑定，注入 `AppHilt` 调用 | `app.AppHilt`（`@HiltAndroidApp`） | 模块提供 `XxxInitImpl : IAppInit` + `XxxModule` |

已接入两套方案的模块（`@XxxInit` 与 `registerAppInit` 一一对应）：

- `module_event`：`EventApp`（手动）/ `EventInitImpl` + `EventModule`（Hilt）
- `module_widget_thirdparty`：`LoadSirApp` / `LoadSirInitImpl` + `LoadSirModule`
- `module_flutter`：`FlutterApp` / `FlutterInitImpl` + `FlutterModule`
- `module_arch`：`MavericksApp` / `MavericksInitImpl` + `MavericksModule`

> 手动方案在 `App.initApp()` 中注册 `EventApp / MavericksApp / LoadSirApp / FlutterApp`；
> Hilt 方案在 `AppHilt.onCreate()` 中按 `baseInit → appInit → eventInit → mavericksInit → loadSirInit → flutterInit` 顺序调用。

---

## Project Structure

```
MyApplication/
├── app                         # 壳工程（Hilt + ARouter 入口）
├── build-logic                 # Convention Plugin，统一插件配置
├── gradle/libs.versions.toml   # 统一版本目录
├── docs                        # 文档（modules / libs / di / event / network / transfer / bluetooth / build-logic / conventions）
├── basic                       # 基础设施层
│   ├── basic_lib               # BaseActivity / Fragment / ViewModel / 通用工具
│   ├── basic_shared            # 通用 Bus、Router、内联日志 UI 脚手架与 JSON 格式化
│   ├── basic_repo              # 通用数据源 / OkHttp / Retrofit 基础封装 / Repository 基类、Room、依赖装配
│   ├── basic_server            # 服务端基础模块
├── flutter                     # Flutter 层（add-to-app 集成）
│   ├── flutter_demo            # Flutter Demo Catalog 子工程（Flutter module）
│   └── flutter_libs            # Flutter 本地库（lib_network_dio / lib_network_http 独立封装、lib_image_loader 内核可切换封装、lib_event_bus 事件总线封装、lib_storage 键值存储封装、lib_bluetooth 蓝牙通信封装）
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
│   ├── lib_mqtt                # MQTT 各客户端复用回调接口（MqttClientListener）
│   ├── lib_mqtt_hivemq         # MQTT 客户端封装（HiveMQ 异步 API，复用 lib_mqtt 回调接口）
│   ├── lib_mqtt_paho_service   # MQTT 客户端封装（Paho Android Service fork，MqttAndroidClient）
│   ├── lib_nanohttpd           # NanoHTTPD 服务器封装
│   ├── lib_eventbus            # EventBus 事件总线封装
│   ├── lib_imageloader         # Glide / Coil 图片加载封装（IImageLoader 接口 + 内核可切换）
│   ├── lib_ninepatch           # NinePatch 图片处理工具
│   └── lib_widget              # 自定义 Widget 控件集合
└── modules                     # Feature 模块（按技术领域分组）
    │
    ├── [UI 组件]
    │   ├── module_widget       # 标准控件（Dialog / PopupWindow / FlexBox / RecyclerView / ViewPager / ViewFlipper / WebView / FloatWindow）
    │   ├── module_tab          # Tab 导航（FragmentTabHost / RadioGroup / ViewPager / ViewPager2 / FlycoTabLayout 联动）
    │   ├── module_anim         # 动画（原生属性/过渡动画 + 第三方动画库 PAG / Lottie / SVGA）
    │   ├── module_widget_custom  # 自定义控件（AlertDialog / CustomPopWindow / BlurView / NinePatch / 跑马灯 / 验证码）
    │   ├── module_widget_thirdparty   # 第三方 UI 库（Banner / CountdownView / EasyFloat / PhotoView / ShadowLayout / SwipeLayout / RealtimeBlurView / CityPicker / PickerView / PictureSelector / LoadSir）
    │   ├── module_markdown     # Markdown 渲染与 AI 流式交互（Markwon 渲染 / Prism4j 代码高亮 / 流式打字机 / AI 聊天）
    │   └── module_imageloader  # 图片加载（Coil / Glide / lib_imageloader）
    │
    ├── [网络通信]
    │   ├── module_http         # HTTP 网络请求（HttpURLConnection / Volley / OkHttp / Retrofit / Rx 动态请求与文件传输 / Ktor）
    │   ├── module_websocket    # WebSocket 专项示例
    │   ├── module_mqtt         # MQTT 发布 / 订阅专项示例
    │   └── module_bluetooth    # 蓝牙通信（BLE 客户端：原生 SDK 方案 + Nordic 官方库方案）
    │
    ├── [数据存储]
    │   ├── module_database     # 数据库（Room / ObjectBox）
    │   └── module_storage      # 键值存储（DataStore / MMKV）
    │
    ├── [系统能力]
    │   ├── module_async        # 异步处理（AsyncTask / HandlerThread）
    │   ├── module_scheduler    # 后台任务调度（JobScheduler / WorkManager）
    │   ├── module_component    # 组件交互（Broadcast / Service / ActivityResult / OnBackPressed）
    │   ├── module_system_service  # 系统服务与底层能力（Notification / Permission / Android Keystore 安全密钥）
    │   ├── module_media        # 多媒体（CameraX 拍照 / 录像 / 图片裁剪）
    │   ├── module_ml           # 机器学习（TensorFlow Lite / LiteRT 端侧推理、GPU 硬件加速与端侧 AI）
    │   └── module_ipc          # 跨进程通信（AIDL / Messenger）
    │
    ├── [架构与工程]
    │   ├── module_arch         # 架构模式（MVP / MVVM / MVI / Compose MVI / Mavericks 并列分包）
    │   ├── module_di           # 依赖注入（Hilt / Koin）
    │   ├── module_event        # 事件总线（EventBus / RxEventBus / LiveEventBus / FlowEventBus）
    │   ├── module_reactive     # 响应式编程（Flow / RxJava 操作符对照）
    │   └── module_performance  # 性能优化（ContentProvider / App Startup / Baseline Profiles / IdleHandler / AsyncLayoutInflater / LruCache / DiffUtil / RecycledViewPool / ConcatAdapter）
    │
    ├── [Kotlin & Jetpack]
    │   ├── module_kotlin       # Kotlin 语言特性（Coroutines / Flow）
    │   └── module_jetpack      # Jetpack 基础架构（Lifecycle / Paging / ViewModel）
    │
    ├── [Compose & Flutter]
    │   ├── module_compose      # Compose 示例（Navigation / 手势 / 拖拽 / SmartRefresh）
    │   └── module_flutter      # Flutter 子工程
    │
    └── [Sample & Feature]
        ├── module_sample       # 技术示例（Hook / Typeface）
        └── module_feature      # 业务功能（转盘 / 麦位动画）
```

---

## Modules Detail

### module_widget（标准控件）

演示 Android 标准 UI 组件。

- AppBar / Dialog / PopupWindow / FlexBox
- RecyclerView（基础 / 嵌套滚动）
- ViewPager / ViewPager2
- ViewFlipper / WebView
- 悬浮窗（WindowManager 系统级浮层 + 拖拽 + 贴边吸附）

### module_tab（Tab 导航）

演示 Fragment + Tab 导航的多种实现方式。

- FragmentTabHost + TabWidget
- RadioGroup + FrameLayout Tab
- ViewPager + RadioGroup 联动
- BottomNavigationView + Fragment（底部导航栏）
- FlycoTabLayout（Sliding / Common / Segment 三种样式）

### module_anim（动画）

演示 Android 原生动画机制与第三方动画库。

- ObjectAnimator 属性动画（透明度/旋转/缩放/平移）
- AnimatorSet 动画组合（顺序/同时/Builder 编排）
- ValueAnimator 差值动画 + 插值器对比 + ViewPropertyAnimator
- 视图过渡动画（ChangeBounds / Fade / Slide / AutoTransition）
- RenderEffect 渲染效果（Android 12+）
- RenderScript 图像处理（已废弃）
- Activity 过渡动画（分解 / 滑动 / 淡入 / 共享元素）
- PAG 动画播放器（腾讯 libpag）
- Lottie 动画播放器（Airbnb Lottie）
- SVGA 动画播放器（YY SVGA）

### module_widget_custom（自定义控件）

演示项目自定义实现的 UI 控件。

- 高斯模糊（BlurView）
- 裸眼 3D 效果（Sensor3D）
- 跑马灯（MarqueeView）
- 无限滚动 ImageView
- 验证码控件
- BottomSheetDialog / AlertDialog
- PopupWindow 封装（CustomPopWindow）
- Spinner / TitleBar
- 九宫格拉伸图片（NinePatch）

### module_async（异步处理）

演示 Android 线程/协程层面的异步机制。

- AsyncTask 异步任务
- HandlerThread 线程间通信

### module_scheduler（后台任务调度）

演示 Android 后台任务调度，聚焦「任务调度」主题。

- JobScheduler 定时任务调度
- WorkManager 现代化可靠后台任务调度与加急前台服务

### module_component（组件交互）

演示 Android 四大组件间的交互机制。

- BroadcastReceiver 广播注册与发送
- ActivityResultContracts 新版结果回调 API
- OnBackPressedDispatcher 返回键拦截
- Service 绑定（bindService）与前台服务

### module_ipc（跨进程通信）

演示 Android 跨进程通信（IPC）的两种基于 Binder 的方案。

- AIDL 跨进程通信（绑定 / 解绑 / 远程调用）
- Messenger 跨进程通信（IPC）

### module_system_service（系统服务）

演示 Android 系统级服务与底层安全密钥机制。

- NotificationChannel 通知渠道创建与通知发送
- 运行时权限批量申请
- Android Keystore 安全密钥创建与 ECDSA 硬件签名

### module_sample（技术示例）

技术技巧与底层探索。收纳不依赖特定业务场景的单点技术技巧、底层 API 机制探索与实验性代码，保持轻量独立，不污染通用架构模块。

- **View Hook**：反射技术（动态代理替换 OnClickListener）
- **自定义字体**：字体加载（Typeface.createFromAsset）

### module_performance（性能优化）

演示 Android 启动与初始化、布局解析与列表渲染等多维度的性能优化与深度调度机制。

- **ContentProvider**：启动早期无侵入自动初始化时序机制与冷启动多 Provider 耗时分析
- **App Startup**：Jetpack 应用初始化组件（单个 InitializationProvider 聚合托管与 DAG 依赖拓扑排序）
- **Baseline Profiles**：Jetpack 基线配置文件（ART 运行时 AOT 预编译提速与 ProfileInstaller 诊断）
- **IdleHandler**：主线程空闲调度（单次/持续监听与延迟初始化）
- **AsyncLayoutInflater**：异步布局解析与 ViewPreloadManager 视图预加载池（0ms 秒开）
- **LruCache**：内存缓存设计模式（Cache-Aside 回源与容量淘汰）
- **ConcatAdapter**：模块化列表组合与 ViewType 隔离刷新
- **RecycledViewPool**：RecyclerView 跨列表/Tab 共享视图池复用
- **DiffUtil**：列表差量计算与 Payload 细粒度局部刷新

### module_media（多媒体）

演示 Android 多媒体能力，按用例（UseCase）拆分为独立示例页。

- **拍照**：ImageCapture 用例（预览取景 + 单张照片捕获 + 结果预览）
- **录像**：VideoCapture 用例（多级分辨率回退 + 音频可选 + 录像回放）
- **图片裁剪**：Intent 调用系统裁剪（图库选择 / 拍照裁剪）

### module_ml（机器学习）

演示 Google 官方轻量级端侧推理框架（TensorFlow Lite / LiteRT）的核心技术链路、硬件加速与落地实践。

- **基础张量与数值回归**：Direct ByteBuffer 内存映射、连续函数数值拟合与批量推理延迟测试
- **图像预处理与分类**：Bitmap 裁剪缩放、RGB 像素归一化、Softmax 分布与 Top-K 置信度标签解析
- **GPU 硬件加速与 Benchmark**：CompatibilityList 兼容性检查、CPU 单/多线程 vs GPU Delegate 多轮基准性能对比
- **LiteRT 架构演进**：从 TFLite 到 LiteRT 演进、FlatBuffers 零拷贝 mmap、模型量化收益与端侧大模型 SLM 落地架构

### module_feature（业务功能）

实战业务场景脱敏。收纳从公司真实商业项目中抽离、脱敏出的典型复合业务场景，展示端到端的真实业务落地能力（UI + 业务逻辑 + 状态联动），不追求强行抽象为纯通用控件。

- 转盘抽奖（旋转动画）
- 麦位动画（自定义 LayoutManager）

### module_http（HTTP 网络请求）

集中演示基础 HTTP 客户端（HttpURLConnection、Volley）、OkHttp、Retrofit（Call / 协程 / RxJava）以及 Rx 动态请求与文件传输。请求页统一使用 `BasicResponseActivity` 居中展示初始说明，并在运行后内联追加响应、日志与原位更新进度；各网络封装的职责、生命周期与差异详见 [Android 网络请求封装](docs/network.md)，上传、下载与并发约定详见 [文件上传与下载](docs/transfer.md)。

- `httpurl`：HttpURLConnection 原生网络请求
- `volley`：Volley 基础请求
- `okhttp`：`lib_okhttp` DSL 与 OkHttp 原生请求
- `retrofit`：Retrofit 原生 `Call` 与 `lib_retrofit` DSL
- `retrofit_coroutine`：Retrofit 协程挂起函数原生调用与 `lib_retrofit` DSL
- `retrofit_rx`：RxJava 原生订阅与默认网络策略
- `request`：`RxRequestActivity` 基于 `BasicResponseActivity` 展示 `lib_rx_request` Form、JSON 与 Multipart 动态请求
- `download`：`RxDownloadActivity` 复用页面级 Rx Retrofit 和统一 `RxDownloadCallback`，展示条件断点续传与单/多文件并发下载
- `upload`：`RxUploadActivity` 复用页面级 Rx Retrofit 和统一 `RxUploadCallback`，展示单/多文件 POST Multipart 上传

### module_websocket（WebSocket & TCP Socket）

WebSocket 与 Netty TCP Socket 专项功能演示。

- **OkHttp WebSocket**：OkHttp 原生连接与 RxJava 封装
- **Java-WebSocket**：Java-WebSocket 客户端、RxJava 封装与内置本地服务端（Port 5566）
- **Netty TCP Socket**：Netty TCP 客户端、RxJava 封装与内置本地服务端（Port 5567）

### module_mqtt（MQTT 发布 / 订阅）

MQTT 消息队列遥测传输专项演示，使用 EMQX 公共 Broker，提供两种客户端实现对比。

- **HiveMQ MQTT Client**：`lib_mqtt_hivemq` 封装，异步 API（流式 Builder + CompletableFuture 回调），依赖 Netty
- **Eclipse Paho Android Service**：`lib_mqtt_paho_service` 封装，MqttAndroidClient 绑定 MqttService（BroadcastReceiver + Service 通信）；采用 hannesa2 维护 fork（官方 1.1.1 已停更，在 targetSdk 34+ 上会因 Receiver 注册缺少导出标志崩溃）

两者均覆盖：连接、订阅、发布（QoS 0/1/2）与断开。

### module_event（事件总线）

从各架构/框架中抽离出的**消息总线专项模块**，统一展示四种 EventBus 实现的差异。

| Bus          | Delayed | Ordered | Sticky | Lifecycle | Cross-process | Thread dispatch |
|--------------|---------|---------|--------|-----------|---------------|-----------------|
| EventBus     | ❌      | ✅      | ✅     | ❌        | ❌            | ✅              |
| RxEventBus   | ❌      | ✅      | ✅     | ❌        | ❌            | ✅              |
| LiveEventBus | ✅      | ✅      | ✅     | ✅        | ✅            | ❌              |
| FlowEventBus | ✅      | ✅      | ✅     | ✅        | ❌            | ✅              |

### module_imageloader（图片加载）

图片加载专项模块，集中展示 Coil、Glide 以及项目级 `lib_imageloader` 统一封装。

- Coil：Coil 3 原生加载（基础 / crossfade / placeholder / error）
- Glide：Glide 4 原生加载（circleCrop / RoundedCorners / centerCrop / crossFade）
- ImageLoader：`lib_imageloader` 统一封装（IImageLoader 接口 + Coil / Glide 内核无感切换）

### module_widget_thirdparty（UI 库）

第三方 UI 控件库集中展示，聚焦可复用的 View/ViewGroup 控件、复合 UI 选择器与第三方页面多状态管理。

- 轮播：Banner
- 倒计时：CountdownView
- 悬浮窗：EasyFloat
- 图片缩放：PhotoView
- 阴影：ShadowLayout
- 侧滑：SwipeLayout
- 模糊：RealtimeBlurView
- 选择器 / 多媒体：CityPicker（城市选择器）/ PickerView（滚动选择器）/ PictureSelector（图片选择器）
- 多状态管理：LoadSir（Activity / Fragment 多状态管理、Loading / Error / Success 切换与点击重试）

### module_markdown（Markdown 渲染与 AI 流式交互）

原生高性能 Markdown 渲染、Prism4j 多语言代码高亮、打字机流控与大模型 AI 聊天流式交互。

- **Markwon 基础与扩展渲染**：基于 AST 解析的高性能原生 Spannable 渲染、GFM 表格、任务清单、HTML 标签、Glide 图片与自定义主题样式
- **Prism4j 代码高亮**（演进中）：Kotlin / Java / Python / JS / SQL 多语言语法着色
- **流式打字机**（演进中）：动态自适应 Buffer 队列、未闭合语法自动补齐与光标闪烁动效
- **AI 聊天实战**（演进中）：RecyclerView Payload 局部增量刷新与智能吸底跟随

### module_kotlin（Kotlin 语言特性）

Kotlin 语言特性在 Android 上的实践。

- **Coroutines 协程**：结构化并发、线程切换调度（Dispatchers）、async/await 并行提速、supervisorScope 异常隔离、withTimeoutOrNull 超时协作式取消与 CoroutineExceptionHandler
- **Flow 数据流**：冷流收集（repeatOnLifecycle）、变换操作符（map/filter/take）、zip/combine 双流组合、debounce + flatMapLatest 搜索防抖、StateFlow vs SharedFlow 热流机制、catch 异常捕获与 retry 自动重试
- **Channel 通道与回调桥接**：Channel 4 种缓冲模式（RENDEZVOUS/BUFFERED/CONFLATED/UNLIMITED）、produce 生产消费模型、callbackFlow 传统监听器桥接（awaitClose 优雅反注册防泄漏）与 channelFlow 跨协程并发发射
- **Concurrency 并发与同步**：Mutex 非阻塞互斥挂起锁（杜绝线程阻塞与死锁）、Semaphore 信号量最大并发度限流、select 多路复用异步竞速与 withContext(NonCancellable) 关键清理保障
- **Delegate 委托机制**：类委托（by base）、自定义 ReadWriteProperty、by lazy 延迟初始化、observable 变更监听、vetoable 条件拦截、Map 映射委托、属性别名重定向与 notNull 非空校验
- **Inline 内联函数**：作用域函数对比（with/let/run/also/apply）、reified 泛型实化与 JSON 解析、自定义内联高阶扩展、noinline 与 crossinline 修饰符
- **Syntax 现代语法与 DSL**：操作符重载（+/*/[ ]/in/invoke）、中缀函数（infix fun）、解构声明（Data Class 与 componentN）、密封接口（Sealed Interface）when 编译器穷举与 @DslMarker 类型安全 DSL 构建器

### module_reactive（响应式编程）

Kotlin Flow 与 RxJava 操作符对照演示，两组页面分组一一对应，便于对比学习。

- **Flow 操作符**：flowOf/asFlow 创建、map/flatMapConcat/buffer 变换、filter/take/distinct 过滤、zip/combine 组合、catch 错误降级
- **RxJava 3 操作符**：just/range 创建、map/flatMap/buffer 变换、filter/take/distinct 过滤、zip/concat 组合、onErrorReturn 错误恢复

### module_jetpack（Jetpack 组件库）

Jetpack 通用基础架构与生命周期数据流组件。

- **Lifecycle**：生命周期感知（DefaultLifecycleObserver、ProcessLifecycleOwner 全局前后台、repeatOnLifecycle / flowWithLifecycle 安全数据流收集）
- **Paging 3**：分页加载（含 RemoteMediator + RemoteKey 方案）
- **ViewModel**：ViewModel / ViewModelProvider.Factory 多种创建方式（标准 Factory、DSL viewModelFactory、SavedStateHandle 与 Fragment 作用域共享）

### module_database（数据库）

结构化与对象型数据库方案。

- **Room**：Jetpack 官方数据库持久化框架，演示 CRUD、Flow 响应式数据流、RxJava Single 异步查询及批量事务操作
- **ObjectBox**：高性能 NoSQL/对象数据库，演示实体存储、Box CRUD 与对象查询

### module_storage（存储）

轻量级键值数据持久化方案。

- **DataStore**：Jetpack 现代化键值存储（Preferences / Proto 两种存储）
- **MMKV**：腾讯开源高性能键值存储，基于 mmap 内存映射

### module_di（依赖注入）

主流依赖注入方案对比与实战。

- **Hilt**：基于 Dagger 2 的编译期依赖注入，覆盖构造注入、接口绑定（@Binds）、第三方对象构建（@Provides）、限定符（@Qualifier）、上下文限定符（@ApplicationContext / @ActivityContext）、作用域生命周期（@Singleton / @ActivityScoped）、@HiltViewModel 及非组件入口点（@EntryPoint）
- **Koin**：基于 Kotlin DSL 的实用主义运行时依赖注入，覆盖 singleOf / factoryOf 声明、接口绑定（bind）、具名限定符（named）、动态参数注入（parametersOf）、Koin ViewModel（viewModelOf / by viewModel）及 Scope 作用域管理

### module_arch（架构模式）

架构模式对比 Demo，覆盖 Android 开发中主流的架构方案。各架构在模块内按 `mvp`、`mvvm`、`mvi`、`compose`、`mavericks` 分包并列管理。

| 模式 | 说明 |
|------|------|
| MVP | Presenter 持有 View 引用，手动桥接 |
| MVVM | LiveData + ViewModel，UseCase 封装单一业务逻辑 |
| MVI | 单向数据流：State → UI → Intent → ViewModel → State |
| Compose MVI | Jetpack Compose + MVI 单向数据流 + SmartRefresh 下拉刷新与分页 |
| Mavericks | 基于 Airbnb [Mavericks](https://airbnb.io/mavericks/) 框架的 MVI 实现，包含不可变状态、状态持久化与异步请求 |

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
- **网络请求**：`lib_network_dio`（DioClient）与 `lib_network_http`（HttpClient）两个独立本地 package，与 Retrofit 共享 `code/message/data` 业务响应和 `code/message/cause` 异常契约，并统一常用 HTTP 方法、请求体和请求取消；支持 SSE（Server-Sent Events）流式传输与 AI 大模型（DeepSeek）打字机对话；日志沿用各自实现且不做脱敏
- **图片加载**：`lib_image_loader` 本地 package（`IImageLoader` 接口 + `ImageLoader` 门面），默认内核 cached_network_image，切换内核调用方零改动，与 Android `lib_imageloader` 结构对齐
- **状态管理**：[Provider](https://pub.dev/packages/provider) / [GetX](https://pub.dev/packages/get) / [BloC](https://pub.dev/packages/flutter_bloc)
- **三方框架**：Toast / Notification / SharedPreferences / ScreenUtil
- **引擎层特性**：CustomPainter 粒子系统、贝塞尔签名板、自定义 RenderObject 环形布局、交错动画、GLSL 片段着色器、沿路径动画、3D 翻转卡片、双指缩放旋转手势识别

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
