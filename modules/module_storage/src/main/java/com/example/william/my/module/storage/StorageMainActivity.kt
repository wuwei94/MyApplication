package com.example.william.my.module.storage

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * 存储模块入口 — 导航到 DataStore、MMKV 等键值存储示例页面。
 */
@Route(path = RouterPath.Storage.Main)
class StorageMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("DataStore（Jetpack 响应式键值存储）", RouterPath.Storage.DataStore))
        routerItems.add(RouterItem("MMKV（Tencent 高性能键值存储）", RouterPath.Storage.MMKV))
        return routerItems
    }
}
