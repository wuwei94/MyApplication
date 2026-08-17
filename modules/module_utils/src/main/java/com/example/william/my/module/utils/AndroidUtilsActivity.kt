package com.example.william.my.module.utils

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * BlankJ AndroidUtilCode 工具类导航页面
 *
 * https://github.com/Blankj/AndroidUtilCode
 */
@Route(path = RouterPath.Utils.Main)
class AndroidUtilsActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("AdaptScreenUtilsActivity", RouterPath.Utils.AdaptScreenUtils))
        routerItems.add(RouterItem("FileIOUtilsActivity", RouterPath.Utils.FileIOUtils))
        routerItems.add(RouterItem("PermissionUtilsActivity", RouterPath.Utils.PermissionUtils))
        routerItems.add(RouterItem("ThreadUtilsActivity", RouterPath.Utils.ThreadUtils))
        return routerItems
    }
}