package com.example.william.my.basic.basic_shared

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * 分类页面 — 根据分类 ID 显示对应的模块列表
 *
 * 通过 ARouter 传入 category 参数，动态构建对应分类的模块列表。
 */
@Route(path = RouterPath.Category_Main)
class CategoryActivity : RouterRecyclerActivity() {

    @JvmField
    @Autowired(name = "category")
    var category: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getCategoryTitle()
    }

    private fun getCategoryTitle(): String {
        return when (category) {
            "ui" -> "UI 与交互"
            "network" -> "网络通信"
            "storage" -> "数据存储"
            "system" -> "系统能力"
            "arch" -> "架构模式"
            "kotlin_jetpack" -> "Kotlin & Jetpack"
            "compose_flutter" -> "Compose & Flutter"
            "android_utils" -> "AndroidUtils & 三方库"
            "other" -> "Other"
            else -> "模块列表"
        }
    }

    override fun buildRouter(): ArrayList<RouterItem> {
        return when (category) {
            "ui" -> buildUiCategory()
            "network" -> buildNetworkCategory()
            "storage" -> buildStorageCategory()
            "system" -> buildSystemCategory()
            "arch" -> buildArchCategory()
            "kotlin_jetpack" -> buildKotlinJetpackCategory()
            "compose_flutter" -> buildComposeFlutterCategory()
            "android_utils" -> buildAndroidUtilsCategory()
            "sample_feature" -> buildOtherCategory()
            else -> arrayListOf()
        }
    }

    /**
     * UI 与交互
     * - 控件：官方控件（Widget）/ 自定义控件（WidgetCustom）/ 第三方控件（WidgetThirdparty）
     * - 导航：Tab
     * - 动画：Anim
     * - 图片加载：ImageLoader（Coil / Glide / lib_imageloader）
     */
    private fun buildUiCategory(): ArrayList<RouterItem> {
        val items = arrayListOf<RouterItem>()
        items.add(RouterItem("── 控件 ──", ""))
        items.add(RouterItem("官方控件", RouterPath.Widget.Main))
        items.add(RouterItem("自定义控件", RouterPath.WidgetCustom.Main))
        items.add(RouterItem("第三方控件", RouterPath.WidgetThirdparty.Main))
        items.add(RouterItem("", ""))
        items.add(RouterItem("── 导航 ──", ""))
        items.add(RouterItem("Tab 导航", RouterPath.Tab.Main))
        items.add(RouterItem("", ""))
        items.add(RouterItem("── 动画交互 ──", ""))
        items.add(RouterItem("动画交互", RouterPath.Anim.Main))
        items.add(RouterItem("", ""))
        items.add(RouterItem("── 图片加载 ──", ""))
        items.add(RouterItem("图片加载", RouterPath.ImageLoader.Main))
        return items
    }

    /**
     * 网络通信
     * - HTTP 请求（请求-响应）：Http, OkHttp, Retrofit, Ktor, RxRetrofit
     * - 长连接：WebSocket
     */
    private fun buildNetworkCategory(): ArrayList<RouterItem> {
        val items = arrayListOf<RouterItem>()
        items.add(RouterItem("── HTTP 请求 ──", ""))
        items.add(RouterItem("HTTP", RouterPath.Http.Main))
        items.add(RouterItem("Ktor", RouterPath.Ktor.Main))
        items.add(RouterItem("OkHttp", RouterPath.OkHttp.Main))
        items.add(RouterItem("Retrofit", RouterPath.Retrofit.Main))
        items.add(RouterItem("RxRetrofit", RouterPath.RxRetrofit.Main))
        items.add(RouterItem("", ""))
        items.add(RouterItem("── 长连接 ──", ""))
        items.add(RouterItem("WebSocket", RouterPath.WebSocket.Main))
        return items
    }

    /**
     * 数据存储
     * - 数据库：Database（Room, ObjectBox）
     * - 键值存储：Storage（DataStore, MMKV）
     */
    private fun buildStorageCategory(): ArrayList<RouterItem> {
        val items = arrayListOf<RouterItem>()
        items.add(RouterItem("── 数据库 ──", ""))
        items.add(RouterItem("数据库", RouterPath.Database.Main))
        items.add(RouterItem("", ""))
        items.add(RouterItem("── 键值存储 ──", ""))
        items.add(RouterItem("键值存储", RouterPath.Storage.Main))
        return items
    }

    /**
     * 系统能力
     * - 系统原生：Async, Component, System
     * - 多媒体：Camera（CameraX 拍照/录像）
     */
    private fun buildSystemCategory(): ArrayList<RouterItem> {
        val items = arrayListOf<RouterItem>()
        items.add(RouterItem("── 系统原生 ──", ""))
        items.add(RouterItem("异步处理", RouterPath.Async.Main))
        items.add(RouterItem("组件交互", RouterPath.Component.Main))
        items.add(RouterItem("系统能力", RouterPath.System.Main))
        items.add(RouterItem("", ""))
        items.add(RouterItem("── 多媒体 ──", ""))
        items.add(RouterItem("相机", RouterPath.Camera.Main))
        return items
    }

    /**
     * 架构模式
     * - 架构：Arch (MVP, MVVM, MVI, Mavericks)
     * - 依赖注入：DI (Hilt, Koin)
     * - 性能优化：Performance
     */
    private fun buildArchCategory(): ArrayList<RouterItem> {
        val items = arrayListOf<RouterItem>()
        items.add(RouterItem("── 架构模式 ──", ""))
        items.add(RouterItem("架构模式", RouterPath.Arch.Main))
        items.add(RouterItem("Mavericks", RouterPath.Mavericks.Main))
        items.add(RouterItem("", ""))
        items.add(RouterItem("── 依赖注入 ──", ""))
        items.add(RouterItem("依赖注入", RouterPath.DI.Main))
        items.add(RouterItem("", ""))
        items.add(RouterItem("── 事件通信 ──", ""))
        items.add(RouterItem("事件总线", RouterPath.Event.Main))
        items.add(RouterItem("", ""))
        items.add(RouterItem("── 性能优化 ──", ""))
        items.add(RouterItem("性能优化", RouterPath.Performance.Main))
        return items
    }

    /**
     * Kotlin & Jetpack
     * - 语言特性：Kotlin (Coroutines, Flow, Channel, Concurrency, Delegate, Inline, Syntax)
     * - Jetpack 组件：Jetpack (Paging)
     */
    private fun buildKotlinJetpackCategory(): ArrayList<RouterItem> {
        val items = arrayListOf<RouterItem>()
        items.add(RouterItem("── 语言特性 ──", ""))
        items.add(RouterItem("Kotlin 特性", RouterPath.Kotlin.Main))
        items.add(RouterItem("", ""))
        items.add(RouterItem("── Jetpack 组件 ──", ""))
        items.add(RouterItem("Jetpack 组件", RouterPath.Jetpack.Main))
        return items
    }

    /**
     * Compose & Flutter
     * - Compose
     * - Flutter
     */
    private fun buildComposeFlutterCategory(): ArrayList<RouterItem> {
        val items = arrayListOf<RouterItem>()
        items.add(RouterItem("── Compose ──", ""))
        items.add(RouterItem("Compose", RouterPath.Compose.Main))
        items.add(RouterItem("", ""))
        items.add(RouterItem("── Flutter ──", ""))
        items.add(RouterItem("Flutter", RouterPath.Flutter.Main))
        return items
    }

    /**
     * AndroidUtils & 三方库
     * - LoadSir：多状态页面
     * - AndroidUtils：工具类
     */
    private fun buildAndroidUtilsCategory(): ArrayList<RouterItem> {
        val items = arrayListOf<RouterItem>()
        items.add(RouterItem("LoadSir", RouterPath.LoadSir.Main))
        items.add(RouterItem("── AndroidUtils ──", ""))
        items.add(RouterItem("AndroidUtils", RouterPath.Utils.Main))
        return items
    }

    /**
     * Other
     * - 示例：Sample, Feature
     */
    private fun buildOtherCategory(): ArrayList<RouterItem> {
        val items = arrayListOf<RouterItem>()
        items.add(RouterItem("── 示例 ──", ""))
        items.add(RouterItem("技术示例", RouterPath.Sample.Main))
        items.add(RouterItem("业务功能", RouterPath.Feature.Main))
        return items
    }
}
