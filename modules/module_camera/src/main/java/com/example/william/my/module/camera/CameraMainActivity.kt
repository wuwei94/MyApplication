package com.example.william.my.module.camera

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * 相机模块入口 — 导航到拍照、录像两个 CameraX 示例页面。
 */
@Route(path = RouterPath.Camera.Main)
class CameraMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("拍照 (ImageCapture)", RouterPath.Camera.Photo))
        routerItems.add(RouterItem("录像 (VideoCapture)", RouterPath.Camera.Video))
        return routerItems
    }
}
