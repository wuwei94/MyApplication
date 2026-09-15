package com.example.william.my.module.security

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * 安全模块入口 — 导航到系统密钥与签名等示例页面。
 *
 * 展示系统安全边界内的密钥管理与签名等安全能力的示例列表。
 */
@Route(path = RouterPath.Security.Main)
class SecurityMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems: ArrayList<RouterItem> = arrayListOf()
        routerItems.add(RouterItem("SecureKey（Android Keystore 硬件级安全密钥）", RouterPath.Security.SecureKey))
        return routerItems
    }
}
