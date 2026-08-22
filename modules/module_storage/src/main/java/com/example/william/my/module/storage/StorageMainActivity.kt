package com.example.william.my.module.storage

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * 存储模块入口
 */
@Route(path = RouterPath.Storage.Main)
class StorageMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("Room", RouterPath.Storage.Room))
        routerItems.add(RouterItem("ObjectBox", RouterPath.Storage.ObjectBox))

        routerItems.add(RouterItem("", ""))
        routerItems.add(RouterItem("DataStore", RouterPath.Storage.DataStore))
        routerItems.add(RouterItem("MMKV", RouterPath.Storage.MMKV))
        return routerItems
    }
}
