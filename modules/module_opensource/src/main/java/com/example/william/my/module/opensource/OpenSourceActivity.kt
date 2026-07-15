package com.example.william.my.module.opensource

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

@Route(path = RouterPath.OpenSource.Main)
class OpenSourceActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("Banner", RouterPath.OpenSource.Widget.Banner))
        routerItems.add(
            RouterItem(
                "RealtimeBlurView",
                RouterPath.OpenSource.Widget.RealtimeBlurView
            )
        )
        routerItems.add(RouterItem("CountdownView", RouterPath.OpenSource.Widget.CountdownView))
        routerItems.add(RouterItem("EasyFloat", RouterPath.OpenSource.Widget.EasyFloat))
        routerItems.add(RouterItem("FlycoTabLayout", RouterPath.OpenSource.Widget.FlycoTabLayout))
        routerItems.add(RouterItem("PhotoView", RouterPath.OpenSource.Widget.PhotoView))
        routerItems.add(RouterItem("PopWindow", RouterPath.OpenSource.Widget.PopWindow))
        routerItems.add(RouterItem("ShadowLayout", RouterPath.OpenSource.Widget.ShadowLayout))
        routerItems.add(RouterItem("SwipeLayout", RouterPath.OpenSource.Widget.SwipeLayout))

        routerItems.add(RouterItem(" ", ""))
        routerItems.add(RouterItem("Pag", RouterPath.OpenSource.Animation.Pag))
        routerItems.add(RouterItem("Lottie", RouterPath.OpenSource.Animation.Lottie))
        routerItems.add(RouterItem("SVGAPlayer", RouterPath.OpenSource.Animation.SVGAPlayer))

        routerItems.add(RouterItem(" ", ""))
        routerItems.add(RouterItem("CityPicker", RouterPath.OpenSource.Selector.CityPicker))
        routerItems.add(RouterItem("PickerView", RouterPath.OpenSource.Selector.PickerView))
        routerItems.add(
            RouterItem(
                "PictureSelector",
                RouterPath.OpenSource.Selector.PictureSelector
            )
        )

        routerItems.add(RouterItem(" ", ""))
        routerItems.add(RouterItem("LoadSir", RouterPath.OpenSource.Utils.LoadSir))
        routerItems.add(RouterItem("MMKV", RouterPath.OpenSource.Utils.MMKV))
        routerItems.add(RouterItem("PermissionX", RouterPath.OpenSource.Utils.PermissionX))
        routerItems.add(RouterItem("RxJava", RouterPath.OpenSource.Utils.RxJava))

        routerItems.add(RouterItem(" ", ""))
        routerItems.add(RouterItem("Coil", RouterPath.OpenSource.ImageLoader.Coil))
        routerItems.add(RouterItem("Glide", RouterPath.OpenSource.ImageLoader.Glide))

        routerItems.add(RouterItem(" ", ""))
        routerItems.add(RouterItem("ObjectBox", RouterPath.OpenSource.Database.ObjectBox))
        return routerItems
    }
}
