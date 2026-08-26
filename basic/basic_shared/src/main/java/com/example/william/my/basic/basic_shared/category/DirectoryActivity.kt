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
        return arrayListOf(
            createCategoryItem("UI 交互", "ui"),
            createCategoryItem("网络通信", "network"),
            createCategoryItem("数据存储", "storage"),
            createCategoryItem("系统能力", "system"),
            createCategoryItem("架构与工程", "engineering"),
            createCategoryItem("Kotlin & Jetpack", "kotlin_jetpack"),
            createCategoryItem("Compose & Flutter", "compose_flutter"),
            createCategoryItem("Sample & Feature", "sample_feature")
        )
    }

    private fun createCategoryItem(title: String, category: String): RouterItem {
        return RouterItem(title, RouterPath.Category_Main, hashMapOf("category" to category))
    }
}
