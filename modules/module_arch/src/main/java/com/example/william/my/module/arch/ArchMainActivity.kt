package com.example.william.my.module.arch

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * 架构模块入口页
 *
 * 展示 MVP/MVVM/MVI/Mavericks 等架构模式的示例列表。
 */
@Route(path = RouterPath.Arch.Main)
class ArchMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("MVP（Contract + Presenter 契约架构）", RouterPath.Arch.MVP))
        routerItems.add(RouterItem("MVVM（LiveData + UseCase 数据驱动）", RouterPath.Arch.MVVM))
        routerItems.add(RouterItem("MVI（StateFlow + Intent + Effect 单向数据流）", RouterPath.Arch.MVI))
        routerItems.add(RouterItem("Compose MVI（Compose + StateFlow 单向数据流）", RouterPath.Arch.ComposeMVI))
        routerItems.add(RouterItem("Mavericks（Airbnb 现代化响应式 MVI 架构）", RouterPath.Arch.Mavericks))
        return routerItems
    }
}
