# 功能模块（modules/）

> 每个模块有独立的入口 Activity，通过 ARouter 路由导航。

## 模块总览

| 模块 | 职责 | 入口 Activity | 路由前缀 |
|------|------|--------------|---------|
| module_sync | 异步处理 | SyncActivity | /Sync |
| module_sample | 技术示例 | SampleActivity | /Sample |
| module_component | 组件交互 | ComponentActivity | /Component |
| module_system | 系统能力 | SystemActivity | /System |
| module_widget | 自定义控件 | WidgetActivity | /Widget |
| module_ui | 系统 UI 组件 | UiActivity | /UI |
| module_utils | 工具类 | AndroidUtilsActivity | /Utils |
| module_network | 网络库 | NetWorkActivity | /Network |
| module_opensource | 第三方库 | OpenSourceActivity | /Opensource |
| module_arch | 架构模式 | ArchActivity | /Arch |
| module_event | 事件总线 | EventActivity | /Event |
| module_features | 业务功能 | FeaturesActivity | /Features |
| module_kotlin | Kotlin 特性 | KotlinActivity | /Kotlin |
| module_jetpack | Jetpack 组件 | JetPackActivity | /JetPack |
| module_animation | 动画 | AnimationActivity | /Animation |
| module_compose | Compose UI | ComposeActivity | /Compose |
| module_flutter | Flutter 集成 | FlutterMainActivity | /Flutter |

---

## 模块详情

### module_sync（异步处理）

演示 Android 异步/后台处理机制。

| Activity | 功能 |
|----------|------|
| AsyncTaskActivity | AsyncTask 异步任务 |
| HandlerThreadActivity | HandlerThread 线程间通信 |
| JobSchedulerActivity | JobScheduler 定时任务调度 |

---

### module_sample（技术示例）

演示零散的技术技巧，不属于完整业务场景。

| Activity | 功能 |
|----------|------|
| HookActivity | View Hook 反射技术 |
| TypefaceActivity | 自定义字体加载（Typeface.createFromAsset） |
| FloatWindowActivity | 悬浮窗（WindowManager + 拖拽 + 贴边动画） |

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

---

### module_widget（自定义控件）

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

### module_ui（系统 UI 组件）

演示 Android 标准 UI 组件。

| Activity | 功能 |
|----------|------|
| AppBarActivity | AppBarLayout + CollapsingToolbarLayout + TabLayout |
| DialogActivity | AlertDialog、DatePickerDialog、DialogFragment |
| FlexBoxActivity | FlexboxLayoutManager 弹性布局 |
| FragmentActivity1 | Fragment 基础用法（show/hide 切换） |
| FragmentActivity2 | Fragment 进阶用法 |
| FragmentTabHostActivity | FragmentTabHost 选项卡 |
| FragmentViewPagerActivity | Fragment + ViewPager 联动 |
| RecyclerViewActivity | RecyclerView 多种 LayoutManager |
| RecyclerViewNestedActivity | RecyclerView 嵌套滚动 |
| ViewFlipperActivity | ViewFlipper 翻转切换 |
| ViewPagerActivity | ViewPager 翻页 |
| ViewPagerActivity2 | ViewPager2 + TabLayoutMediator |
| WebViewActivity | WebView 配置、JS 交互、SSL 处理 |

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

### module_network（网络库）

演示各种网络通信库。

| Activity | 功能 |
|----------|------|
| CoilActivity | Coil 图片加载库 |
| KtorActivity | Ktor HTTP 客户端 |
| KtorUtilsActivity | Ktor 封装工具 |
| HttpURLActivity | HttpURLConnection 原生网络请求 |
| VolleyActivity | Volley HTTP 库 |
| VolleyHelperActivity | Volley 封装工具 |
| OkHttpActivity | OkHttp HTTP 库 |
| OkHttpHelperActivity | OkHttp 封装工具 |
| RetrofitActivity | Retrofit HTTP 库 |
| RetrofitHelperActivity | Retrofit 封装工具 |
| RetrofitRxJavaActivity | Retrofit + RxJava 网络请求 |
| RetrofitRxJavaHelperActivity | Retrofit + RxJava 封装工具 |
| RxRetrofitActivity | RxRetrofit 封装工具 |
| OkhttpDownloadActivity | OkHttp 文件下载 |
| RetrofitDownloadActivity | Retrofit 文件下载 |
| RxDownloadActivity | RxDownload 响应式下载 |
| WebSocketActivity | OkHttp WebSocket 长连接 |
| WebSocketUtilsActivity | WebSocket 封装工具 |
| NanoActivity | NanoHTTPD 内嵌 HTTP 服务器 |
| NettyActivity | Netty TCP 网络框架 |
| SocketActivity | Java Socket TCP 通信 |

---

### module_opensource（第三方库）

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
| ImagenActivity | Firebase Imagen AI 图像生成 |
| RealtimeBlurViewActivity | RealtimeBlurView 实时模糊 |
| ObjectBoxActivity | ObjectBox 数据库 |

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

### module_event（事件总线）

演示各种事件总线实现。

| Activity | 功能 |
|----------|------|
| EventBusActivity | EventBus（GreenRobot） |
| RxEventBusActivity | RxEventBus（基于 RxJava） |
| LiveEventBusActivity | LiveEventBus（基于 LiveData） |
| FlowEventBusActivity | FlowEventBus（基于 Kotlin Flow） |

---

### module_features（业务功能）

演示完整的业务场景。

| Activity | 功能 |
|----------|------|
| TurntableActivity | 转盘抽奖（旋转动画） |
| MicAnimationActivity | 麦位动画（自定义 LayoutManager） |
| CameraActivity | CameraX 相机拍照/录像 |
| CropActivity | 图片裁剪（Intent 调用系统裁剪） |

---

### module_kotlin（Kotlin 特性）

演示 Kotlin 语言核心特性。

| Activity | 功能 |
|----------|------|
| CoroutinesActivity | Kotlin 协程 |
| FLowActivity | Kotlin Flow 数据流 |

---

### module_jetpack（Jetpack 组件）

演示 Jetpack 官方组件库。

| Activity | 功能 |
|----------|------|
| DataStoreActivity | DataStore（Preferences + Proto）数据存储 |
| WorkManagerActivity | WorkManager 后台任务调度 |
| RoomActivity | Room 数据库 |
| PagingActivity | Paging3 分页加载 |
| HiltActivity | Hilt 依赖注入 |

---

### module_animation（动画）

演示 Android 原生动画机制。

| Activity | 功能 |
|----------|------|
| AnimatorActivity | ObjectAnimator/ValueAnimator 属性动画 |
| RenderEffectActivity | RenderEffect 渲染效果（Android 12+） |
| RenderScriptActivity | RenderScript 图像处理（已废弃） |
| TransitionFirstActivity | Activity 过渡动画（分解/滑动/淡入/共享元素） |
| TransitionSecondActivity | 过渡动画目标页 |

---

### module_compose（Compose UI）

演示 Jetpack Compose 组件。

| Activity | 功能 |
|----------|------|
| LazyColumnActivity | LazyColumn 列表 |
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
