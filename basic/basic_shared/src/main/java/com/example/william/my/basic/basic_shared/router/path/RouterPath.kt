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
    object Sync {
        private const val Sync = "/Sync"

        const val Main = "${Sync}/Main"

        const val AsyncTask = "${Sync}/AsyncTask"
        const val HandlerThread = "${Sync}/HandlerThread"
        const val JobScheduler = "${Sync}/JobScheduler"
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
    }

    // 业务功能
    object Features {
        private const val Features = "/Features"

        const val Main = "${Features}/Main"

        object Business {
            private const val Business = "${Features}/Business"

            const val Turntable = "${Business}/Turntable"
            const val MicAnimation = "${Business}/MicAnimation"
        }

        object Media {
            private const val Media = "${Features}/Media"

            const val Camera = "${Media}/Camera"
            const val Crop = "${Media}/Crop"
        }
    }

    // ═══════════════════════════════════════════
    // 工具 / 网络 / 第三方库
    // ═══════════════════════════════════════════

    // OkHttp / Retrofit
    object OkHttp {
        private const val PATH = "/OkHttp"

        const val Main = "${PATH}/Main"

        object OkHttpLib {
            private const val OKHTTP_PATH = "${PATH}/OkHttpLib"

            const val OkHttp = "${OKHTTP_PATH}/OkHttp"
        }

        object Retrofit {
            private const val RETROFIT_PATH = "${PATH}/Retrofit"

            const val RetrofitCall = "${RETROFIT_PATH}/RetrofitCall"
            const val RetrofitCallDsl = "${RETROFIT_PATH}/RetrofitCallDsl"
        }

        object RetrofitRx {
            private const val RETROFIT_RX_PATH = "${PATH}/RetrofitRx"

            const val RetrofitRx = "${RETROFIT_RX_PATH}/RetrofitRx"
            const val RetrofitRxDsl = "${RETROFIT_RX_PATH}/RetrofitRxDsl"
        }
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

        object Ktor {
            private const val KTOR_PATH = "${PATH}/Ktor"

            const val Ktor = "${KTOR_PATH}/Ktor"
            const val KtorClient = "${KTOR_PATH}/KtorClient"
        }

        object HttpURL {
            private const val HTTP_URL_PATH = "${PATH}/HttpURL"

            const val HttpURL = "${HTTP_URL_PATH}/HttpURL"
        }

        object Volley {
            private const val VOLLEY_PATH = "${PATH}/Volley"

            const val Volley = "${VOLLEY_PATH}/Volley"
        }
    }

    // Socket 模块
    object WebSocket {
        private const val PATH = "/WebSocket"

        const val Main = "${PATH}/Main"

        object OkHttpWebSocket {
            private const val WS_PATH = "${PATH}/OkHttpWebSocket"

            const val OkHttpWebSocketClient = "${WS_PATH}/OkHttpWebSocketClient"
            const val OkHttpWebSocketClientRx = "${WS_PATH}/OkHttpWebSocketClientRx"
        }

        object JavaWebSocket {
            private const val SOCKET_PATH = "${PATH}/JavaWebSocket"
            const val JavaWebSocketClient = "${SOCKET_PATH}/JavaWebSocketClient"
            const val JavaWebSocketClientRx = "${SOCKET_PATH}/JavaWebSocketClientRx"
        }

        object NettyWebSocket {
            private const val NETTY_PATH = "${PATH}/NettyWebSocket"
            const val NettyWebSocketClient = "${NETTY_PATH}/NettyWebSocketClient"
            const val NettyWebSocketClientRx = "${NETTY_PATH}/NettyWebSocketClientRx"
        }
    }

    // 第三方库
    object OpenSource {
        private const val Opensource = "/Opensource"

        const val Main = "${Opensource}/Main"

        object Widget {
            private const val Widget = "${Opensource}/Widget"

            const val Banner = "${Widget}/Banner"
            const val CountdownView = "${Widget}/CountdownView"
            const val EasyFloat = "${Widget}/EasyFloat"
            const val FlycoTabLayout = "${Widget}/FlycoTabLayout"
            const val PhotoView = "${Widget}/PhotoView"
            const val PopWindow = "${Widget}/PopWindow"
            const val ShadowLayout = "${Widget}/ShadowLayout"
            const val SwipeLayout = "${Widget}/SwipeLayout"
            const val RealtimeBlurView = "${Widget}/BlurView"
        }

        object Animation {
            private const val Animation = "${Opensource}/Animation"

            const val Pag = "${Animation}/Pag"
            const val Lottie = "${Animation}/Lottie"
            const val SVGAPlayer = "${Animation}/SVGAPlayer"
        }

        object Selector {
            private const val Selector = "${Opensource}/Selector"

            const val CityPicker = "${Selector}/CityPicker"
            const val PickerView = "${Selector}/PickerView"
            const val PictureSelector = "${Selector}/PictureSelector"
        }

        object Utils {
            private const val Utils = "${Opensource}/Utils"

            const val LoadSir = "${Utils}/LoadSir"
            const val MMKV = "${Utils}/MMKV"
            const val PermissionX = "${Utils}/PermissionX"
            const val RxJava = "${Utils}/RxJava"
        }

        object ImageLoader {
            private const val ImageLoader = "${Opensource}/ImageLoader"

            const val Coil = "${ImageLoader}/Coil"
            const val Glide = "${ImageLoader}/Glide"
        }

        object Database {
            private const val Database = "${Opensource}/Database"

            const val ObjectBox = "${Database}/ObjectBox"
        }
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
        const val EventBus = "${Event}/event_bus"
        const val RxEventBus = "${Event}/rx_event_bus"
        const val LiveEventBus = "${Event}/live_event_bus"
        const val FlowEventBus = "${Event}/flow_event_bus"
    }

    // Kotlin 特性
    object Kotlin {
        private const val Kotlin = "/Kotlin"

        const val Main = "${Kotlin}/Main"

        const val Coroutines = "${Kotlin}/Coroutines"
        const val Flow = "${Kotlin}/Flow"
    }

    // Jetpack 组件
    object JetPack {
        private const val JetPack = "/JetPack"

        const val Main = "${JetPack}/Main"

        const val DataStore = "${JetPack}/DataStore"
        const val WorkManager = "${JetPack}/WorkManager"
        const val Room = "${JetPack}/Room"
        const val Paging = "${JetPack}/Paging"
        const val Hilt = "${JetPack}/Hilt"
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
