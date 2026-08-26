package com.example.william.my.module.arch.hilt

import android.app.Application
import com.airbnb.mvrx.Mavericks
import com.example.william.my.core.base.hilt.interfaces.IAppInit
import javax.inject.Inject

/**
 * Mavericks 模块 Hilt 初始化实现（IAppInit 方案）。
 *
 * 与手动方案 [com.example.william.my.module.arch.app.MavericksApp] 一一对应，
 * 二者都调用 [Mavericks.initialize]，仅初始化机制不同：
 *  - 手动方案：继承 BaseAppInit，由 App 在 initApp() 中 registerAppInit(...) 触发；
 *  - Hilt 方案：实现 IAppInit，由本类经 @MavericksInit 绑定后注入 AppHilt 触发。
 * 切换 AndroidManifest 中的 android:name（App / AppHilt）即可二选一。
 */
class MavericksInitImpl @Inject constructor() : IAppInit {

    private lateinit var mApp: Application

    override fun init(app: Application) {
        this.mApp = app

        initMavericks(app)
    }

    override fun getApp(): Application {
        return mApp
    }

    private fun initMavericks(app: Application) {
        Mavericks.initialize(app)
    }
}
