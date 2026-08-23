package com.example.william.my.module.opensource

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

@Route(path = RouterPath.OpenSource.Main)
class OpenSourceMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("── UI 组件 ──", ""))
        routerItems.add(RouterItem("EasyFloat", RouterPath.OpenSource.EasyFloat))
        routerItems.add(RouterItem("FlycoTabLayout", RouterPath.OpenSource.FlycoTabLayout))
        routerItems.add(RouterItem("PhotoView", RouterPath.OpenSource.PhotoView))

        routerItems.add(RouterItem(" ", ""))
        routerItems.add(RouterItem("── 选择器 ──", ""))
        routerItems.add(RouterItem("CityPicker", RouterPath.OpenSource.CityPicker))
        routerItems.add(RouterItem("PickerView", RouterPath.OpenSource.PickerView))
        routerItems.add(
            RouterItem(
                "PictureSelector",
                RouterPath.OpenSource.PictureSelector
            )
        )

        routerItems.add(RouterItem(" ", ""))
        routerItems.add(RouterItem("── 工具 ──", ""))
        routerItems.add(RouterItem("PermissionX", RouterPath.OpenSource.PermissionX))
        routerItems.add(RouterItem("RxJava", RouterPath.OpenSource.RxJava))

        routerItems.add(RouterItem(" ", ""))
        routerItems.add(RouterItem("── 图片加载 ──", ""))
        routerItems.add(RouterItem("Coil", RouterPath.OpenSource.Coil))
        routerItems.add(RouterItem("Glide", RouterPath.OpenSource.Glide))
        return routerItems
    }
}
