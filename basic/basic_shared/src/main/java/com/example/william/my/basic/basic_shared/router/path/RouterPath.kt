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
        private const val Service = "/Service"

        const val FileIOUtilsService = "${Service}/FileIOUtilsService"
        const val ImageUtilsService = "${Service}/ImageUtilsService"
        const val ResourceUtilsService = "${Service}/ResourceUtilsService"
    }

    object Fragment {
        private const val Fragment = "/Fragment"

        const val FragmentPrimary = "${Fragment}/fragment/primary"
        const val FragmentPrimaryDark = "${Fragment}/fragment/primary_dark"
        const val FragmentBasicRecycler = "${Fragment}/fragment/basic_recycler"
    }

    // ═══════════════════════════════════════════
    // 基础服务
    // ═══════════════════════════════════════════

    object Server {
        private const val Server = "/Server"

        const val Nano = "${Server}/Nano"
    }

    // ═══════════════════════════════════════════
    // UI 三件套
    // ═══════════════════════════════════════════

    // 标准控件
    object Widget {
        private const val Widget = "/Widget"

        const val Main = "${Widget}/Main"

        const val Appbar = "${Widget}/Appbar"
        const val Dialog = "${Widget}/Dialog"
        const val FlexBox = "${Widget}/FlexBox"
        const val RecyclerView = "${Widget}/RecyclerView"
        const val ViewFlipper = "${Widget}/ViewFlipper"
        const val ViewPager = "${Widget}/ViewPager"
        const val ViewPager2 = "${Widget}/ViewPager2"
        const val WebView = "${Widget}/WebView"
        const val RecyclerViewNested = "${Widget}/RecyclerViewNested"
    }

    // Tab 导航
    object Tab {
        private const val Tab = "/Tab"

        const val Main = "${Tab}/Main"

        const val TabHost = "${Tab}/TabHost"
        const val FrameLayoutTab = "${Tab}/FrameLayoutTab"
        const val ViewPagerTab = "${Tab}/ViewPagerTab"
        const val ViewPager2Tab = "${Tab}/ViewPager2Tab"
        const val BottomNav = "${Tab}/BottomNav"
    }

    // 动画
    object Anim {
        private const val Anim = "/Anim"

        const val Main = "${Anim}/Main"

        const val ObjectAnimator = "${Anim}/ObjectAnimator"
        const val AnimatorSet = "${Anim}/AnimatorSet"
        const val ValueAnimator = "${Anim}/ValueAnimator"
        const val Keyframe = "${Anim}/Keyframe"
        const val Transition = "${Anim}/Transition"
        const val Transition2 = "${Anim}/Transition2"
        const val RenderEffect = "${Anim}/RenderEffect"
        const val RenderScript = "${Anim}/RenderScript"
    }

    // 自定义控件
    object CustomView {
        private const val CustomView = "/CustomView"

        const val Main = "${CustomView}/Main"

        const val AlertDialog = "${CustomView}/AlertDialog"
        const val BlurView = "${CustomView}/BlurView"
        const val InfiniteImage = "${CustomView}/InfiniteImage"
        const val MarqueeView = "${CustomView}/MarqueeView"
        const val Sensor3DView = "${CustomView}/Sensor3DView"
        const val Spinner = "${CustomView}/Spinner"
        const val TitleBar = "${CustomView}/TitleBar"
        const val VerifyCode = "${CustomView}/VerifyCode"
        const val NinePatch = "${CustomView}/NinePatch"
    }

    // ═══════════════════════════════════════════
    // 平台能力
    // ═══════════════════════════════════════════

    // 异步处理
    object Async {
        private const val Async = "/Async"

        const val Main = "${Async}/Main"

        const val AsyncTask = "${Async}/AsyncTask"
        const val HandlerThread = "${Async}/HandlerThread"
        const val JobScheduler = "${Async}/JobScheduler"
        const val WorkManager = "${Async}/WorkManager"
    }

    // 组件交互
    object Component {
        private const val Component = "/Component"

        const val Main = "${Component}/Main"

        const val Broadcast = "${Component}/Broadcast"
        const val ActivityResult = "${Component}/ActivityResult"
        const val OnBackPressed = "${Component}/OnBackPressed"
        const val Service = "${Component}/Service"
        const val Messenger = "${Component}/Messenger"
    }

    // 系统能力
    object System {
        private const val System = "/System"

        const val Main = "${System}/Main"

        const val Notification = "${System}/Notification"
        const val Permission = "${System}/Permission"
        const val SecureKey = "${System}/SecureKey"
        const val FloatWindow = "${System}/FloatWindow"
    }

    // 技术示例
    object Sample {
        private const val Sample = "/Sample"

        const val Main = "${Sample}/Main"

        const val Hook = "${Sample}/Hook"
        const val Typeface = "${Sample}/Typeface"
    }

    // 性能优化
    object Performance {
        private const val Performance = "/Performance"

        const val Main = "${Performance}/Main"

        const val AsyncLayoutInflater = "${Performance}/AsyncLayoutInflater"
        const val IdleHandler = "${Performance}/IdleHandler"
        const val LruCache = "${Performance}/LruCache"

        const val ConcatAdapter = "${Performance}/ConcatAdapter"
        const val RecycledViewPool = "${Performance}/RecycledViewPool"
        const val DiffUtil = "${Performance}/DiffUtil"
    }

    // 业务功能
    object Feature {
        private const val Feature = "/Feature"

        const val Main = "${Feature}/Main"

        const val Turntable = "${Feature}/Turntable"
        const val MicAnimation = "${Feature}/MicAnimation"
        const val Camera = "${Feature}/Camera"
        const val Crop = "${Feature}/Crop"
    }

    // ═══════════════════════════════════════════
    // 工具 / 网络 / 第三方库
    // ═══════════════════════════════════════════

    // OkHttp
    object OkHttp {
        private const val PATH = "/OkHttp"

        const val Main = "${PATH}/Main"
        const val OkHttp = "${PATH}/OkHttp"
    }

    // Retrofit
    object Retrofit {
        private const val PATH = "/Retrofit"

        const val Main = "${PATH}/Main"
        const val RetrofitCall = "${PATH}/RetrofitCall"
        const val RetrofitCallDsl = "${PATH}/RetrofitCallDsl"
        const val RetrofitCoroutine = "${PATH}/RetrofitCoroutine"
        const val RetrofitCoroutineDsl = "${PATH}/RetrofitCoroutineDsl"
        const val RetrofitRx = "${PATH}/RetrofitRx"
        const val RetrofitRxDsl = "${PATH}/RetrofitRxDsl"
    }

    // Rx 动态请求 / 文件传输
    object RxRetrofit {
        private const val PATH = "/RxRetrofit"

        const val Main = "${PATH}/Main"
        const val Request = "${PATH}/RxRequest"
        const val Download = "${PATH}/RxDownload"
        const val Upload = "${PATH}/RxUpload"
    }

    // 工具类
    object Utils {
        private const val Utils = "/Utils"

        const val Main = "${Utils}/Main"

        const val AdaptScreenUtils = "${Utils}/AdaptScreenUtils"
        const val FileIOUtils = "${Utils}/FileIOUtils"
        const val PermissionUtils = "${Utils}/PermissionUtils"
        const val ThreadUtils = "${Utils}/ThreadUtils"
    }

    // HTTP 客户端
    object Http {
        private const val PATH = "/Http"

        const val Main = "${PATH}/Main"
        const val HttpURL = "${PATH}/HttpURL"
        const val Volley = "${PATH}/Volley"
    }

    // Ktor
    object Ktor {
        private const val PATH = "/Ktor"

        const val Main = "${PATH}/Main"
        const val Ktor = "${PATH}/Ktor"
        const val KtorClient = "${PATH}/KtorClient"
    }

    // Socket 模块
    object WebSocket {
        private const val PATH = "/WebSocket"

        const val Main = "${PATH}/Main"
        const val OkHttpWebSocketClient = "${PATH}/OkHttpWebSocketClient"
        const val OkHttpWebSocketClientRx = "${PATH}/OkHttpWebSocketClientRx"
        const val JavaWebSocketClient = "${PATH}/JavaWebSocketClient"
        const val JavaWebSocketClientRx = "${PATH}/JavaWebSocketClientRx"
        const val NettyWebSocketClient = "${PATH}/NettyWebSocketClient"
        const val NettyWebSocketClientRx = "${PATH}/NettyWebSocketClientRx"
    }

    // 第三方库
    object OpenSource {
        private const val OpenSource = "/OpenSource"

        const val Main = "${OpenSource}/Main"

        const val Banner = "${OpenSource}/Banner"
        const val CountdownView = "${OpenSource}/CountdownView"
        const val EasyFloat = "${OpenSource}/EasyFloat"
        const val FlycoTabLayout = "${OpenSource}/FlycoTabLayout"
        const val PhotoView = "${OpenSource}/PhotoView"
        const val PopWindow = "${OpenSource}/PopWindow"
        const val ShadowLayout = "${OpenSource}/ShadowLayout"
        const val SwipeLayout = "${OpenSource}/SwipeLayout"
        const val RealtimeBlurView = "${OpenSource}/BlurView"

        const val Pag = "${OpenSource}/Pag"
        const val Lottie = "${OpenSource}/Lottie"
        const val SVGAPlayer = "${OpenSource}/SVGAPlayer"

        const val CityPicker = "${OpenSource}/CityPicker"
        const val PickerView = "${OpenSource}/PickerView"
        const val PictureSelector = "${OpenSource}/PictureSelector"

        const val LoadSir = "${OpenSource}/LoadSir"
        const val MMKV = "${OpenSource}/MMKV"
        const val PermissionX = "${OpenSource}/PermissionX"
        const val RxJava = "${OpenSource}/RxJava"

        const val Coil = "${OpenSource}/Coil"
        const val Glide = "${OpenSource}/Glide"
    }

    // ═══════════════════════════════════════════
    // 架构 / 语言 / 框架
    // ═══════════════════════════════════════════

    // 架构模式
    object Arch {
        private const val Arch = "/Arch"

        const val Main = "${Arch}/Main"

        const val MVP = "${Arch}/MVP"
        const val MVVM = "${Arch}/MVVM"
        const val MVI = "${Arch}/MVI"

        const val Counter = "${Arch}/Counter"
        const val Mavericks = "${Arch}/Mavericks"
    }

    // 事件总线
    object Event {
        private const val Event = "/Event"

        const val Main = "${Event}/Main"
        const val EventBus = "${Event}/EventBus"
        const val RxEventBus = "${Event}/RxEventBus"
        const val LiveEventBus = "${Event}/LiveEventBus"
        const val FlowEventBus = "${Event}/FlowEventBus"
    }

    // Kotlin 特性
    object Kotlin {
        private const val Kotlin = "/Kotlin"

        const val Main = "${Kotlin}/Main"

        const val Coroutines = "${Kotlin}/Coroutines"
        const val Flow = "${Kotlin}/Flow"
        const val Channel = "${Kotlin}/Channel"
        const val Concurrency = "${Kotlin}/Concurrency"
        const val Delegate = "${Kotlin}/Delegate"
        const val Inline = "${Kotlin}/Inline"
        const val Syntax = "${Kotlin}/Syntax"
    }

    // Jetpack 组件
    object Jetpack {
        private const val Jetpack = "/Jetpack"

        const val Main = "${Jetpack}/Main"

        const val Paging = "${Jetpack}/Paging"
    }

    // 存储
    object Storage {
        private const val Storage = "/Storage"

        const val Main = "${Storage}/Main"

        const val Room = "${Storage}/Room"
        const val ObjectBox = "${Storage}/ObjectBox"
        const val DataStore = "${Storage}/DataStore"
        const val MMKV = "${Storage}/MMKV"
    }

    // 依赖注入 (DI)
    object DI {
        private const val DI = "/DI"

        const val Main = "${DI}/Main"

        const val Hilt = "${DI}/Hilt"
        const val Koin = "${DI}/Koin"
    }

    // ═══════════════════════════════════════════
    // 新范式
    // ═══════════════════════════════════════════

    // Compose
    object Compose {
        private const val Compose = "/Compose"

        const val Main = "${Compose}/Main"

        const val Text = "${Compose}/Text"
        const val Button = "${Compose}/Button"
        const val Image = "${Compose}/Image"
        const val Canvas = "${Compose}/Canvas"

        const val ConstraintLayout = "${Compose}/ConstraintLayout"
        const val HorizontalPager = "${Compose}/HorizontalPager"

        const val BackHandler = "${Compose}/BackHandler"

        const val CompositionLocal = "${Compose}/CompositionLocal"

        const val CoordinatorLayout = "${Compose}/CoordinatorLayout"

        const val Draggable = "${Compose}/Draggable"
        const val DragGestures = "${Compose}/DragGestures"

        const val GuaguaCard = "${Compose}/GuaguaCard"

        const val NavHost = "${Compose}/NavHost"

        const val BottomNavigation = "${Compose}/BottomNavigation"
        const val NavigationBar = "${Compose}/NavigationBar"

        const val Remember = "${Compose}/Remember"

        const val AnchoredDraggable = "${Compose}/AnchoredDraggable"

        const val SmartRefresh = "${Compose}/SmartRefresh"

        const val ScrollableTab = "${Compose}/ScrollableTab"
    }

    // Flutter
    object Flutter {
        private const val Flutter = "/Flutter"

        const val Main = "${Flutter}/Main"
    }
}
