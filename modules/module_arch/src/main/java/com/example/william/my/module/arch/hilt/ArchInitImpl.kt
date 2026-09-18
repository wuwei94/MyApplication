package com.example.william.my.module.arch.hilt

import android.app.Application
import com.airbnb.mvrx.Mavericks
import com.example.william.my.basic.basic_sync.Sync
import com.example.william.my.core.base.app.hilt.interfaces.IAppInit
import javax.inject.Inject

/**
 * Arch 模块 Hilt 初始化实现（IAppInit 方案）。
 *
 * 与手动方案 [com.example.william.my.module.arch.app.ArchApp] 一一对应，
 * 二者初始化内容相同，仅机制不同：
 *  - 手动方案：继承 BaseAppInit，由 App 在 initApp() 中 registerAppInit(...) 触发；
 *  - Hilt 方案：实现 IAppInit，由本类经 @ArchInit 绑定后注入 AppHilt 触发。
 * 切换 AndroidManifest 中的 android:name（App / AppHilt）即可二选一。
 *
 * 启动时初始化 Mavericks，并调用 [Sync.initialize] 排队增量同步 Worker。
 */
class ArchInitImpl @Inject constructor() : IAppInit {

    private lateinit var app: Application

    override fun init(app: Application) {
        this.app = app

        initMavericks(app)
        Sync.initialize(app)
    }

    override fun getApp(): Application = app

    private fun initMavericks(app: Application) {
        Mavericks.initialize(app)
    }
}
