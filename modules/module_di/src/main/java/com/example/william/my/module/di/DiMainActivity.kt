package com.example.william.my.module.di

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * 依赖注入 (DI) 演示入口
 *
 * 集中展示 Android 主流依赖注入方案（Hilt 与 Koin）的核心特性与最佳实践。
 */
@Route(path = RouterPath.Di.Main)
class DiMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems = ArrayList<RouterItem>()
        routerItems.add(RouterItem("Hilt（编译期依赖注入）", RouterPath.Di.Hilt))
        routerItems.add(RouterItem("Koin（Kotlin DSL 运行时依赖注入）", RouterPath.Di.Koin))
        return routerItems
    }
}
