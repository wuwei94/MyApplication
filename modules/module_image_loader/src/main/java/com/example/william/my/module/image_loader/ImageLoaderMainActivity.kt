package com.example.william.my.module.image_loader

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * 图片加载模块入口 — 导航到 Coil、Glide 与 lib_image_loader 统一封装等示例页面。
 *
 * 聚合主流图片加载方案（Coil、Glide）以及项目级 `lib_image_loader` 统一封装。
 */
@Route(path = RouterPath.ImageLoader.Main)
class ImageLoaderMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems = ArrayList<RouterItem>()
        routerItems.add(RouterItem("Coil（Kotlin 协程图片库）", RouterPath.ImageLoader.Coil))
        routerItems.add(RouterItem("Glide（生命周期绑定与变换链）", RouterPath.ImageLoader.Glide))
        routerItems.add(RouterItem("LibImageLoader（项目级统一封装 lib_image_loader）", RouterPath.ImageLoader.LibImageLoader))
        return routerItems
    }
}
