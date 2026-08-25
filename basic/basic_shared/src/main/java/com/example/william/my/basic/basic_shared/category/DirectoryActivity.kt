package com.example.william.my.basic.basic_shared.category

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * 目录页面 — 按技术领域分组展示所有模块
 *
 * 分类维度：
 * - 一级：技术领域（横向对比）
 * - 二级：技术来源（系统原生 / Jetpack / 第三方 / 语言特性）
 *
 * 归类规则：Jetpack 组件有明确主题时归入对应主题模块，无主题架构组件才进 module_jetpack，
 * 详见 docs/conventions.md#分类判据。
 */
@Route(path = RouterPath.Directory_Main)
class DirectoryActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()

        // UI 交互
        routerItems.add(RouterItem("UI 交互", getCategoryPath("ui"), getCategoryParams("ui")))

        // 网络通信
        routerItems.add(RouterItem("网络通信", getCategoryPath("network"), getCategoryParams("network")))

        // 数据存储
        routerItems.add(RouterItem("数据存储", getCategoryPath("storage"), getCategoryParams("storage")))

        // 系统能力
        routerItems.add(RouterItem("系统能力", getCategoryPath("system"), getCategoryParams("system")))

        // 多媒体
        routerItems.add(RouterItem("多媒体", getCategoryPath("media"), getCategoryParams("media")))

        // 架构模式
        routerItems.add(RouterItem("架构模式", getCategoryPath("arch"), getCategoryParams("arch")))

        // 工程实践
        routerItems.add(RouterItem("工程实践", getCategoryPath("engineering"), getCategoryParams("engineering")))

        // Kotlin & Jetpack
        routerItems.add(RouterItem("Kotlin & Jetpack", getCategoryPath("kotlin_jetpack"), getCategoryParams("kotlin_jetpack")))

        // Compose & Flutter
        routerItems.add(RouterItem("Compose & Flutter", getCategoryPath("compose_flutter"), getCategoryParams("compose_flutter")))

        // Sample & Feature
        routerItems.add(RouterItem("Sample & Feature", getCategoryPath("sample_feature"), getCategoryParams("sample_feature")))

        return routerItems
    }

    private fun getCategoryPath(category: String): String {
        return RouterPath.Category_Main
    }

    private fun getCategoryParams(category: String): HashMap<String, String> {
        return hashMapOf("category" to category)
    }
}
