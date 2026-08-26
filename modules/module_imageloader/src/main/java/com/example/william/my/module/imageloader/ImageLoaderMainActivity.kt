package com.example.william.my.module.imageloader

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * 图片加载演示入口
 *
 * 聚合主流图片加载方案（Coil、Glide）以及项目级 `lib_imageloader` 统一封装。
 */
@Route(path = RouterPath.ImageLoader.Main)
class ImageLoaderMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems = ArrayList<RouterItem>()
        routerItems.add(RouterItem("Coil（Kotlin 协程图片库）", RouterPath.ImageLoader.Coil))
        routerItems.add(RouterItem("Glide（成熟稳定 Google 推荐）", RouterPath.ImageLoader.Glide))
        routerItems.add(RouterItem("ImageLoader（项目级统一封装 lib_imageloader）", RouterPath.ImageLoader.ImageLoader))
        return routerItems
    }
}
