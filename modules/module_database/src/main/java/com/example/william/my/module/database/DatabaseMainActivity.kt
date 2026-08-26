package com.example.william.my.module.database

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * 数据库模块入口 — 导航到 Room、ObjectBox 等数据库示例页面。
 */
@Route(path = RouterPath.Database.Main)
class DatabaseMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("Room（关系型数据库）", RouterPath.Database.Room))
        routerItems.add(RouterItem("ObjectBox（NoSQL 对象数据库）", RouterPath.Database.ObjectBox))
        return routerItems
    }
}
