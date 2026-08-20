package com.example.william.my.basic.basic_shared.router.path

/**
 * ARouter
 * https://github.com/alibaba/ARouter
 * 一般以模块名称作为一级目录，Activity名称作为二级目录。当分不同包时，前两个要不一样。
 */
object RouterPath {

    const val Module_Main = "/module/main"

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

    // UI 组件
    object UI {
        private const val UI = "/UI"

        const val Main = "${UI}/Main"

        const val Appbar = "${UI}/Appbar"
        const val Dialog = "${UI}/Dialog"
        const val FlexBox = "${UI}/FlexBox"
        const val RecyclerView = "${UI}/RecyclerView"
        const val ViewFlipper = "${UI}/ViewFlipper"
        const val ViewPager = "${UI}/ViewPager"
        const val ViewPager2 = "${UI}/ViewPager2"
        const val WebView = "${UI}/WebView"
        const val RecyclerViewNested = "${UI}/RecyclerViewNested"
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
    object Widget {
        private const val Widget = "/Widget"

        const val Main = "${Widget}/Main"

        const val AlertDialog = "${Widget}/AlertDialog"
        const val BlurView = "${Widget}/BlurView"
        const val InfiniteImage = "${Widget}/InfiniteImage"
        const val MarqueeView = "${Widget}/MarqueeView"
        const val Sensor3DView = "${Widget}/Sensor3DView"
        const val Spinner = "${Widget}/Spinner"
        const val TitleBar = "${Widget}/TitleBar"
        const val VerifyCode = "${Widget}/VerifyCode"
        const val NinePatch = "${Widget}/NinePatch"
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
    }

    // 技术示例
    object Sample {
        private const val Sample = "/Sample"

        const val Main = "${Sample}/Main"

        const val Hook = "${Sample}/Hook"
        const val Typeface = "${Sample}/Typeface"
        const val FloatWindow = "${Sample}/FloatWindow"

        const val DiffUtil = "${Sample}/DiffUtil"
        const val LruCache = "${Sample}/LruCache"
        const val IdleHandler = "${Sample}/IdleHandler"
        const val AsyncLayoutInflater = "${Sample}/AsyncLayoutInflater"
        const val RecycledViewPool = "${Sample}/RecycledViewPool"
        const val ConcatAdapter = "${Sample}/ConcatAdapter"
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

    // OkHttp / Retrofit
    object OkHttp {
        private const val PATH = "/OkHttp"

        const val Main = "${PATH}/Main"
        const val OkHttp = "${PATH}/OkHttp"
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

    // 网络库
    object Network {
        private const val PATH = "/Network"

        const val Main = "${PATH}/Main"
        const val HttpURL = "${PATH}/HttpURL"
        const val Volley = "${PATH}/Volley"
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

        const val ObjectBox = "${OpenSource}/ObjectBox"
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
        const val Delegate = "${Kotlin}/Delegate"
        const val Inline = "${Kotlin}/Inline"
    }

    // Jetpack 组件
    object Jetpack {
        private const val Jetpack = "/Jetpack"

        const val Main = "${Jetpack}/Main"

        const val DataStore = "${Jetpack}/DataStore"
        const val WorkManager = "${Jetpack}/WorkManager"
        const val Room = "${Jetpack}/Room"
        const val Paging = "${Jetpack}/Paging"
        const val Hilt = "${Jetpack}/Hilt"
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
