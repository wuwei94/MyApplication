package com.example.william.my.basic.basic_shared.category

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

    private fun getCategoryTitle(): String = when (category) {
        "ui" -> "UI 交互"
        "network" -> "网络通信"
        "storage" -> "数据存储"
        "system" -> "系统能力"
        "engineering" -> "架构与工程"
        "kotlin_jetpack" -> "Kotlin & Jetpack"
        "compose_flutter" -> "Compose & Flutter"
        "sample_feature" -> "Sample & Feature"
        else -> "模块列表"
    }

    override fun buildRouter(): ArrayList<RouterItem> = when (category) {
        "ui" -> buildUiCategory()
        "network" -> buildNetworkCategory()
        "storage" -> buildStorageCategory()
        "system" -> buildSystemCategory()
        "engineering" -> buildEngineeringCategory()
        "kotlin_jetpack" -> buildKotlinJetpackCategory()
        "compose_flutter" -> buildComposeFlutterCategory()
        "sample_feature" -> buildSampleFeatureCategory()
        else -> arrayListOf()
    }

    /**
     * UI 交互
     * - 控件：官方控件（Widget）/ 自定义控件（WidgetCustom）/ 第三方控件（WidgetThirdparty）
     * - 导航：Tab
     * - 动画：Anim（原生动画 + 第三方动画库）
     * - 图片加载：ImageLoader（Coil / Glide / lib_image_loader）
     */
    private fun buildUiCategory(): ArrayList<RouterItem> {
        val items = arrayListOf<RouterItem>()
        items.add(RouterItem("── 控件 ──", ""))
        items.add(RouterItem("官方控件", RouterPath.Widget.Main))
        items.add(RouterItem("自定义控件", RouterPath.WidgetCustom.Main))
        items.add(RouterItem("第三方 UI 库", RouterPath.WidgetThirdparty.Main))
        items.add(RouterItem("Markdown 渲染与 AI 流式交互", RouterPath.Markdown.Main))
        items.add(RouterItem("", ""))
        items.add(RouterItem("── 导航 ──", ""))
        items.add(RouterItem("Tab 导航", RouterPath.Tab.Main))
        items.add(RouterItem("", ""))
        items.add(RouterItem("── 动画 ──", ""))
        items.add(RouterItem("动画", RouterPath.Anim.Main))
        items.add(RouterItem("", ""))
        items.add(RouterItem("── 图片加载 ──", ""))
        items.add(RouterItem("图片加载", RouterPath.ImageLoader.Main))
        return items
    }

    /**
     * 网络通信
     * - HTTP 请求（请求-响应）：HTTP 网络请求（基础 / OkHttp / Retrofit / RxRetrofit / Ktor）
     * - 长连接：WebSocket
     * - 消息队列：MQTT（发布 / 订阅）
     */
    private fun buildNetworkCategory(): ArrayList<RouterItem> {
        val items = arrayListOf<RouterItem>()
        items.add(RouterItem("── HTTP ──", ""))
        items.add(RouterItem("HTTP 网络请求", RouterPath.Http.Main))
        items.add(RouterItem("", ""))
        items.add(RouterItem("── 流式推送 ──", ""))
        items.add(RouterItem("SSE 流式推送", RouterPath.SSE.Main))
        items.add(RouterItem("", ""))
        items.add(RouterItem("── Socket ──", ""))
        items.add(RouterItem("WebSocket & TCP Socket", RouterPath.Socket.Main))
        items.add(RouterItem("", ""))
        items.add(RouterItem("── 消息队列 ──", ""))
        items.add(RouterItem("MQTT", RouterPath.Mqtt.Main))
        items.add(RouterItem("", ""))
        items.add(RouterItem("── 蓝牙通信 ──", ""))
        items.add(RouterItem("蓝牙（BLE 客户端）", RouterPath.Bluetooth.Main))
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
     * - 系统原生：Async, Component, SystemService, Scheduler
     * - 多媒体：Media（CameraX 拍照 / 录像 / 图片裁剪）
     * - 跨进程通信：Ipc（AIDL / Messenger）
     */
    private fun buildSystemCategory(): ArrayList<RouterItem> {
        val items = arrayListOf<RouterItem>()
        items.add(RouterItem("── 系统原生 ──", ""))
        items.add(RouterItem("异步处理", RouterPath.Async.Main))
        items.add(RouterItem("组件交互", RouterPath.Component.Main))
        items.add(RouterItem("系统服务", RouterPath.SystemService.Main))
        items.add(RouterItem("任务调度", RouterPath.Scheduler.Main))
        items.add(RouterItem("", ""))
        items.add(RouterItem("── 多媒体 ──", ""))
        items.add(RouterItem("多媒体", RouterPath.Media.Main))
        items.add(RouterItem("", ""))
        items.add(RouterItem("── 跨进程通信 ──", ""))
        items.add(RouterItem("跨进程通信", RouterPath.Ipc.Main))
        items.add(RouterItem("", ""))
        items.add(RouterItem("── 机器学习 ──", ""))
        items.add(RouterItem("机器学习（TFLite / LiteRT）", RouterPath.Ml.Main))
        return items
    }

    /**
     * 架构与工程
     * - 架构模式：Arch (MVP, MVVM, MVI, Mavericks)
     * - 依赖注入：DI (Hilt, Koin)
     * - 事件通信：Event（事件总线）
     * - 响应式编程：Reactive（Flow / RxJava 操作符对照）
     * - 性能优化：Performance
     */
    private fun buildEngineeringCategory(): ArrayList<RouterItem> {
        val items = arrayListOf<RouterItem>()
        items.add(RouterItem("── 架构模式 ──", ""))
        items.add(RouterItem("架构模式", RouterPath.Arch.Main))
        items.add(RouterItem("", ""))
        items.add(RouterItem("── 依赖注入 ──", ""))
        items.add(RouterItem("依赖注入", RouterPath.Di.Main))
        items.add(RouterItem("", ""))
        items.add(RouterItem("── 事件通信 ──", ""))
        items.add(RouterItem("事件总线", RouterPath.Event.Main))
        items.add(RouterItem("", ""))
        items.add(RouterItem("── 响应式编程 ──", ""))
        items.add(RouterItem("响应式编程", RouterPath.Reactive.Main))
        items.add(RouterItem("", ""))
        items.add(RouterItem("── 性能优化 ──", ""))
        items.add(RouterItem("性能优化", RouterPath.Performance.Main))
        return items
    }

    /**
     * Kotlin & Jetpack
     * - 语言特性：Kotlin (Coroutines, Flow, Channel, Concurrency, Delegate, Inline, Syntax)
     * - Jetpack 组件：Jetpack (Paging, ViewModel)
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
     * Sample & Feature
     * - 示例：Sample, Feature
     */
    private fun buildSampleFeatureCategory(): ArrayList<RouterItem> {
        val items = arrayListOf<RouterItem>()
        items.add(RouterItem("── 示例 ──", ""))
        items.add(RouterItem("技术示例", RouterPath.Sample.Main))
        items.add(RouterItem("业务功能", RouterPath.Feature.Main))
        return items
    }
}
