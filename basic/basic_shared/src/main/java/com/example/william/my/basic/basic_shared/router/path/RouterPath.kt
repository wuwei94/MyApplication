package com.example.william.my.basic.basic_shared.router.path

/**
 * ARouter
 * https://github.com/alibaba/ARouter
 * 一般以模块名称作为一级目录，Activity名称作为二级目录。当分不同包时，前两个要不一样。
 */
object RouterPath {

    const val Directory_Main = "/directory/main"
    const val Category_Main = "/category/main"

    const val PERMISSION_LOGIN = 1 // 登录才能显示的页面

    object Service {
        private const val PATH = "/Service"

        const val FileIOUtilsService = "$PATH/FileIOUtilsService"
        const val ImageUtilsService = "$PATH/ImageUtilsService"
        const val ResourceUtilsService = "$PATH/ResourceUtilsService"
    }

    object Fragment {
        private const val PATH = "/Fragment"

        const val FragmentPrimary = "$PATH/fragment/primary"
        const val FragmentPrimaryDark = "$PATH/fragment/primary_dark"
        const val FragmentBasicRecycler = "$PATH/fragment/basic_recycler"
    }

    // ───────────────────────────────────────────
    // 基础服务
    // ───────────────────────────────────────────
    object Server {
        private const val PATH = "/Server"

        const val Nano = "$PATH/Nano"
        const val JavaWebSocket = "$PATH/JavaWebSocket"
        const val Netty = "$PATH/Netty"
    }

    // ───────────────────────────────────────────
    // UI 交互
    // ───────────────────────────────────────────
    // 标准控件
    object Widget {
        private const val PATH = "/Widget"

        const val Main = "$PATH/Main"

        const val Appbar = "$PATH/Appbar"
        const val Dialog = "$PATH/Dialog"
        const val PopWindow = "$PATH/PopWindow"
        const val FlexBox = "$PATH/FlexBox"
        const val RecyclerView = "$PATH/RecyclerView"
        const val ViewFlipper = "$PATH/ViewFlipper"
        const val ViewPager = "$PATH/ViewPager"
        const val ViewPager2 = "$PATH/ViewPager2"
        const val WebView = "$PATH/WebView"
        const val RecyclerViewNested = "$PATH/RecyclerViewNested"
        const val FloatWindow = "$PATH/FloatWindow"
    }

    // Tab 导航
    object Tab {
        private const val PATH = "/Tab"

        const val Main = "$PATH/Main"

        const val TabHost = "$PATH/TabHost"
        const val FrameLayoutTab = "$PATH/FrameLayoutTab"
        const val ViewPagerTab = "$PATH/ViewPagerTab"
        const val ViewPager2Tab = "$PATH/ViewPager2Tab"
        const val BottomNavigation = "$PATH/BottomNavigation"
        const val FlycoTabLayout = "$PATH/FlycoTabLayout"
    }

    // 动画（原生动画 + 第三方动画库）
    object Anim {
        private const val PATH = "/Anim"

        const val Main = "$PATH/Main"

        const val ObjectAnimator = "$PATH/ObjectAnimator"
        const val AnimatorSet = "$PATH/AnimatorSet"
        const val ValueAnimator = "$PATH/ValueAnimator"
        const val Transition = "$PATH/Transition"
        const val Transition2 = "$PATH/Transition2"
        const val RenderEffect = "$PATH/RenderEffect"
        const val RenderScript = "$PATH/RenderScript"

        // 第三方动画库
        const val Pag = "$PATH/Pag"
        const val Lottie = "$PATH/Lottie"
        const val SvgaPlayer = "$PATH/SvgaPlayer"
    }

    // 自定义控件
    object WidgetCustom {
        private const val PATH = "/WidgetCustom"

        const val Main = "$PATH/Main"

        const val AlertDialog = "$PATH/AlertDialog"
        const val CustomPopWindow = "$PATH/CustomPopWindow"
        const val BlurView = "$PATH/BlurView"
        const val InfiniteImage = "$PATH/InfiniteImage"
        const val MarqueeView = "$PATH/MarqueeView"
        const val Sensor3DView = "$PATH/Sensor3DView"
        const val Spinner = "$PATH/Spinner"
        const val TitleBar = "$PATH/TitleBar"
        const val VerifyCode = "$PATH/VerifyCode"
        const val NinePatch = "$PATH/NinePatch"
    }

    // UI 库（第三方 UI 控件库）
    object WidgetThirdparty {
        private const val PATH = "/WidgetThirdparty"

        const val Main = "$PATH/Main"

        const val Banner = "$PATH/Banner"
        const val CountdownView = "$PATH/CountdownView"
        const val EasyFloat = "$PATH/EasyFloat"
        const val PhotoView = "$PATH/PhotoView"
        const val RealtimeBlurView = "$PATH/RealtimeBlurView"
        const val ShadowLayout = "$PATH/ShadowLayout"
        const val SwipeLayout = "$PATH/SwipeLayout"

        // ── 数据可视化图表 (MPAndroidChart) ──
        const val MPLineChart = "$PATH/MPLineChart"
        const val MPBarChart = "$PATH/MPBarChart"
        const val MPPieChart = "$PATH/MPPieChart"
        const val MPRadarChart = "$PATH/MPRadarChart"
        const val MPChartLinkage = "$PATH/MPChartLinkage"

        // ── 选择器 / 多媒体选择 ──
        const val CityPicker = "$PATH/CityPicker"
        const val PickerView = "$PATH/PickerView"
        const val PictureSelector = "$PATH/PictureSelector"

        // ── 页面状态管理 ──
        const val LoadSir = "$PATH/LoadSir"
        const val LoadSirFragment = "$PATH/LoadSirFragment"
    }

    // 图片加载
    object ImageLoader {
        private const val PATH = "/ImageLoader"

        const val Main = "$PATH/Main"

        const val Coil = "$PATH/Coil"
        const val Glide = "$PATH/Glide"
        const val ImageLoader = "$PATH/ImageLoader"
    }

    // ───────────────────────────────────────────
    // 平台能力与系统服务
    // ───────────────────────────────────────────
    // 异步处理
    object Async {
        private const val PATH = "/Async"

        const val Main = "$PATH/Main"

        const val AsyncTask = "$PATH/AsyncTask"
        const val HandlerThread = "$PATH/HandlerThread"
    }

    // 后台任务调度
    object Scheduler {
        private const val PATH = "/Scheduler"

        const val Main = "$PATH/Main"

        const val JobScheduler = "$PATH/JobScheduler"
        const val WorkManager = "$PATH/WorkManager"
    }

    // 组件交互
    object Component {
        private const val PATH = "/Component"

        const val Main = "$PATH/Main"

        const val Broadcast = "$PATH/Broadcast"
        const val ActivityResult = "$PATH/ActivityResult"
        const val OnBackPressed = "$PATH/OnBackPressed"
        const val Service = "$PATH/Service"
    }

    // 跨进程通信
    object Ipc {
        private const val PATH = "/Ipc"

        const val Main = "$PATH/Main"

        const val AIDL = "$PATH/AIDL"
        const val Messenger = "$PATH/Messenger"
    }

    // 系统服务
    object SystemService {
        private const val PATH = "/SystemService"

        const val Main = "$PATH/Main"

        const val Notification = "$PATH/Notification"
        const val Permission = "$PATH/Permission"
        const val PermissionX = "$PATH/PermissionX"
        const val SecureKey = "$PATH/SecureKey"
    }

    // 技术示例
    object Sample {
        private const val PATH = "/Sample"

        const val Main = "$PATH/Main"

        const val Hook = "$PATH/Hook"
        const val Typeface = "$PATH/Typeface"
    }

    // 性能优化
    object Performance {
        private const val PATH = "/Performance"

        const val Main = "$PATH/Main"

        // 启动与初始化优化
        const val ContentProvider = "$PATH/ContentProvider"
        const val Startup = "$PATH/Startup"
        const val BaselineProfiles = "$PATH/BaselineProfiles"
        const val IdleHandler = "$PATH/IdleHandler"

        // 布局与渲染优化
        const val AsyncLayoutInflater = "$PATH/AsyncLayoutInflater"

        // 内存与列表优化
        const val LruCache = "$PATH/LruCache"
        const val ConcatAdapter = "$PATH/ConcatAdapter"
        const val RecycledViewPool = "$PATH/RecycledViewPool"
        const val DiffUtil = "$PATH/DiffUtil"

        // 运行时监控
        const val JankStats = "$PATH/JankStats"
    }

    // 业务功能
    object Feature {
        private const val PATH = "/Feature"

        const val Main = "$PATH/Main"

        const val Turntable = "$PATH/Turntable"
        const val MicAnimation = "$PATH/MicAnimation"
    }

    // 多媒体
    object Media {
        private const val PATH = "/Media"

        const val Main = "$PATH/Main"
        const val Photo = "$PATH/Photo"
        const val Video = "$PATH/Video"
        const val Crop = "$PATH/Crop"
    }

    // 机器学习 / AI (TensorFlow Lite / 端侧推理)
    object Ml {
        private const val PATH = "/Ml"

        const val Main = "$PATH/Main"
        const val DigitClassifier = "$PATH/DigitClassifier"
        const val ImageClassification = "$PATH/ImageClassification"
        const val GpuDelegate = "$PATH/GpuDelegate"
        const val TensorBasics = "$PATH/TensorBasics"
    }

    // ───────────────────────────────────────────
    // 网络通信
    // ───────────────────────────────────────────
    // HTTP 网络请求（基础、OkHttp、Retrofit、Rx 文件传输）
    object Http {
        private const val PATH = "/Http"

        const val Main = "$PATH/Main"

        // 基础客户端
        const val HttpURL = "$PATH/HttpURL"
        const val Volley = "$PATH/Volley"

        // OkHttp
        const val OkHttp = "$PATH/OkHttp"

        // Retrofit
        const val RetrofitCall = "$PATH/RetrofitCall"
        const val RetrofitCallDsl = "$PATH/RetrofitCallDsl"
        const val RetrofitCoroutine = "$PATH/RetrofitCoroutine"
        const val RetrofitCoroutineDsl = "$PATH/RetrofitCoroutineDsl"
        const val RetrofitRx = "$PATH/RetrofitRx"
        const val RetrofitRxDsl = "$PATH/RetrofitRxDsl"

        // Rx 动态请求 / 文件传输
        const val RxRequest = "$PATH/RxRequest"
        const val RxDownload = "$PATH/RxDownload"
        const val RxUpload = "$PATH/RxUpload"

        // Ktor
        const val Ktor = "$PATH/Ktor"
        const val KtorClient = "$PATH/KtorClient"
    }

    // Socket 长连接通信模块（WebSocket & TCP Socket）
    object Socket {
        private const val PATH = "/Socket"

        const val Main = "$PATH/Main"
        const val OkHttpWebSocketClient = "$PATH/OkHttpWebSocketClient"
        const val OkHttpWebSocketClientRx = "$PATH/OkHttpWebSocketClientRx"
        const val OkHttpWebSocketClientFlow = "$PATH/OkHttpWebSocketClientFlow"
        const val JavaWebSocketClient = "$PATH/JavaWebSocketClient"
        const val JavaWebSocketClientRx = "$PATH/JavaWebSocketClientRx"
        const val JavaWebSocketClientFlow = "$PATH/JavaWebSocketClientFlow"
        const val NettyTcpSocketClient = "$PATH/NettyTcpSocketClient"
        const val NettyTcpSocketClientRx = "$PATH/NettyTcpSocketClientRx"
        const val NettyTcpSocketClientFlow = "$PATH/NettyTcpSocketClientFlow"
    }

    // SSE 流式推送模块
    object SSE {
        private const val PATH = "/SSE"

        const val Main = "$PATH/Main"
        const val OkHttpSseClient = "$PATH/OkHttpSseClient"
        const val OkHttpSseClientRx = "$PATH/OkHttpSseClientRx"
        const val OkHttpSseClientFlow = "$PATH/OkHttpSseClientFlow"
        const val KtorSseClient = "$PATH/KtorSseClient"
        const val KtorSseClientFlow = "$PATH/KtorSseClientFlow"
    }

    // Markdown 富文本渲染与 AI 流式交互模块
    object Markdown {
        private const val PATH = "/Markdown"

        const val Main = "$PATH/Main"
        const val MarkwonBasic = "$PATH/MarkwonBasic"
        const val MarkwonHighlight = "$PATH/MarkwonHighlight"
        const val StreamTypewriter = "$PATH/StreamTypewriter"
        const val AiChat = "$PATH/AiChat"
    }

    // MQTT 模块
    object Mqtt {
        private const val PATH = "/Mqtt"

        const val Main = "$PATH/Main"
        const val HiveMqClient = "$PATH/HiveMqClient"
        const val PahoServiceClient = "$PATH/PahoServiceClient"
    }

    // 蓝牙通信模块
    object Bluetooth {
        private const val PATH = "/Bluetooth"

        const val Main = "$PATH/Main"

        // 原生 SDK 方案
        const val NativeScan = "$PATH/NativeScan"
        const val NativeConnect = "$PATH/NativeConnect"
        const val NativeQueue = "$PATH/NativeQueue"

        // Nordic BLE 方案
        const val NordicScan = "$PATH/NordicScan"
        const val NordicConnect = "$PATH/NordicConnect"
        const val NordicTransfer = "$PATH/NordicTransfer"

        // FastBle 方案
        const val FastScan = "$PATH/FastScan"
        const val FastConnect = "$PATH/FastConnect"

        // RxAndroidBle 方案
        const val RxScan = "$PATH/RxScan"
        const val RxConnect = "$PATH/RxConnect"
    }

    // ───────────────────────────────────────────
    // 架构 / 语言 / 框架
    // ───────────────────────────────────────────
    // 架构模式
    object Arch {
        private const val PATH = "/Arch"

        const val Main = "$PATH/Main"

        const val MVP = "$PATH/MVP"
        const val MVVM = "$PATH/MVVM"
        const val MVI = "$PATH/MVI"
        const val Mavericks = "$PATH/Mavericks"
        const val ComposeMVI = "$PATH/ComposeMVI"
        const val OfflineFirst = "$PATH/OfflineFirst"
        const val SSOT = OfflineFirst
    }

    // 事件总线
    object Event {
        private const val PATH = "/Event"

        const val Main = "$PATH/Main"
        const val EventBus = "$PATH/EventBus"
        const val RxEventBus = "$PATH/RxEventBus"
        const val LiveEventBus = "$PATH/LiveEventBus"
        const val FlowEventBus = "$PATH/FlowEventBus"
    }

    // Kotlin 特性
    object Kotlin {
        private const val PATH = "/Kotlin"

        const val Main = "$PATH/Main"

        const val Coroutines = "$PATH/Coroutines"
        const val Flow = "$PATH/Flow"
        const val Channel = "$PATH/Channel"
        const val Concurrency = "$PATH/Concurrency"
        const val Delegate = "$PATH/Delegate"
        const val Inline = "$PATH/Inline"
        const val Syntax = "$PATH/Syntax"
    }

    // 响应式编程（Flow / RxJava 操作符对照）
    object Reactive {
        private const val PATH = "/Reactive"

        const val Main = "$PATH/Main"

        const val FlowOperators = "$PATH/FlowOperators"
        const val RxJavaOperators = "$PATH/RxJavaOperators"
    }

    // Jetpack 组件
    object Jetpack {
        private const val PATH = "/Jetpack"

        const val Main = "$PATH/Main"

        const val Lifecycle = "$PATH/Lifecycle"
        const val Paging = "$PATH/Paging"
        const val ViewModel = "$PATH/ViewModel"
    }

    // 数据库
    object Database {
        private const val PATH = "/Database"

        const val Main = "$PATH/Main"

        const val Room = "$PATH/Room"
        const val ObjectBox = "$PATH/ObjectBox"
    }

    // 存储
    object Storage {
        private const val PATH = "/Storage"

        const val Main = "$PATH/Main"

        const val DataStore = "$PATH/DataStore"
        const val MMKV = "$PATH/MMKV"
    }

    // 依赖注入 (DI)
    object Di {
        private const val PATH = "/Di"

        const val Main = "$PATH/Main"

        const val Hilt = "$PATH/Hilt"
        const val Koin = "$PATH/Koin"
    }

    // ───────────────────────────────────────────
    // 新 UI 范式（Compose / Flutter）
    // ───────────────────────────────────────────
    // Compose
    object Compose {
        private const val PATH = "/Compose"

        const val Main = "$PATH/Main"

        const val ComposeActivity = "$PATH/ComposeActivity"
        const val ComposeViewActivity = "$PATH/ComposeViewActivity"

        const val Text = "$PATH/Text"
        const val Button = "$PATH/Button"
        const val Image = "$PATH/Image"
        const val Canvas = "$PATH/Canvas"

        const val ConstraintLayout = "$PATH/ConstraintLayout"
        const val HorizontalPager = "$PATH/HorizontalPager"

        const val BackHandler = "$PATH/BackHandler"

        const val CompositionLocal = "$PATH/CompositionLocal"

        const val CoordinatorLayout = "$PATH/CoordinatorLayout"

        const val Draggable = "$PATH/Draggable"
        const val DragGestures = "$PATH/DragGestures"

        const val GuaguaCard = "$PATH/GuaguaCard"

        const val NavHost = "$PATH/NavHost"

        const val NavigationBar = "$PATH/NavigationBar"

        const val Remember = "$PATH/Remember"

        const val AnchoredDraggable = "$PATH/AnchoredDraggable"

        const val SmartRefresh = "$PATH/SmartRefresh"

        const val ScrollableTab = "$PATH/ScrollableTab"

        // ── 数据可视化图表 (Compose Canvas) ──
        const val LineChart = "$PATH/LineChart"
        const val BarChart = "$PATH/BarChart"
        const val PieChart = "$PATH/PieChart"
        const val RadarChart = "$PATH/RadarChart"
        const val ChartLinkage = "$PATH/ChartLinkage"
    }

    // Flutter
    object Flutter {
        private const val PATH = "/Flutter"

        const val Main = "$PATH/Main"
    }
}
