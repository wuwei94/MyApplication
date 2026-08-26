package com.example.william.my.module.jetpack

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

@Route(path = RouterPath.Jetpack.Main)
class JetpackMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("Lifecycle（生命周期感知与安全数据流收集）", RouterPath.Jetpack.Lifecycle))
        routerItems.add(RouterItem("Paging3（大数据集分页加载）", RouterPath.Jetpack.Paging))
        routerItems.add(RouterItem("ViewModel（多种创建模式与伴生 Factory）", RouterPath.Jetpack.ViewModel))
        return routerItems
    }
}
