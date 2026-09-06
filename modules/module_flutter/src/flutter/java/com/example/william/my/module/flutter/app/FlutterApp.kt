package com.example.william.my.module.flutter.app

import com.example.william.my.core.base.app.BaseAppInit
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor

/**
 * Flutter 初始化
 *
 * 预热并缓存 FlutterEngine，供 FlutterActivity 复用。
 */
class FlutterApp : BaseAppInit() {

    override fun init() {
        super.init()

        initFlutterEngine()
    }

    private fun initFlutterEngine() {
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
