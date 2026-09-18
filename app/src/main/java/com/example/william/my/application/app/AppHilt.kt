package com.example.william.my.application.app

import android.app.Application
import androidx.work.Configuration
import com.example.william.my.basic.basic_sync.work.SyncWorkerFactory
import com.example.william.my.core.base.app.hilt.interfaces.IAppInit
import com.example.william.my.core.base.app.hilt.qualifier.AppInit
import com.example.william.my.core.base.app.hilt.qualifier.ArchInit
import com.example.william.my.core.base.app.hilt.qualifier.BaseInit
import com.example.william.my.core.base.app.hilt.qualifier.EventInit
import com.example.william.my.core.base.app.hilt.qualifier.FlutterInit
import com.example.william.my.core.base.app.hilt.qualifier.WidgetThirdpartyInit
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Hilt 应用入口 Application。
 *
 * 通过 Hilt 注入各模块的 IAppInit 实现，并在 onCreate 中依次执行初始化。
 * 实现 [Configuration.Provider]，为 WorkManager 提供 [SyncWorkerFactory]，
 * 同时覆盖 `@HiltWorker` 与 ServiceLocator 两条 Worker 装配路径。
 */
@HiltAndroidApp
class AppHilt :
    Application(),
    Configuration.Provider {

    @BaseInit
    @Inject
    lateinit var baseInit: IAppInit

    @AppInit
    @Inject
    lateinit var appInit: IAppInit

    @EventInit
    @Inject
    lateinit var eventInit: IAppInit

    @ArchInit
    @Inject
    lateinit var archInit: IAppInit

    @WidgetThirdpartyInit
    @Inject
    lateinit var widgetThirdpartyInit: IAppInit

    @FlutterInit
    @Inject
    lateinit var flutterInit: IAppInit

    @Inject
    lateinit var syncWorkerFactory: SyncWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(syncWorkerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        baseInit.init(this)

        appInit.init(this)

        eventInit.init(this)
        archInit.init(this)
        widgetThirdpartyInit.init(this)

        flutterInit.init(this) // FlutterEngine
    }
}
