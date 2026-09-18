package com.example.william.my.module.arch.app

import com.airbnb.mvrx.Mavericks
import com.example.william.my.basic.basic_sync.Sync
import com.example.william.my.core.base.app.BaseAppInit

/**
 * Arch 模块 Application 初始化
 *
 * 与 Hilt 方案 [com.example.william.my.module.arch.hilt.ArchInitImpl] 双轨对应：
 * 1. 初始化 Mavericks，供 module_arch 中的 Mavericks 示例页面使用；
 * 2. 调用 [Sync.initialize] 在应用启动时排队增量同步 Worker。
 */
class ArchApp : BaseAppInit() {

    override fun init() {
        super.init()

        Mavericks.initialize(app)
        Sync.initialize(app)
    }
}
