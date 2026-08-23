package com.example.william.my.module.mavericks.app

import com.airbnb.mvrx.Mavericks
import com.example.william.my.core.base.app.BaseAppInit

/**
 * Mavericks 框架初始化
 *
 * 在 Application 启动时初始化 Mavericks，供 module_mavericks 中的示例页面使用。
 */
class MavericksApp : BaseAppInit() {

    override fun init() {
        super.init()

        Mavericks.initialize(app)
    }
}
