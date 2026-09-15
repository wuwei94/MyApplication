package com.example.william.my.module.flutter

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import io.flutter.embedding.android.FlutterActivity

/**
 * Flutter — 跨平台 UI 引擎入口
 *
 * 演示 Flutter 的默认引擎、新引擎与缓存引擎三种启动方式。
 * 本页作为跳板，立即启动 FlutterActivity 并 finish 自身。
 *
 * 核心机制与避坑点：
 * 1. 默认引擎：createDefaultIntent() 使用系统默认 FlutterEngine
 * 2. 新引擎：withNewEngine() 每次启动创建全新引擎实例
 * 3. 缓存引擎：withCachedEngine() 复用预热引擎，启动更快
 *
 * https://docs.flutter.dev/add-to-app/android/add-flutter-screen
 */
@Route(path = RouterPath.Flutter.Main)
class FlutterMainActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        startWithCachedEngine()

        finish()
    }

    private fun startDefault() {
        startActivity(
            FlutterActivity
                .createDefaultIntent(this),
        )
    }

    private fun startWithNewEngine() {
        startActivity(
            FlutterActivity
                .withNewEngine()
                .initialRoute("/")
                .build(this),
        )
    }

    private fun startWithCachedEngine() {
        startActivity(
            FlutterActivity
                .withCachedEngine("cached_engine_id")
                .build(this),
        )
    }
}
