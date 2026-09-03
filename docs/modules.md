# 功能模块（modules/）

> 每个模块有独立的入口 Activity，通过 ARouter 路由导航。

## 目录结构

项目采用两级目录结构，按技术领域分组：

```
目录页面 (DirectoryActivity)
├── UI 交互
│   ├── 控件：标准控件、自定义控件、第三方控件
│   ├── 导航：Tab 导航
│   ├── 动画：原生动画与第三方动画库（PAG、Lottie、SVGA）
│   └── 图片加载：Coil、Glide、lib_imageloader
├── 网络通信
│   ├── HTTP 请求：HTTP 网络请求（基础、OkHttp、Retrofit、RxRetrofit、Ktor）
│   ├── Socket 通信：WebSocket & TCP Socket
│   └── 消息队列：MQTT（发布 / 订阅）
├── 数据存储
│   ├── 数据库：Room、ObjectBox
│   └── 键值存储：DataStore、MMKV
├── 系统能力
│   ├── 系统原生：异步处理、任务调度、组件交互、系统服务
│   ├── 多媒体：相机、图片裁剪
│   ├── 跨进程通信：AIDL、Messenger
│   └── 机器学习：TensorFlow Lite、LiteRT
├── 架构与工程
│   ├── 架构模式：MVP、MVVM、MVI、Mavericks
│   ├── 依赖注入：Hilt、Koin
│   ├── 事件通信：事件总线
│   ├── 响应式编程：Reactive（Flow / RxJava 操作符对照）
│   └── 性能优化
├── Kotlin & Jetpack
│   ├── 语言特性：Kotlin
│   └── Jetpack 组件
├── Compose & Flutter
│   └── Compose、Flutter
└── Sample & Feature
    └── 技术示例、业务功能
```

## 模块总览

| 模块 | 职责 | 入口 Activity | 路由前缀 |
|------|------|--------------|---------|
| module_widget | 标准控件 | WidgetMainActivity | /Widget |
| module_tab | Tab 导航 | TabMainActivity | /Tab |
| module_anim | 动画（原生与第三方） | AnimMainActivity | /Anim |
| module_widget_custom | 自定义控件 | WidgetCustomMainActivity | /WidgetCustom |
| module_widget_thirdparty | 第三方 UI 库 | WidgetThirdpartyMainActivity | /WidgetThirdparty |
| module_markdown | Markdown 渲染与 AI 流式交互 | MarkdownMainActivity | /Markdown |
| module_async | 异步处理 | AsyncMainActivity | /Async |
| module_scheduler | 后台任务调度 | SchedulerMainActivity | /Scheduler |
| module_component | 组件交互 | ComponentMainActivity | /Component |
| module_ipc | 跨进程通信 | IpcMainActivity | /Ipc |
| module_system_service | 系统服务 | SystemServiceMainActivity | /SystemService |
| module_media | 多媒体 | MediaMainActivity | /Media |
| module_ml | 机器学习（TFLite / LiteRT） | MlMainActivity | /Ml |
| module_sample | 技术示例 | SampleMainActivity | /Sample |
| module_performance | 性能优化 | PerformanceMainActivity | /Performance |
| module_feature | 业务功能 | FeatureMainActivity | /Feature |
| module_http | HTTP 网络请求（含 Ktor） | HttpMainActivity | /Http |
| module_sse | SSE 流式推送 | SseMainActivity | /SSE |
| module_socket | WebSocket & TCP Socket | SocketMainActivity | /Socket |
| module_mqtt | MQTT 发布 / 订阅 | MqttMainActivity | /Mqtt |
| module_bluetooth | 蓝牙通信（BLE 客户端） | BluetoothMainActivity | /Bluetooth |
| module_event | 事件总线 | EventMainActivity | /Event |
| module_imageloader | 图片加载 | ImageLoaderMainActivity | /ImageLoader |
| module_kotlin | Kotlin 特性 | KotlinMainActivity | /Kotlin |
| module_reactive | 响应式编程 | ReactiveMainActivity | /Reactive |
| module_jetpack | Jetpack 组件 | JetpackMainActivity | /Jetpack |
| module_database | 数据库 | DatabaseMainActivity | /Database |
| module_storage | 存储 | StorageMainActivity | /Storage |
| module_di | 依赖注入 | DiMainActivity | /DI |
| module_arch | 架构模式 | ArchMainActivity | /Arch |
| module_compose | Compose UI | ComposeMainActivity | /Compose |
| module_flutter | Flutter 集成 | FlutterMainActivity | /Flutter |

---

## 模块详情

### module_widget（标准控件）

演示 Android 标准 UI 组件。

| Activity | 功能 |
|----------|------|
| AppBarActivity | AppBarLayout + CollapsingToolbarLayout + TabLayout |
| DialogActivity | AlertDialog、DatePickerDialog、DialogFragment |
| PopWindowActivity | PopupWindow 弹窗 |
| FlexBoxActivity | FlexboxLayoutManager 弹性布局 |
| RecyclerViewActivity | RecyclerView 多种 LayoutManager |
| RecyclerViewNestedActivity | RecyclerView 嵌套滚动 |
| ViewFlipperActivity | ViewFlipper 翻转切换 |
| ViewPagerActivity | ViewPager 翻页 |
| ViewPager2Activity | ViewPager2 + TabLayoutMediator |
| WebViewActivity | WebView 配置、JS 交互、SSL 处理 |
| FloatWindowActivity | 系统级悬浮窗（WindowManager + 拖拽 + 贴边吸附动画） |

---

### module_tab（Tab 导航）

演示 Fragment + Tab 导航的多种实现方式。

| Activity | 功能 |
|----------|------|
| TabHostActivity | FragmentTabHost 选项卡 |
| FrameLayoutTabActivity | FrameLayout + RadioGroup Tab |
| ViewPagerTabActivity | ViewPager + NoScrollViewPager + RadioGroup 联动 |
| ViewPager2TabActivity | ViewPager2 + RadioGroup 联动 |
| BottomNavActivity | BottomNavigationView + Fragment（底部导航栏） |
| FlycoTabLayoutActivity | FlycoTabLayout 标签页（Sliding / Common / Segment 三种样式） |

---

### module_anim（动画）

演示 Android 原生动画机制与第三方动画库（PAG、Lottie、SVGA）。

| Activity | 功能 |
|----------|------|
| AnimMainActivity | 模块入口，导航到原生动画与第三方动画库页面 |
| ObjectAnimatorActivity | ObjectAnimator 属性动画（透明度/旋转/缩放/平移） |
| AnimatorSetActivity | AnimatorSet 动画组合（顺序/同时/Builder 编排） |
| ValueAnimatorActivity | ValueAnimator 差值动画 + 插值器对比 + ViewPropertyAnimator |
| TransitionFirstActivity | 视图过渡动画（ChangeBounds/Fade/Slide/AutoTransition） |
| TransitionSecondActivity | 视图过渡动画目标页 |
| RenderEffectActivity | RenderEffect 渲染效果（Android 12+） |
| RenderScriptActivity | RenderScript 图像处理（已废弃） |
| PagActivity | PAG 动画播放器 |
| LottieActivity | Lottie 动画播放器 |
| SvgaPlayerActivity | SVGA 动画播放器 |

---

### module_widget_custom（自定义控件）

演示项目自定义实现的 UI 控件。

| Activity | 功能 |
|----------|------|
| AlertDialogActivity | 自定义 IosAlertDialog/BottomSheetDialog |
| CustomPopWindowActivity | 自定义 PopupWindow 封装 |
| BlurViewActivity | 自定义高斯模糊控件 |
| InfiniteImageActivity | 无限循环图片控件 |
| MarqueeViewActivity | 自定义跑马灯滚动控件 |
| Sensor3DActivity | 3D 传感器倾斜控件 |
| SpinnerActivity | 自定义下拉选择器控件 |
| TitleBarActivity | 自定义标题栏控件 |
| VerifyCodeActivity | 自定义验证码输入控件 |
| NinePatchActivity | 九宫格拉伸图片控件 |

---

### module_async（异步处理）

演示 Android 线程/协程层面的异步机制。

| Activity | 功能 |
|----------|------|
| AsyncMainActivity | 模块入口，导航到异步演示页面 |
| AsyncTaskActivity | AsyncTask 异步任务 |
| HandlerThreadActivity | HandlerThread 线程间通信 |

---

### module_scheduler（后台任务调度）

演示 Android 后台任务调度，聚焦「任务调度」主题：系统原生 JobScheduler 与 Jetpack WorkManager。

| Activity | 功能 |
|----------|------|
| SchedulerMainActivity | 模块入口，导航到 JobScheduler、WorkManager 示例页面 |
| JobSchedulerActivity | JobScheduler 定时任务调度 |
| WorkManagerActivity | WorkManager 现代化可靠后台任务调度与加急前台服务 |

---

### module_component（组件交互）

演示 Android 原生四大组件与 Jetpack 现代交互契约机制（原生三大组件：Broadcast / Service；Jetpack 交互契约：ActivityResultContracts / OnBackPressedDispatcher；ContentProvider 启动初始化详见 `module_performance`）。

| Activity | 功能 |
|----------|------|
| BroadcastActivity | BroadcastReceiver 广播注册与发送 |
| ActivityResultActivity | ActivityResultContracts 新版结果回调 API |
| ActivityResultActivity2 | ActivityResultActivity 的目标页 |
| OnBackPressedActivity | OnBackPressedDispatcher 返回键拦截 |
| ServiceActivity | Service 绑定（bindService）与前台服务 |

---

### module_ipc（跨进程通信）

演示 Android 跨进程通信（IPC）的两种基于 Binder 的方案。

| Activity | 功能 |
|----------|------|
| IpcMainActivity | 模块入口，导航到 AIDL、Messenger 示例页面 |
| AIDLActivity | AIDL 跨进程通信（绑定、解绑、远程调用） |
| MessengerActivity | Messenger 跨进程通信（IPC） |

---

### module_system_service（系统服务）

演示 Android 系统级服务与底层安全密钥机制。

| Activity | 功能 |
|----------|------|
| SystemServiceMainActivity | 模块入口，导航到系统服务与安全密钥示例页面 |
| NotificationActivity | NotificationChannel 通知渠道创建与通知发送 |
| PermissionActivity | 运行时权限申请（Jetpack ActivityResult 契约模式） |
| PermissionXActivity | 运行时权限申请（PermissionX 链式开源库模式） |
| SecureKeyActivity | Android Keystore 安全密钥创建与签名（硬件保护与 ECDSA 签名） |

---

### module_sample（技术示例）

**定位：技术技巧与底层探索**。收纳不依赖特定业务场景的单点技术技巧、底层 API 机制探索与实验性代码（如 Hook 反射、自定义 Typeface 等），保持轻量独立，不污染通用架构模块。

| Activity | 功能 |
|----------|------|
| SampleMainActivity | 模块入口，导航到技术技巧示例页面 |
| HookActivity | View Hook 反射技术 |
| TypefaceActivity | 自定义字体加载（Typeface.createFromAsset） |

---

### module_performance（性能优化）

演示 Android 启动与初始化、布局解析与列表渲染等多维度的性能优化与深度调度机制。

| Activity | 功能 |
|----------|------|
| PerformanceMainActivity | 模块入口，导航到启动优化、布局解析与列表复用等性能优化页面 |
| ContentProviderActivity | ContentProvider 自动初始化机制（利用 onCreate 优先时序自动捕获 Context、跨组件数据共享与多 Provider 冷启动弊端分析） |
| StartupActivity | Jetpack App Startup 初始化组件（单个 InitializationProvider 聚合托管、DAG 依赖拓扑排序、手动按需延迟加载与多 Provider 耗时分析） |
| BaselineProfilesActivity | Jetpack Baseline Profiles 基线配置文件（ART 运行时 AOT 预编译提速、ProfileInstaller 本地诊断写入、CUJ 关键路径规则与 ADB 测试指令） |
| IdleHandlerActivity | IdleHandler 主线程空闲调度（单次/持续监听与延迟初始化） |
| AsyncLayoutInflaterActivity | AsyncLayoutInflater 异步布局解析与视图预加载池 |
| LruCacheActivity | LruCache 内存缓存设计模式（Cache-Aside 回源与容量淘汰） |
| ConcatAdapterActivity | ConcatAdapter 模块化列表组合与 ViewType 隔离刷新 |
| RecycledViewPoolActivity | RecyclerView.RecycledViewPool 跨列表/Tab 共享视图池 |
| DiffUtilActivity | DiffUtil 列表差量计算与 Payload 细粒度局部刷新 |

---

### module_feature（业务功能）

**定位：实战业务场景脱敏**。收纳从公司真实商业项目中抽离、脱敏出的典型复合业务场景（如抽奖转盘、麦位动画等），展示端到端的真实业务落地能力（UI + 业务逻辑 + 状态联动），不追求强行抽象为纯通用控件。

| Activity | 功能 |
|----------|------|
| FeatureMainActivity | 模块入口，导航到抽奖转盘与麦位动画示例页面 |
| TurntableActivity | 转盘抽奖（旋转动画） |
| MicAnimationActivity | 麦位动画（自定义 LayoutManager） |

---

### module_media（多媒体）

演示 Android 原生底层多媒体能力（硬件/系统 API），按用例（UseCase）拆分为独立示例页。第三方图片选择器（如 PictureSelector）归入 `module_widget_thirdparty`。

| Activity | 功能 |
|----------|------|
| MediaMainActivity | 模块入口，导航到拍照、录像、图片裁剪示例页面 |
| MediaPhotoActivity | CameraX 拍照（ImageCapture 用例：预览取景 + 单张照片捕获） |
| MediaVideoActivity | CameraX 录像（VideoCapture 用例：多级分辨率回退 + 录像回放） |
| CropActivity | 图片裁剪（Intent 调用系统裁剪，支持图库选择与拍照） |

---

### module_ml（机器学习 / AI）

演示 Google 官方轻量级端侧推理框架（TensorFlow Lite / LiteRT）的核心技术链路、真实模型推理、硬件加速与落地实践。详细技术指南参见 [docs/ml.md](file:///E:/StudioProjects/MyApplication/docs/ml.md)。

| Activity | 功能 |
|----------|------|
| MlMainActivity | 模块入口，导航到 MNIST 手写识别、MobileNet 图像分类、GPU 硬件加速与张量底层操作页面 |
| TFLiteDigitClassifierActivity | MNIST 手写数字实时识别（自定义 FingerDrawView 画板涂鸦、28x28 灰度提取、实时 TFLite 推理与 0~9 置信度条形图） |
| TFLiteImageClassificationActivity | MobileNet 图像物体分类（4:3 黄金比例无黑边、Center-Crop 等比裁剪、1000 类别纯中文标签、Top-5 识别结果与 CPU/GPU 切换） |
| TFLiteGpuDelegateActivity | CPU 多核 vs GPU 硬件加速跑分实测（2×3 对照矩阵：单核 1T、单核+XNN、多核 4T、多核+XNN、GPU Delegate 30 轮真实性能实测） |
| TFLiteTensorBasicsActivity | TFLite 张量底层操作与内存架构（FlatBuffers 零拷贝 mmap、Direct ByteBuffer 内存排布、动态张量调整与 Native 资源防泄漏） |

---

### module_http（HTTP 网络请求）

集中演示基础 HTTP 客户端（HttpURLConnection、Volley）、OkHttp、Retrofit（Call/协程/RxJava）以及 Rx 动态请求与文件传输。实际请求页统一基于 `BasicResponseActivity` 内联展示响应、日志与原位更新进度。

| Activity | 功能 |
|----------|------|
| HttpMainActivity | 模块入口，导航到各个 HTTP 请求示例页面 |
| HttpURLActivity | HttpURLConnection 原生网络请求 |
| VolleyActivity | Volley HTTP 请求 |
| OkHttpActivity | `lib_okhttp` DSL 与 OkHttp 原生请求示例 |
| RetrofitCallActivity | Retrofit 原生 `Call` 请求示例 |
| RetrofitCallDslActivity | `lib_retrofit` DSL 与 `createApi` 示例 |
| RetrofitCoroutineActivity | Retrofit 协程挂起函数原生调用示例 |
| RetrofitCoroutineDslActivity | `lib_retrofit` DSL 与协程挂起函数示例 |
| RetrofitRxActivity | Retrofit RxJava 原生订阅与页面销毁释放示例 |
| RetrofitRxDslActivity | `lib_retrofit_rx` 标准接口与默认策略示例 |
| RxRequestActivity | 基于 `BasicResponseActivity` 展示 `lib_rx_request` Form、JSON 与 Multipart 动态请求 |
| RxDownloadActivity | 基于 `BasicResponseActivity` 直接展示单任务 Builder、并发队列 Builder、统一 `RxDownloadCallback`、原位更新进度和 `File` 目录清理 |
| RxUploadActivity | 基于 `BasicResponseActivity` 直接展示 `addFile` / `addFiles` 单多文件 Multipart 上传、`RxUploadCallback`、同步创建少量示例文件和 `File` 目录清理 |
| KtorActivity | Ktor 原生请求（POST 多部分表单与挂起函数） |
| KtorClientActivity | 基于 `lib_ktor` 的项目级 Ktor 客户端封装（`postFormResponse`） |

---

### module_sse（SSE 流式传输 / DeepSeek AI 对话）

演示 Server-Sent Events (SSE) 协议在现代 AI 大模型（DeepSeek 官方 API）对话场景下的单次流式请求与响应落地，包含 OkHttp SSE（普通版本、RxJava 封装版本、Coroutines Flow 封装版本）与 Ktor SSE（普通版本、Coroutines Flow 封装版本）。

| Activity | 功能 |
|----------|------|
| OkHttpSseClientActivity | OkHttp SSE 原始回调版本（DeepSeek POST Prompt $\rightarrow$ 逐 Token 流式输出 $\rightarrow$ 收到 `[DONE]` 结束） |
| OkHttpSseClientRxActivity | OkHttp SSE RxJava 封装版本（DeepSeek POST Observable 流 $\rightarrow$ 收到 `[DONE]` 触发 `onComplete()`） |
| OkHttpSseClientFlowActivity | OkHttp SSE Coroutines Flow 封装版本（DeepSeek POST Flow $\rightarrow$ 协程生命周期感知与逐字打字机效果） |
| KtorSseClientActivity | Ktor SSE 原始回调版本（DeepSeek POST Prompt $\rightarrow$ KtorSseListener 主线程回调） |
| KtorSseClientFlowActivity | Ktor SSE Coroutines Flow 封装版本（Ktor Client + SSE Plugin 发起 DeepSeek POST 大模型流式请求） |

---

### module_socket（WebSocket & TCP Socket）

演示 WebSocket/TCP 通信库的使用，包含 OkHttp WebSocket、Java-WebSocket 和 Netty TCP 三种方案。

| Activity | 功能 |
|----------|------|
| OkHttpWebSocketClientActivity | OkHttp WebSocket 普通版本（连接外部服务器） |
| OkHttpWebSocketClientRxActivity | OkHttp WebSocket RxJava 封装版本 |
| OkHttpWebSocketClientFlowActivity | OkHttp WebSocket Coroutines Flow 封装版本 |
| JavaWebSocketClientActivity | Java-WebSocket 普通版本（连接外部服务器） |
| JavaWebSocketClientRxActivity | Java-WebSocket RxJava 封装版本 |
| JavaWebSocketClientFlowActivity | Java-WebSocket Coroutines Flow 封装版本 |
| NettyTcpSocketClientActivity | Netty TCP 普通版本（ARouter ServerService 启动本地服务端 + 连接） |
| NettyTcpSocketClientRxActivity | Netty TCP RxJava 封装版本（ARouter ServerService 启动本地服务端 + 连接） |
| NettyTcpSocketClientFlowActivity | Netty TCP Coroutines Flow 封装版本（ARouter ServerService 启动本地服务端 + 连接） |

服务端 Service：

| Service | 功能 | 默认端口 |
|---------|------|----------|
| JavaWebSocketServerService | Java-WebSocket 服务端 | 5566 |
| NettyWebSocketServerService | Netty TCP 服务端 | 5567 |

---

### module_mqtt（MQTT 发布 / 订阅）

演示 MQTT（Message Queuing Telemetry Transport）发布/订阅消息队列协议，使用 EMQX 公共 Broker，提供两种客户端实现对比。

| Activity | 功能 |
|----------|------|
| MqttMainActivity | 模块入口，导航到两个 MQTT 客户端示例页面 |
| HiveMqClientActivity | HiveMQ MQTT Client 异步 API：连接、订阅、发布（QoS 0/1/2）与断开连接 |
| PahoServiceClientActivity | Eclipse Paho Android Service（MqttAndroidClient，绑定 MqttService）：连接、订阅、发布（QoS 0/1/2）与断开连接 |

---

### module_bluetooth（蓝牙通信）

演示低功耗蓝牙（Bluetooth Low Energy, BLE）客户端开发，包含原生 SDK、Nordic 官方库、FastBle 链式封装与 RxAndroidBle 响应式流四套方案对比。

| Activity | 功能 |
|----------|------|
| BluetoothMainActivity | 模块入口，导航到四大技术方案页面 |
| BleNativeScanActivity | 原生 BLE 扫描（动态权限、扫描模式、RSSI 实时更新与广播数据解析） |
| BleNativeConnectActivity | 原生 BLE 连接与 GATT 交互（服务发现、MTU 协商、读写特征与 Notify 订阅） |
| BleNativeQueueActivity | 原生 BLE 协程队列与分包传输（解决并发冲突、Channel 串行排队与大包 Chunking） |
| BleNordicScanActivity | Nordic BLE 扫描与设备发现（UART 服务过滤与扫描最佳实践） |
| BleNordicConnectActivity | Nordic BLE 连接与挂起调用（BleManager 工业级架构、自动重试与 suspend 读写） |
| BleNordicTransferActivity | Nordic BLE 大数据流式传输（.split() 自动分包切割与 .merge() 流式拼包） |
| BleFastScanActivity | FastBle 链式扫描与规则过滤（BleScanRuleConfig 配置与单例扫描） |
| BleFastConnectActivity | FastBle 链式连接与读写回调（BleGattCallback、UUID 驱动读写与 Notify） |
| BleRxScanActivity | RxAndroidBle 响应式扫描与过滤（Observable 数据流、RxJava 操作符流控） |
| BleRxConnectActivity | RxAndroidBle 响应式连接与流控（establishConnection 管道、flatMap 串联与自动释放） |

---

### module_event（事件总线）

演示各种事件总线实现。

| Activity | 功能 |
|----------|------|
| EventBusActivity | EventBus（GreenRobot） |
| RxEventBusActivity | RxEventBus（基于 RxJava） |
| LiveEventBusActivity | LiveEventBus（基于 LiveData） |
| FlowEventBusActivity | FlowEventBus（基于 Kotlin Flow） |

---

### module_imageloader（图片加载）

图片加载专项模块，集中展示 Coil、Glide 以及项目级 `lib_imageloader` 网络图片管道与加载引擎封装（图片手势缩放 UI 控件 PhotoView 归入 `module_widget_thirdparty`）。

| Activity | 功能 |
|----------|------|
| ImageLoaderMainActivity | 模块入口，导航到各个图片加载器示例页面 |
| CoilActivity | Coil 3 原生加载（基础 / crossfade / placeholder / error） |
| GlideActivity | Glide 4 原生加载（circleCrop / RoundedCorners / centerCrop / crossFade） |
| ImageLoaderActivity | `lib_imageloader` 统一封装（IImageLoader 接口 + Coil / Glide 内核无感切换） |

---

### module_widget_thirdparty（UI 库）

演示第三方 UI 控件库。集中展示可复用的 View/ViewGroup 控件、复合 UI 选择器库（如 PhotoView 图片手势缩放 View、PictureSelector 图片选择器）以及第三方页面多状态管理框架（LoadSir）。

| Activity | 功能 |
|----------|------|
| WidgetThirdpartyMainActivity | 模块入口，导航到各 UI 库示例页面 |
| BannerActivity | Youth Banner 轮播图 |
| CountdownActivity | CountdownView 倒计时 |
| EasyFloatActivity | EasyFloat 悬浮窗 |
| PhotoViewActivity | PhotoView 图片手势缩放 |
| ShadowLayoutActivity | ShadowLayout 阴影布局 |
| SwipeLayoutActivity | AndroidSwipeLayout 侧滑 |
| RealtimeBlurViewActivity | RealtimeBlurView 实时模糊 |
| CityPickerActivity | CityPicker 城市选择器 |
| PickerViewActivity | Android-PickerView 滚动选择器 |
| PictureSelectorActivity | PictureSelector 图片选择器 |
| LoadSirActivity | LoadSir 多状态页面管理（Loading / Error / Success 切换与点击重试） |
| LoadSirFragmentActivity | LoadSir 在 Fragment 场景下的多状态管理与布局复用 |
| MPLineChartActivity | MPAndroidChart 折线图（双曲线收支对比、Cubic 贝塞尔平滑、渐变面积填充与 MarkerView 联动） |
| MPBarChartActivity | MPAndroidChart 柱状图（季度目标 vs 实际销售额分组柱状图、柱顶圆角与达成率联动） |
| MPPieChartActivity | MPAndroidChart 饼图（成本预算环形甜甜圈图、中心总额标注与扇区触控外扩动画） |
| MPRadarChartActivity | MPAndroidChart 雷达图（六维技术能力评估模型、双数据集半透明覆盖与顶点评级） |
| MPChartLinkageActivity | MPAndroidChart 多图表全景联动看板（时间轴主控折线图联动部门柱状图与渠道饼图） |

---

### module_markdown（Markdown 渲染与 AI 流式交互）

演示原生高性能 Markdown 渲染、Prism4j 多语言代码高亮、打字机流控与大模型 AI 聊天流式交互。

| Activity | 功能 |
|----------|------|
| MarkdownMainActivity | 模块入口，导航到 Markdown 渲染与 AI 交互各示例页面 |
| MarkwonBasicActivity | Markwon 原生高性能 Markdown 渲染（基础排版、GFM 表格、任务清单、HTML、图片与自定义主题） |
| MarkwonHighlightActivity | Prism4j 多语言代码语法高亮（Kotlin / Java / Python / JS / SQL / Shell / C++、Darkula 与 Light 主题、协程异步高亮） |
| StreamTypewriterActivity | 流式 Markdown 打字机与未闭合语法容错（TypewriterEngine 自适应时钟、MarkdownStreamFixer 自动补齐、呼吸光标与突发推流） |
| AiChatActivity | AI 流式对话完整实战（RecyclerView Payload 局部增量刷新、智能吸底滚动、手势打断与悬浮回底、多轮对话与代码复制） |

---

### module_kotlin（Kotlin 特性）

演示 Kotlin 语言核心特性。

| Activity | 功能 |
|----------|------|
| CoroutinesActivity | Kotlin 协程（线程切换、async/await 并发、supervisorScope 异常隔离、超时取消与 CoroutineExceptionHandler） |
| FlowActivity | Kotlin Flow 数据流（冷流收集、数据变换、zip/combine 双流组合、debounce/flatMapLatest 防抖搜索、StateFlow/SharedFlow 热流与 catch/retry 重试） |
| ChannelActivity | Kotlin Channel 与回调桥接（4 种缓冲模式、produce 生产消费模型、callbackFlow 回调防漏桥接与 channelFlow 跨协程并发发射） |
| ConcurrencyActivity | Kotlin 协程高阶并发控制（Mutex 非阻塞互斥锁、Semaphore 信号量限流、select 竞速多路复用与 NonCancellable 清理保障） |
| MyDelegateActivity | Kotlin 委托机制（类委托、自定义 ReadWriteProperty、by lazy、observable、vetoable、by map、属性重定向与 notNull） |
| MyInlineActivity | Kotlin 内联函数（作用域函数 with/let/run/also/apply、reified 泛型实化、自定义扩展、noinline 与 crossinline） |
| MySyntaxActivity | Kotlin 现代语法与 DSL（操作符重载 +/*/[ ]/in/invoke、中缀函数 infix fun、解构声明、密封接口 Sealed Interface 穷举与类型安全 DSL 构建器） |

---

### module_reactive（响应式编程）

Kotlin Flow 与 RxJava 操作符对照演示，两组页面分组一一对应，便于对比学习。

| Activity | 功能 |
|----------|------|
| FlowOperatorsActivity | Kotlin Flow 操作符（flowOf/asFlow 创建、map/flatMapConcat/buffer 变换、filter/take/distinct 过滤、zip/combine 组合、catch 错误降级） |
| RxJavaOperatorsActivity | RxJava 3 操作符（just/range 创建、map/flatMap/buffer 变换、filter/take/distinct 过滤、zip/concat 组合、onErrorReturn 错误恢复） |

---

### module_jetpack（Jetpack 组件）

承载未被具体业务/技术领域大类吸纳的**通用 Jetpack 架构与生命周期数据流组件**（不包含 UI 控件）。

> **说明**：具备明确主题的 Jetpack 组件均按「主题优先」归入对应业务/技术模块：
> - `Room` → `module_database`（数据存储）
> - `DataStore` → `module_storage`（键值存储）
> - `CameraX` → `module_media`（多媒体底层能力）
> - `Hilt` → `module_di`（依赖注入）
> - `WorkManager` → `module_scheduler`（后台任务调度）
> - `App Startup` / `Baseline Profiles` / `AsyncLayoutInflater` / `ConcatAdapter` / `DiffUtil` → `module_performance`（启动、布局与渲染性能优化）

| Activity | 功能 |
|----------|------|
| JetpackMainActivity | 模块入口，导航到 Lifecycle、Paging3 与 ViewModel 示例页面 |
| LifecycleActivity | Lifecycle 生命周期感知（DefaultLifecycleObserver、ProcessLifecycleOwner 全局前后台、repeatOnLifecycle / flowWithLifecycle 安全数据流收集） |
| PagingActivity | Paging3 分页加载 |
| ViewModelActivity | ViewModel 多种创建方式（默认 / 标准 Factory / DSL Factory，含 Fragment 作用域共享） |

---

### module_database（数据库）

演示 Android 关系型 / 对象型数据库方案。

| Activity | 功能 |
|----------|------|
| DatabaseMainActivity | 模块入口，导航到 Room、ObjectBox 示例 |
| RoomActivity | Room 数据库 CRUD、协程 Flow 响应式查询与 RxJava 响应式流 |
| ObjectBoxActivity | ObjectBox 高性能对象数据库增删查改 |

---

### module_storage（存储）

演示 Android 键值存储方案。

| Activity | 功能 |
|----------|------|
| StorageMainActivity | 模块入口，导航到 DataStore、MMKV 示例 |
| DataStoreActivity | DataStore（Preferences + Proto）数据存储 |
| MMKVActivity | MMKV 高性能键值存储（写入 / 读取 / 删除 / 清空） |

---

### module_di（依赖注入）

演示 Android 主流依赖注入框架（Hilt 与 Koin）的对比与实践。

| Activity | 功能 |
|----------|------|
| DiMainActivity | 模块入口，导航到 Hilt 与 Koin 示例 |
| HiltActivity | 基于 `BasicResponseActivity` 展示 Hilt 构造注入、接口绑定、第三方构建、限定符、上下文、作用域、ViewModel 及 EntryPoint |
| KoinActivity | 基于 `BasicResponseActivity` 展示 Koin 4.x DSL 声明、singleOf/factoryOf、接口绑定、具名限定符、动态传参、ViewModel 及 Scope |

---

### module_arch（架构模式）

演示 Android 主流架构模式（MVP、MVVM、MVI、Compose MVI 与 Airbnb Mavericks）。各架构模式在 `module_arch` 下分包并列管理。

| Activity | 功能 |
|----------|------|
| ArchMainActivity | 模块入口，导航到 MVP / MVVM / MVI / Compose MVI / Mavericks 架构模式示例 |
| MvpActivity | MVP 架构模式（ArticleContract + ArticlePresenter） |
| MvvmActivity | MVVM 架构模式（ArticleLiveDataViewModel + UseCase） |
| MviActivity | MVI 架构模式（StateFlow + ArticleIntent + ArticleUiEffect） |
| ComposeMviActivity | Compose MVI 架构模式（Jetpack Compose + StateFlow + SmartRefresh Compose） |
| MavericksActivity | Mavericks 架构模式（Airbnb MVI，MavericksState + MavericksViewModel + MavericksRepository） |

---

### module_compose（Compose UI）

演示 Jetpack Compose 组件。

| Activity | 功能 |
|----------|------|
| ComposeMainActivity | Compose 模块入口导航页 |
| ComposeActivity | Compose 入门（声明式 UI / 组件复用） |
| ComposeViewActivity | ComposeView 在传统 View 中嵌入 Compose 混排 |
| TextActivity | Text 组件 |
| ButtonActivity | Button 组件 |
| ImageActivity | Image 组件 |
| CanvasActivity | Canvas 自定义绘制 |
| EffectActivity | 副作用 API |
| ConstraintLayoutActivity | ConstraintLayout |
| HorizontalPagerActivity | HorizontalPager |
| CompositionLocalActivity | CompositionLocal |
| CoordinatorLayoutActivity | CoordinatorLayout |
| DraggableActivity | draggable 修饰符 |
| DragGesturesActivity | detectDragGestures 手势 |
| GuaguaCardActivity | 刮刮卡效果 |
| BackHandlerActivity | BackHandler |
| NavHostActivity | Navigation NavHost |
| BottomNavigationActivity | BottomNavigation（Material） |
| NavigationBarActivity | NavigationBar（Material3） |
| RememberActivity | 状态管理（remember） |
| AnchoredDraggableActivity | 锚定拖拽 |
| SmartRefreshActivity | 下拉刷新 |
| ScrollableTabActivity | 可滚动标签页 |
| ComposeLineChartActivity | Compose Canvas 折线图（贝塞尔平滑曲线、渐变面积填充与 pointerInput 十字吸附 Tooltip） |
| ComposeBarChartActivity | Compose Canvas 柱状图（drawRoundRect 分组圆角矩形柱、触控高亮与达成率分析） |
| ComposePieChartActivity | Compose Canvas 环形饼图（drawArc 甜甜圈图、animateFloatAsState 扇区外扩动画与中心数字） |
| ComposeRadarChartActivity | Compose Canvas 雷达图（4 层蛛网正多边形自绘、双数据集对比与能力等级评估） |
| ComposeChartLinkageActivity | Compose Canvas 多图表联动看板（主控时间轴状态提升、跨组件数据流与实时重组） |

---

### module_flutter（Flutter 集成）

Flutter 嵌入页入口。

| Activity | 功能 |
|----------|------|
| FlutterMainActivity | Flutter 嵌入页 |
