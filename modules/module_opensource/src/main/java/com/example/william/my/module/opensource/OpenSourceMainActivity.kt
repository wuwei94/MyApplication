package com.example.william.my.module.opensource

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

@Route(path = RouterPath.OpenSource.Main)
class OpenSourceMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("Banner", RouterPath.OpenSource.Banner))
        routerItems.add(
            RouterItem(
                "RealtimeBlurView",
                RouterPath.OpenSource.RealtimeBlurView
            )
        )
        routerItems.add(RouterItem("CountdownView", RouterPath.OpenSource.CountdownView))
        routerItems.add(RouterItem("EasyFloat", RouterPath.OpenSource.EasyFloat))
        routerItems.add(RouterItem("FlycoTabLayout", RouterPath.OpenSource.FlycoTabLayout))
        routerItems.add(RouterItem("PhotoView", RouterPath.OpenSource.PhotoView))
        routerItems.add(RouterItem("PopWindow", RouterPath.OpenSource.PopWindow))
        routerItems.add(RouterItem("ShadowLayout", RouterPath.OpenSource.ShadowLayout))
        routerItems.add(RouterItem("SwipeLayout", RouterPath.OpenSource.SwipeLayout))

        routerItems.add(RouterItem(" ", ""))
        routerItems.add(RouterItem("Pag", RouterPath.OpenSource.Pag))
        routerItems.add(RouterItem("Lottie", RouterPath.OpenSource.Lottie))
        routerItems.add(RouterItem("SVGAPlayer", RouterPath.OpenSource.SVGAPlayer))

        routerItems.add(RouterItem(" ", ""))
        routerItems.add(RouterItem("CityPicker", RouterPath.OpenSource.CityPicker))
        routerItems.add(RouterItem("PickerView", RouterPath.OpenSource.PickerView))
        routerItems.add(
            RouterItem(
                "PictureSelector",
                RouterPath.OpenSource.PictureSelector
            )
        )

        routerItems.add(RouterItem(" ", ""))
        routerItems.add(RouterItem("LoadSir", RouterPath.OpenSource.LoadSir))
        routerItems.add(RouterItem("MMKV", RouterPath.OpenSource.MMKV))
        routerItems.add(RouterItem("PermissionX", RouterPath.OpenSource.PermissionX))
        routerItems.add(RouterItem("RxJava", RouterPath.OpenSource.RxJava))

        routerItems.add(RouterItem(" ", ""))
        routerItems.add(RouterItem("Coil", RouterPath.OpenSource.Coil))
        routerItems.add(RouterItem("Glide", RouterPath.OpenSource.Glide))

        routerItems.add(RouterItem(" ", ""))
        routerItems.add(RouterItem("ObjectBox", RouterPath.OpenSource.ObjectBox))
        return routerItems
    }
}
