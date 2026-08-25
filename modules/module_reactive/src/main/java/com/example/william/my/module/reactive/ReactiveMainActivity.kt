package com.example.william.my.module.reactive

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * 响应式编程专题：Kotlin Flow 与 RxJava 操作符对照演示。
 */
@Route(path = RouterPath.Reactive.Main)
class ReactiveMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("Flow 操作符（创建 / 变换 / 过滤 / 组合 / 错误恢复）", RouterPath.Reactive.FlowOperators))
        routerItems.add(RouterItem("RxJava 操作符（创建 / 变换 / 过滤 / 组合 / 错误恢复）", RouterPath.Reactive.RxJavaOperators))
        return routerItems
    }
}
