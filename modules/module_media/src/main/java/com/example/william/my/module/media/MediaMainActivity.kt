package com.example.william.my.module.media

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * 多媒体模块入口 — 导航到拍照、录像、图片裁剪三个示例页面。
 */
@Route(path = RouterPath.Media.Main)
class MediaMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("拍照 (ImageCapture)", RouterPath.Media.Photo))
        routerItems.add(RouterItem("录像 (VideoCapture)", RouterPath.Media.Video))
        routerItems.add(RouterItem("图片裁剪", RouterPath.Media.Crop))
        return routerItems
    }
}
