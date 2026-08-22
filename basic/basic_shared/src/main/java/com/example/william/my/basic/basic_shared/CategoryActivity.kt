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
            "ui" -> "UI 组件"
            "network" -> "网络通信"
            "storage" -> "数据存储"
            "system" -> "系统能力"
            "arch" -> "架构模式"
            "kotlin_jetpack" -> "Kotlin & Jetpack"
            "cross_platform" -> "跨平台"
            "third_party" -> "三方库 & 工具"
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
            "cross_platform" -> buildCrossPlatformCategory()
            "third_party" -> buildThirdPartyCategory()
            else -> arrayListOf()
        }
    }

    /**
     * UI 组件
     * - 系统原生：Widget (RecyclerView, ViewPager, Dialog, AppBar, WebView, FlexBox)
     * - 自定义：CustomView (BlurView, MarqueeView, Sensor3D, Spinner, TitleBar, etc.)
     * - 导航：Tab
     * - 动画：Anim
     */
    private fun buildUiCategory(): ArrayList<RouterItem> {
        val items = arrayListOf<RouterItem>()
        items.add(RouterItem("── 系统原生 ──", ""))
        items.add(RouterItem("标准控件", RouterPath.Widget.Main))
        items.add(RouterItem("Tab 导航", RouterPath.Tab.Main))
        items.add(RouterItem("动画", RouterPath.Anim.Main))
        items.add(RouterItem("", ""))
        items.add(RouterItem("── 自定义 ──", ""))
        items.add(RouterItem("自定义控件", RouterPath.CustomView.Main))
        return items
    }

    /**
     * 网络通信
     * - 系统原生：Http (HttpURLConnection, Volley)
     * - 第三方库：OkHttp, Retrofit, Ktor
     * - 扩展能力：RxRetrofit, WebSocket
     */
    private fun buildNetworkCategory(): ArrayList<RouterItem> {
        val items = arrayListOf<RouterItem>()
        items.add(RouterItem("── 系统原生 ──", ""))
        items.add(RouterItem("HTTP 客户端", RouterPath.Http.Main))
        items.add(RouterItem("", ""))
        items.add(RouterItem("── 第三方库 ──", ""))
        items.add(RouterItem("OkHttp", RouterPath.OkHttp.Main))
        items.add(RouterItem("Retrofit", RouterPath.Retrofit.Main))
        items.add(RouterItem("Ktor", RouterPath.Ktor.Main))
        items.add(RouterItem("", ""))
        items.add(RouterItem("── 扩展能力 ──", ""))
        items.add(RouterItem("Rx 动态请求", RouterPath.RxRetrofit.Main))
        items.add(RouterItem("WebSocket", RouterPath.WebSocket.Main))
        return items
    }

    /**
     * 数据存储
     * - Jetpack：Room, DataStore
     * - 第三方库：ObjectBox, MMKV
     */
    private fun buildStorageCategory(): ArrayList<RouterItem> {
        val items = arrayListOf<RouterItem>()
        items.add(RouterItem("── Jetpack ──", ""))
        items.add(RouterItem("Room (关系型数据库)", RouterPath.Storage.Room))
        items.add(RouterItem("DataStore (键值对/Proto)", RouterPath.Storage.DataStore))
        items.add(RouterItem("", ""))
        items.add(RouterItem("── 第三方库 ──", ""))
        items.add(RouterItem("ObjectBox (NoSQL 对象数据库)", RouterPath.Storage.ObjectBox))
        items.add(RouterItem("MMKV (高性能键值存储)", RouterPath.Storage.MMKV))
        return items
    }

    /**
     * 系统能力
     * - 系统原生：Async, Component, System
     */
    private fun buildSystemCategory(): ArrayList<RouterItem> {
        val items = arrayListOf<RouterItem>()
        items.add(RouterItem("── 系统原生 ──", ""))
        items.add(RouterItem("异步处理", RouterPath.Async.Main))
        items.add(RouterItem("组件交互", RouterPath.Component.Main))
        items.add(RouterItem("系统能力", RouterPath.System.Main))
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
     * 跨平台
     * - Compose
     * - Flutter
     */
    private fun buildCrossPlatformCategory(): ArrayList<RouterItem> {
        val items = arrayListOf<RouterItem>()
        items.add(RouterItem("── 跨平台 UI ──", ""))
        items.add(RouterItem("Compose", RouterPath.Compose.Main))
        items.add(RouterItem("Flutter", RouterPath.Flutter.Main))
        return items
    }

    /**
     * 三方库 & 工具
     * - 第三方库：OpenSource
     * - 工具类：Utils
     * - 事件总线：Event
     * - 示例：Sample, Feature
     */
    private fun buildThirdPartyCategory(): ArrayList<RouterItem> {
        val items = arrayListOf<RouterItem>()
        items.add(RouterItem("── 第三方库 ──", ""))
        items.add(RouterItem("第三方库", RouterPath.OpenSource.Main))
        items.add(RouterItem("", ""))
        items.add(RouterItem("── 工具 ──", ""))
        items.add(RouterItem("工具类", RouterPath.Utils.Main))
        items.add(RouterItem("", ""))
        items.add(RouterItem("── 示例 ──", ""))
        items.add(RouterItem("技术示例", RouterPath.Sample.Main))
        items.add(RouterItem("业务功能", RouterPath.Feature.Main))
        return items
    }
}
