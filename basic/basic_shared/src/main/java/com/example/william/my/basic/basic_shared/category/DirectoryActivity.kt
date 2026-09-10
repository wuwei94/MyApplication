package com.example.william.my.basic.basic_shared.category

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * 目录页面 — 按技术领域分组展示所有模块
 *
 * 分类维度：
 * - 一级：技术领域（横向对比），定义集中在 [Category]，本页按枚举顺序生成入口
 * - 二级：技术来源或主题（同一能力的不同实现按来源分组，不同技术点按主题分组）
 *
 * 归类规则：Jetpack 组件有明确主题时归入对应主题模块，无主题架构组件才进 module_jetpack，
 * 详见 docs/conventions.md#分类判据。
 */
@Route(path = RouterPath.Directory_Main)
class DirectoryActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> = ArrayList(
        Category.entries.map { createCategoryItem(it.title, it.id) },
    )

    private fun createCategoryItem(title: String, category: String): RouterItem = RouterItem(title, RouterPath.Category_Main, hashMapOf("category" to category))
}
