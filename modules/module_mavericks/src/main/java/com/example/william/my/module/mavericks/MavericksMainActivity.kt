package com.example.william.my.module.mavericks

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * Mavericks 模块入口
 *
 * 聚合 Mavericks 框架下的示例页面（Counter、Mavericks 文章列表）。
 */
@Route(path = RouterPath.Mavericks.Main)
class MavericksMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("Counter", RouterPath.Mavericks.Counter))
        routerItems.add(RouterItem("Mavericks", RouterPath.Mavericks.Mavericks))
        return routerItems
    }
}
