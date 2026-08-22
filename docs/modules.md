# 功能模块（modules/）

> 每个模块有独立的入口 Activity，通过 ARouter 路由导航。

## 模块总览

| 模块 | 职责 | 入口 Activity | 路由前缀 |
|------|------|--------------|---------|
| module_widget | 标准控件 | WidgetMainActivity | /Widget |
| module_tab | Tab 导航 | TabMainActivity | /Tab |
| module_anim | 动画 | AnimMainActivity | /Anim |
| module_custom_view | 自定义控件 | CustomViewMainActivity | /CustomView |
| module_async | 异步处理 | AsyncMainActivity | /Async |
| module_component | 组件交互 | ComponentMainActivity | /Component |
| module_system | 系统能力 | SystemMainActivity | /System |
| module_sample | 技术示例 | SampleMainActivity | /Sample |
| module_performance | 性能优化 | PerformanceMainActivity | /Performance |
| module_feature | 业务功能 | FeatureMainActivity | /Feature |
| module_http | HTTP 客户端 | HttpMainActivity | /Http |
| module_okhttp | OkHttp / Retrofit / Retrofit Rx | OkHttpMainActivity | /OkHttp |
| module_rx_retrofit | Rx 动态请求与文件传输 | RxRetrofitMainActivity | /RxRetrofit |
| module_websocket | WebSocket 示例 | WebSocketMainActivity | /WebSocket |
| module_utils | 工具类 | UtilsMainActivity | /Utils |
| module_event | 事件总线 | EventMainActivity | /Event |
| module_open_source | 第三方库 | OpenSourceMainActivity | /OpenSource |
| module_kotlin | Kotlin 特性 | KotlinMainActivity | /Kotlin |
| module_jetpack | Jetpack 组件 | JetpackMainActivity | /Jetpack |
| module_database | 数据库 | DatabaseMainActivity | /Database |
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
| FlexBoxActivity | FlexboxLayoutManager 弹性布局 |
| RecyclerViewActivity | RecyclerView 多种 LayoutManager |
| RecyclerViewNestedActivity | RecyclerView 嵌套滚动 |
| ViewFlipperActivity | ViewFlipper 翻转切换 |
| ViewPagerActivity | ViewPager 翻页 |
| ViewPager2Activity | ViewPager2 + TabLayoutMediator |
| WebViewActivity | WebView 配置、JS 交互、SSL 处理 |

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

---

### module_anim（动画）

演示 Android 原生动画机制。

| Activity | 功能 |
|----------|------|
| ObjectAnimatorActivity | ObjectAnimator 属性动画（透明度/旋转/缩放/平移） |
| AnimatorSetActivity | AnimatorSet 动画组合（顺序/同时/Builder 编排） |
| ValueAnimatorActivity | ValueAnimator 差值动画 + 插值器对比 + ViewPropertyAnimator |
| KeyframeActivity | Keyframe 关键帧动画 + PropertyValuesHolder |
| TransitionFirstActivity | 视图过渡动画（ChangeBounds/Fade/Slide/AutoTransition） |
| TransitionSecondActivity | 视图过渡动画目标页 |
| RenderEffectActivity | RenderEffect 渲染效果（Android 12+） |
| RenderScriptActivity | RenderScript 图像处理（已废弃） |

---

### module_custom_view（自定义控件）

演示项目自定义实现的 UI 控件。

| Activity | 功能 |
|----------|------|
| AlertDialogActivity | 自定义 IosAlertDialog/BottomSheetDialog |
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

演示 Android 异步/后台处理机制。

| Activity | 功能 |
|----------|------|
| AsyncMainActivity | 模块入口，导航到异步演示页面 |
| AsyncTaskActivity | AsyncTask 异步任务 |
| HandlerThreadActivity | HandlerThread 线程间通信 |
| JobSchedulerActivity | JobScheduler 定时任务调度 |
| WorkManagerActivity | WorkManager 现代化可靠后台任务调度与加急前台服务 |

---

### module_component（组件交互）

演示 Android 四大组件间的交互机制。

| Activity | 功能 |
|----------|------|
| BroadcastActivity | BroadcastReceiver 广播注册与发送 |
| ActivityResultActivity | ActivityResultContracts 新版结果回调 API |
| ActivityResultActivity2 | ActivityResultActivity 的目标页 |
| OnBackPressedActivity | OnBackPressedDispatcher 返回键拦截 |
| ServiceActivity | Service 绑定（bindService）与前台服务 |
| MessengerActivity | Messenger 跨进程通信（IPC） |

---

### module_system（系统能力）

演示 Android 系统级能力。

| Activity | 功能 |
|----------|------|
| NotificationActivity | NotificationChannel 通知渠道创建与通知发送 |
| PermissionActivity | 运行时权限批量申请 |
| SecureKeyActivity | Android Keystore 安全密钥创建与签名 |
| FloatWindowActivity | 系统级悬浮窗（WindowManager + 拖拽 + 贴边吸附动画） |

---

### module_sample（技术示例）

演示零散的技术技巧，不属于完整业务场景。

| Activity | 功能 |
|----------|------|
| HookActivity | View Hook 反射技术 |
| TypefaceActivity | 自定义字体加载（Typeface.createFromAsset） |

---

### module_performance（性能优化）

演示 Android 性能优化与深度调度机制。

| Activity | 功能 |
|----------|------|
| PerformanceMainActivity | 模块入口，导航到性能与列表优化页面 |
| AsyncLayoutInflaterActivity | AsyncLayoutInflater 异步布局解析与视图预加载池 |
| IdleHandlerActivity | IdleHandler 主线程空闲调度（单次/持续监听与延迟初始化） |
| LruCacheActivity | LruCache 内存缓存设计模式（Cache-Aside 回源与容量淘汰） |
| ConcatAdapterActivity | ConcatAdapter 模块化列表组合与 ViewType 隔离刷新 |
| RecycledViewPoolActivity | RecyclerView.RecycledViewPool 跨列表/Tab 共享视图池 |
| DiffUtilActivity | DiffUtil 列表差量计算与 Payload 细粒度局部刷新 |

---

### module_feature（业务功能）

演示完整的业务场景。

| Activity | 功能 |
|----------|------|
| TurntableActivity | 转盘抽奖（旋转动画） |
| MicAnimationActivity | 麦位动画（自定义 LayoutManager） |
| CameraActivity | CameraX 相机拍照/录像 |
| CropActivity | 图片裁剪（Intent 调用系统裁剪） |

---

### module_http（HTTP 客户端）

演示 HttpURLConnection 和 Volley 原生客户端。实际请求页统一基于 `BasicResponseActivity` 内联展示响应与错误。

| Activity | 功能 |
|----------|------|
| HttpMainActivity | 模块入口，导航到各个 HTTP 客户端示例页面 |
| HttpURLActivity | HttpURLConnection 原生网络请求 |
| VolleyActivity | Volley HTTP 请求 |

---

### module_ktor（Ktor）

演示 Ktor 原生客户端和项目级 Ktor Client 封装。实际请求页统一基于 `BasicResponseActivity` 内联展示响应与错误。

| Activity | 功能 |
|----------|------|
| KtorMainActivity | 模块入口，导航到 Ktor 示例页面 |
| KtorActivity | Ktor 原生 HTTP 客户端，区分成功与非 2xx 响应 |
| KtorClientActivity | 项目级 Ktor Client 封装，区分业务失败与传输失败 |

---

### module_okhttp（OkHttp）

演示 OkHttp 原生请求和 `lib_okhttp` DSL 封装。实际请求页统一基于 `BasicResponseActivity` 累积展示响应与错误。

| Activity | 功能 |
|----------|------|
| OkHttpMainActivity | 模块入口，导航到 OkHttp 示例页面 |
| OkHttpActivity | `lib_okhttp` DSL 与 OkHttp 原生请求示例 |

---

### module_retrofit（Retrofit）

按封装方式分类演示 Retrofit 的 Call、协程和 RxJava 调用方式。实际请求页统一基于 `BasicResponseActivity` 累积展示响应与错误。

| Activity | 功能 |
|----------|------|
| RetrofitMainActivity | 模块入口，导航到 Retrofit 示例页面 |
| RetrofitCallActivity | Retrofit 原生 `Call` 请求示例 |
| RetrofitCallDslActivity | `lib_retrofit` DSL 与 `createApi` 示例 |
| RetrofitCoroutineActivity | Retrofit 协程挂起函数原生调用示例 |
| RetrofitCoroutineDslActivity | `lib_retrofit` DSL 与协程挂起函数示例 |
| RetrofitRxActivity | Retrofit RxJava 原生订阅与页面销毁释放示例 |
| RetrofitRxDslActivity | `lib_retrofit_rx` 标准接口与默认策略示例 |

---

### module_rx_retrofit（Rx 动态请求与文件传输）

集中展示 `lib_rx_request`、`lib_rx_download` 和 `lib_rx_upload` 三个 Rx 链式库。三个实际请求页均基于 `BasicResponseActivity`，传输进度在日志区原位更新。

| Activity | 功能 |
|----------|------|
| RxRetrofitMainActivity | 模块入口，导航到动态请求、下载和上传页面 |
| RxRequestActivity | 基于 `BasicResponseActivity` 展示 `lib_rx_request` Form、JSON 与 Multipart 动态请求 |
| RxDownloadActivity | 基于 `BasicResponseActivity` 直接展示单任务 Builder、并发队列 Builder、统一 `RxDownloadCallback`、原位更新进度和 `File` 目录清理 |
| RxUploadActivity | 基于 `BasicResponseActivity` 直接展示 `addFile` / `addFiles` 单多文件 Multipart 上传、`RxUploadCallback`、同步创建少量示例文件和 `File` 目录清理 |

---

### module_websocket（WebSocket 示例）

演示 WebSocket/TCP 通信库的使用，包含 OkHttp WebSocket、Java-WebSocket 和 Netty TCP 三种方案。

| Activity | 功能 |
|----------|------|
| OkHttpWebSocketClientActivity | OkHttp WebSocket 普通版本（连接外部服务器） |
| OkHttpWebSocketClientRxActivity | OkHttp WebSocket RxJava 封装版本 |
| JavaWebSocketClientActivity | Java-WebSocket 普通版本（启动本地服务端 + 连接） |
| JavaWebSocketClientRxActivity | Java-WebSocket RxJava 封装版本 |
| NettyWebSocketClientActivity | Netty TCP 普通版本（启动本地服务端 + 连接） |
| NettyWebSocketClientRxActivity | Netty TCP RxJava 封装版本 |

服务端 Service：

| Service | 功能 | 默认端口 |
|---------|------|----------|
| JavaWebSocketServerService | Java-WebSocket 服务端 | 5566 |
| NettyWebSocketServerService | Netty TCP 服务端 | 5567 |

---

### module_utils（工具类）

演示 BlankJ utilcode 工具库的各种工具类。

| Activity | 功能 |
|----------|------|
| AdaptScreenUtilsActivity | 屏幕适配工具 |
| FileIOUtilsActivity | 文件读写工具 |
| PermissionUtilsActivity | 权限请求工具 |
| ThreadUtilsActivity | 线程池工具 |

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

### module_open_source（第三方库）

演示第三方开源库的使用。

| Activity | 功能 |
|----------|------|
| BannerActivity | Youth Banner 轮播图 |
| CityPickerActivity | CityPicker 城市选择器 |
| CountdownActivity | CountdownView 倒计时 |
| EasyFloatActivity | EasyFloat 悬浮窗 |
| FlycoTabLayoutActivity | FlycoTabLayout 标签页 |
| PhotoViewActivity | PhotoView 图片手势缩放 |
| PickerViewActivity | PickerView 选择器 |
| PictureSelectorActivity | PictureSelector 图片选择器 |
| PopWindowActivity | CustomPopWindow 弹窗 |
| ShadowLayoutActivity | ShadowLayout 阴影布局 |
| SwipeLayoutActivity | AndroidSwipeLayout 侧滑 |
| PagActivity | PAG 动画播放器 |
| LottieActivity | Lottie 动画播放器 |
| SvgaPlayerActivity | SVGA 动画播放器 |
| LoadSirActivity | LoadSir 加载状态页 |
| MMKVActivity | MMKV 键值存储 |
| PermissionXActivity | PermissionX 权限请求 |
| RxJavaActivity | RxJava3 响应式编程 |
| CoilActivity | Coil 图片加载库 |
| GlideActivity | Glide 图片加载库 |
| RealtimeBlurViewActivity | RealtimeBlurView 实时模糊 |

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

### module_jetpack（Jetpack 组件）

演示 Jetpack 官方组件库。

| Activity | 功能 |
|----------|------|
| DataStoreActivity | DataStore（Preferences + Proto）数据存储 |
| PagingActivity | Paging3 分页加载 |

---

### module_database（数据库）

演示 Android 移动端主流数据库持久化方案。

| Activity | 功能 |
|----------|------|
| DatabaseMainActivity | 模块入口，导航到 Room 与 ObjectBox 示例 |
| RoomActivity | Room 数据库 CRUD、协程 Flow 响应式查询与 RxJava 响应式流 |
| ObjectBoxActivity | ObjectBox 高性能对象数据库增删查改 |

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

演示 Android 架构模式。

| Activity | 功能 |
|----------|------|
| MvpActivity | MVP 架构模式 |
| MvvmActivity | MVVM 架构模式 |
| MviActivity | MVI 架构模式 |
| CounterActivity | Mavericks 计数器示例 |
| MavericksActivity | Mavericks 文章列表示例 |

---

### module_compose（Compose UI）

演示 Jetpack Compose 组件。

| Activity | 功能 |
|----------|------|
| ComposeMainActivity | Compose 模块入口导航页 |
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

---

### module_flutter（Flutter 集成）

Flutter 嵌入页入口。

| Activity | 功能 |
|----------|------|
| FlutterMainActivity | Flutter 嵌入页 |
