# 功能模块（modules/）

> 每个模块有独立的入口 Activity，通过 ARouter 路由导航。

## 模块总览

| 模块 | 职责 | 入口 Activity | 路由前缀 |
|------|------|--------------|---------|
| module_ui | 系统 UI 组件 | UiActivity | /UI |
| module_tab | Tab 导航 | TabActivity | /Tab |
| module_anim | 动画 | AnimActivity | /Animation |
| module_widget | 自定义控件 | WidgetActivity | /Widget |
| module_sync | 异步处理 | SyncActivity | /Sync |
| module_component | 组件交互 | ComponentActivity | /Component |
| module_system | 系统能力 | SystemActivity | /System |
| module_sample | 技术示例 | SampleActivity | /Sample |
| module_features | 业务功能 | FeaturesActivity | /Features |
| module_network | 网络库 | NetWorkActivity | /Network |
| module_okhttp | OkHttp / Retrofit / Retrofit Rx | HttpClientActivity | /OkHttp |
| module_websocket | WebSocket 示例 | WebSocketActivity | /WebSocket |
| module_utils | 工具类 | AndroidUtilsActivity | /Utils |
| module_event | 事件总线 | EventActivity | /Event |
| module_opensource | 第三方库 | OpenSourceActivity | /Opensource |
| module_kotlin | Kotlin 特性 | KotlinActivity | /Kotlin |
| module_jetpack | Jetpack 组件 | JetPackActivity | /JetPack |
| module_arch | 架构模式 | ArchActivity | /Arch |
| module_compose | Compose UI | ComposeActivity | /Compose |
| module_flutter | Flutter 集成 | FlutterMainActivity | /Flutter |

---

## 模块详情

### module_ui（系统 UI 组件）

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
| TransitionActivity | 视图过渡动画（ChangeBounds/Fade/Slide/AutoTransition） |
| RenderEffectActivity | RenderEffect 渲染效果（Android 12+） |
| RenderScriptActivity | RenderScript 图像处理（已废弃） |

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

### module_sync（异步处理）

演示 Android 异步/后台处理机制。

| Activity | 功能 |
|----------|------|
| AsyncTaskActivity | AsyncTask 异步任务 |
| HandlerThreadActivity | HandlerThread 线程间通信 |
| JobSchedulerActivity | JobScheduler 定时任务调度 |

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

### module_sample（技术示例）

演示零散的技术技巧，不属于完整业务场景。

| Activity | 功能 |
|----------|------|
| HookActivity | View Hook 反射技术 |
| TypefaceActivity | 自定义字体加载（Typeface.createFromAsset） |
| FloatWindowActivity | 悬浮窗（WindowManager + 拖拽 + 贴边动画） |

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

### module_network（网络库）

演示各种网络通信库。

| Activity | 功能 |
|----------|------|
| KtorActivity | Ktor HTTP 客户端 |
| KtorClientActivity | 项目级 Ktor Client 封装示例 |
| HttpURLActivity | HttpURLConnection 原生网络请求 |
| VolleyActivity | Volley HTTP 库 |
| VolleyHelperActivity | Volley 封装工具 |
| NanoActivity | NanoHTTPD 内嵌 HTTP 服务器 |

---

### module_okhttp（OkHttp / Retrofit / Retrofit Rx）

按封装库分类演示 OkHttp、Retrofit 和 Retrofit RxJava 的请求方式。

| Activity | 功能 |
|----------|------|
| OkHttpActivity | `lib_okhttp` DSL 与 OkHttp 原生请求示例 |
| RetrofitCallActivity | Retrofit 原生 `Call` 请求示例 |
| RetrofitCallDslActivity | `lib_retrofit` DSL 与 `createApi` 示例 |
| RetrofitRxActivity | Retrofit RxJava 原生订阅示例 |
| RetrofitRxDslActivity | `lib_retrofit_rx` 标准接口与默认策略示例 |
| RxDynamicRequestActivity | `lib_retrofit_rx` 动态请求示例 |

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
| CoilActivity | Coil 图片加载库 |
| GlideActivity | Glide 图片加载库 |
| RealtimeBlurViewActivity | RealtimeBlurView 实时模糊 |
| ObjectBoxActivity | ObjectBox 数据库 |

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
