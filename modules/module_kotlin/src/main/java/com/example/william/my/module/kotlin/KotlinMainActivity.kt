package com.example.william.my.module.kotlin

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

@Route(path = RouterPath.Kotlin.Main)
class KotlinMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("Coroutines", RouterPath.Kotlin.Coroutines))
        routerItems.add(RouterItem("Flow", RouterPath.Kotlin.Flow))
        routerItems.add(RouterItem("Delegate（委托属性与类委托）", RouterPath.Kotlin.Delegate))
        routerItems.add(RouterItem("Inline（内联函数与泛型实化）", RouterPath.Kotlin.Inline))
        return routerItems
    }
}