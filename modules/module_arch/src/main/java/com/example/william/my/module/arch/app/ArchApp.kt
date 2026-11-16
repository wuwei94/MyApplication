package com.example.william.my.module.arch.app

import com.example.william.my.basic.basic_sync.Sync
import com.example.william.my.core.base.app.BaseAppInit

/**
 * Arch 模块 Application 初始化
 *
 * 与 Hilt 方案 [com.example.william.my.module.arch.hilt.ArchInitImpl] 双轨对应：
 * 调用 [Sync.initialize] 在应用启动时排队增量同步 Worker，供 Offline-First 示例页面使用。
 */
class ArchApp : BaseAppInit() {

    override fun init() {
        super.init()

        Sync.initialize(app)
    }
}
