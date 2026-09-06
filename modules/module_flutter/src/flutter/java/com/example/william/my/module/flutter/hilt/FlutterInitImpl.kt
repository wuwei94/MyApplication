package com.example.william.my.module.flutter.hilt

import android.app.Application
import com.example.william.my.core.base.app.hilt.interfaces.IAppInit
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import javax.inject.Inject

/**
 * Flutter 初始化实现
 *
 * 实现 IAppInit，预热并缓存 FlutterEngine。
 */
class FlutterInitImpl @Inject constructor() : IAppInit {

    private lateinit var mApp: Application

    override fun init(app: Application) {
        this.mApp = app

        initFlutterEngine(app)
    }

    override fun getApp(): Application = mApp

    private fun initFlutterEngine(app: Application) {
        // 实例化FlutterEngine。
        val flutterEngine = FlutterEngine(app)

        // 配置初始路由
        flutterEngine.navigationChannel.setInitialRoute("/")

        // 开始执行Dart代码来预热FlutterEngine
        flutterEngine.dartExecutor.executeDartEntrypoint(
            DartExecutor.DartEntrypoint.createDefault(),
        )

        // 缓存FlutterActivity要使用的FlutterEngine。
        FlutterEngineCache
            .getInstance()
            .put("cached_engine_id", flutterEngine)
    }
}
