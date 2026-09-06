package com.example.william.my.module.feature

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * 特性模块入口页
 *
 * 展示麦克风动画、转盘等特色功能的示例列表。
 */
@Route(path = RouterPath.Feature.Main)
class FeatureMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("Turntable（幸运大转盘实战）", RouterPath.Feature.Turntable))
        routerItems.add(RouterItem("MicAnimation（麦位光波音量动画）", RouterPath.Feature.MicAnimation))
        return routerItems
    }
}
