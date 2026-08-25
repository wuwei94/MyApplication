package com.example.william.my.basic.basic_shared

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
 */
@Route(path = RouterPath.Directory_Main)
class DirectoryActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()

        // UI交互
        routerItems.add(RouterItem("UI交互", getCategoryPath("ui"), getCategoryParams("ui")))

        // 网络通信
        routerItems.add(RouterItem("网络通信", getCategoryPath("network"), getCategoryParams("network")))

        // 数据存储
        routerItems.add(RouterItem("数据存储", getCategoryPath("storage"), getCategoryParams("storage")))

        // 系统能力
        routerItems.add(RouterItem("系统能力", getCategoryPath("system"), getCategoryParams("system")))

        // 架构模式
        routerItems.add(RouterItem("架构模式", getCategoryPath("arch"), getCategoryParams("arch")))

        // Kotlin & Jetpack
        routerItems.add(RouterItem("Kotlin & Jetpack", getCategoryPath("kotlin_jetpack"), getCategoryParams("kotlin_jetpack")))

        // Compose & Flutter
        routerItems.add(RouterItem("Compose & Flutter", getCategoryPath("compose_flutter"), getCategoryParams("compose_flutter")))

        // AndroidUtils & 三方库
        routerItems.add(RouterItem("AndroidUtils & 三方库", getCategoryPath("android_utils"), getCategoryParams("android_utils")))

        // 其他
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
