package com.example.william.my.module.kotlin

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * Kotlin模块入口页
 *
 * 展示协程、Flow、作用域函数、委托等 Kotlin 特性的示例列表。
 */
@Route(path = RouterPath.Kotlin.Main)
class KotlinMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("Coroutines（协程并发与作用域）", RouterPath.Kotlin.Coroutines))
        routerItems.add(RouterItem("Flow（响应式冷热数据流）", RouterPath.Kotlin.Flow))
        routerItems.add(RouterItem("Channel（通道与回调转 Flow 桥梁）", RouterPath.Kotlin.Channel))
        routerItems.add(RouterItem("Concurrency（并发控制与非阻塞同步）", RouterPath.Kotlin.Concurrency))
        routerItems.add(RouterItem("Delegate（委托属性与类委托）", RouterPath.Kotlin.Delegate))
        routerItems.add(RouterItem("Inline（内联函数与泛型实化）", RouterPath.Kotlin.Inline))
        routerItems.add(RouterItem("Syntax（现代语法、操作符重载与 DSL）", RouterPath.Kotlin.Syntax))
        return routerItems
    }
}
