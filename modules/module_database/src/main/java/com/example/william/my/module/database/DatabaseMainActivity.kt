package com.example.william.my.module.database

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * 数据库模块入口
 */
@Route(path = RouterPath.Database.Main)
class DatabaseMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("Room", RouterPath.Database.Room))
        routerItems.add(RouterItem("ObjectBox", RouterPath.Database.ObjectBox))
        return routerItems
    }
}
