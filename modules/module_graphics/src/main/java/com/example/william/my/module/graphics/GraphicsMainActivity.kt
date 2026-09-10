package com.example.william.my.module.graphics

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * 图形渲染模块入口页
 *
 * 展示 Android 底层图形图像处理、着色器与渲染特效的示例列表，点击列表项跳转到对应的示例页。
 */
@Route(path = RouterPath.Graphics.Main)
class GraphicsMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("── 渲染特效 ──", ""))
        routerItems.add(RouterItem("RenderEffect（Android 12+ 渲染特效）", RouterPath.Graphics.RenderEffect))
        routerItems.add(RouterItem("RenderScript（底层模糊与图形计算）", RouterPath.Graphics.RenderScript))
        return routerItems
    }
}
