package com.example.william.my.module.arch.app

import com.airbnb.mvrx.Mavericks
import com.example.william.my.core.base.app.BaseAppInit

/**
 * Arch 模块 Application 初始化
 *
 * 在 Application 启动时初始化 Mavericks，供 module_arch 中的 Mavericks 示例页面使用。
 */
class ArchApp : BaseAppInit() {

    override fun init() {
        super.init()

        Mavericks.initialize(app)
    }
}
