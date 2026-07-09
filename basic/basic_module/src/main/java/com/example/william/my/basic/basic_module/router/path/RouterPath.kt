package com.example.william.my.basic.basic_module.router.path

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

    // 示例
    object Sample {
        private const val Sample = "/Sample"

        const val Main = "${Sample}/Main"

        object Background {
            private const val Background = "${Sample}/Background"

            const val AsyncTask = "${Background}/AsyncTask"
            const val HandlerThread = "${Background}/HandlerThread"
            const val JobScheduler = "${Background}/JobScheduler"
        }

        object Communication {
            private const val Communication = "${Sample}/Communication"

            const val Broadcast = "${Communication}/Broadcast"
            const val Messenger = "${Communication}/Messenger"
            const val Service = "${Communication}/Service"
        }

        object System {
            private const val System = "${Sample}/System"

            const val ActivityResult = "${System}/ActivityResult"
            const val OnBackPressed = "${System}/OnBackPressed"
            const val Notification = "${System}/Notification"
            const val Permission = "${System}/Permission"
            const val Typeface = "${System}/Typeface"
        }
    }

    // 动画
    object Animation {

        private const val Animation = "/Animation"

        const val Main = "${Animation}/Main"

        const val Animator = "${Animation}/Animator"
        const val Transition = "${Animation}/Transition"
        const val Transition2 = "${Animation}/Transition2"
        const val RenderEffect = "${Animation}/RenderEffect"
        const val RenderScript = "${Animation}/RenderScript"
    }

    // UI 控件
    object UI {

        private const val UI = "/UI"

        const val Main = "${UI}/Main"

        const val Appbar = "${UI}/Appbar"
        const val Dialog = "${UI}/Dialog"
        const val FlexBox = "${UI}/FlexBox"
        const val Fragment1 = "${UI}/Fragment1"
        const val Fragment2 = "${UI}/Fragment2"
        const val FragmentTabHost = "${UI}/FragmentTabHost"
        const val RecyclerView = "${UI}/RecyclerView"
        const val ViewFlipper = "${UI}/ViewFlipper"
        const val ViewPager = "${UI}/ViewPager"
        const val ViewPager2 = "${UI}/ViewPager2"
        const val WebView = "${UI}/WebView"
        const val FragmentViewPager = "${UI}/FragmentViewPager"
        const val RecyclerViewNested = "${UI}/RecyclerViewNested"
    }

    // 控件
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

    // 工具库
    object Utils {
        private const val Utils = "/Utils"

        const val Main = "${Utils}/Main"

        const val AdaptScreenUtils = "${Utils}/AdaptScreenUtils"
        const val FileIOUtils = "${Utils}/FileIOUtils"
        const val PermissionUtils = "${Utils}/PermissionUtils"
        const val ThreadUtilsActivity = "${Utils}/ThreadUtilsActivity"
    }

    // 网络库
    object Network {
        private const val PATH = "/Network"

        const val Main = "${PATH}/Main"

        object Coil {
            private const val COIL_PATH = "${PATH}/Coil"

            const val Coil = "${COIL_PATH}/Coil"
        }

        object Ktor {
            private const val KTOR_PATH = "${PATH}/Ktor"

            const val Ktor = "${KTOR_PATH}/Ktor"
            const val KtorUtils = "${KTOR_PATH}/KtorUtils"
        }

        object HttpURL {
            private const val HTTP_URL_PATH = "${PATH}/HttpURL"

            const val HttpURL = "${HTTP_URL_PATH}/HttpURL"
        }

        object Volley {
            private const val VOLLEY_PATH = "${PATH}/Volley"

            const val Volley = "${VOLLEY_PATH}/Volley"
            const val VolleyHelper = "${VOLLEY_PATH}/VolleyHelper"
        }

        object OkHttp {
            private const val OKHTTP_PATH = "${PATH}/OkHttp"

            const val OkHttp = "${OKHTTP_PATH}/OkHttp"
            const val OkHttpHelper = "${OKHTTP_PATH}/OkHttpHelper"
        }

        object Retrofit {
            private const val RETROFIT_PATH = "${PATH}/Retrofit"

            const val Retrofit = "${RETROFIT_PATH}/Retrofit"
            const val RetrofitHelper = "${RETROFIT_PATH}/RetrofitHelper"

            const val RetrofitRxJava = "${RETROFIT_PATH}/RetrofitRxJava"
            const val RetrofitRxJavaHelper = "${RETROFIT_PATH}/RetrofitRxJavaHelper"

            const val RxRetrofit = "${RETROFIT_PATH}/RxRetrofit"
        }

        object Download {
            private const val DOWNLOAD_PATH = "${PATH}/Download"

            const val OkHttpDownload = "${DOWNLOAD_PATH}/OkHttpDownload"
            const val RetrofitDownload = "${DOWNLOAD_PATH}/RetrofitDownload"
            const val RxDownload = "${DOWNLOAD_PATH}/RxDownload"
        }

        object WebSocket {
            private const val WS_PATH = "${PATH}/WebSocket"

            const val WebSocket = "${WS_PATH}/WebSocket"
            const val WebSocketUtils = "${WS_PATH}/WebSocketUtils"
        }

        object Socket {
            private const val SOCKET_PATH = "${PATH}/Socket"

            const val Nano = "${SOCKET_PATH}/Nano"
            const val Netty = "${SOCKET_PATH}/Netty"
            const val Socket = "${SOCKET_PATH}/Socket"
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

        object Image {
            private const val Image = "${Opensource}/Image"

            const val Imagen = "${Image}/Imagen"
        }

        object Database {
            private const val Database = "${Opensource}/Database"

            const val ObjectBox = "${Database}/ObjectBox"
        }
    }

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

    // 业务功能
    object Features {
        private const val Features = "/Features"

        const val Main = "${Features}/Main"

        object Business {
            private const val Business = "${Features}/Business"

            const val Turntable = "${Business}/Turntable"
            const val MicAnimation = "${Business}/MicAnimation"
            const val SecureKey = "${Business}/SecureKey"
        }

        object Media {
            private const val Media = "${Features}/Media"

            const val Camera = "${Media}/Camera"
            const val Crop = "${Media}/Crop"
            const val FloatWindow = "${Media}/FloatWindow"
            const val Hook = "${Media}/Hook"
        }
    }

    // Kotlin 特性
    object Kotlin {
        private const val Kotlin = "/Kotlin"

        const val Main = "${Kotlin}/Main"

        const val Coroutines = "${Kotlin}/Coroutines"
        const val Flow = "${Kotlin}/Flow"
    }

    // Jetpack 组件库
    object JetPack {
        private const val JetPack = "/JetPack"

        const val Main = "${JetPack}/Main"

        const val DataStore = "${JetPack}/DataStore"
        const val WorkManager = "${JetPack}/WorkManager"
        const val Room = "${JetPack}/Room"
        const val Paging = "${JetPack}/Paging"
        const val Hilt = "${JetPack}/Hilt"
    }

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
