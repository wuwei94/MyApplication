package com.example.william.my.module.router.arouter.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * ARouter 目标接收页 — 演示 `@Autowired` 自动参数注入
 *
 * 核心机制与避坑点：
 * 1. 参数注入前提：需在 `onCreate` / `initView` 中调用 `ARouter.getInstance().inject(this)`
 * 2. 字段可见性：Kotlin 属性须添加 `@JvmField`，否则 KAPT 生成的注入代码无法直接赋值
 */
@Route(path = RouterPath.Router.ARouterTarget)
class ARouterTargetActivity : BasicResponseActivity() {

    @JvmField
    @Autowired(name = "from")
    var from: String? = null

    @JvmField
    @Autowired(name = "code")
    var code: Int = 0

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        ARouter.getInstance().inject(this)
        showDescription("ARouter 目标接收页\n通过 @Autowired 自动注入 Intent 参数。")
        appendLog("✓ 收到 @Autowired 参数: from=$from, code=$code")
    }

    override fun buildList(): ArrayList<String> = arrayListOf("1. 关闭并返回")

    override fun onRecyclerClick(position: Int, string: String) {
        if (position == 0) {
            finish()
        }
    }
}
