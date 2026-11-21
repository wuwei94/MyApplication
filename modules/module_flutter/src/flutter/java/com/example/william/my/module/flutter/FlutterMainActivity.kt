package com.example.william.my.module.flutter

import android.os.Bundle
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
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
class FlutterMainActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        startWithCachedEngine()

        finish()
    }

    /**
     * 使用默认 FlutterEngine 启动 FlutterActivity
     */
    private fun startDefault() {
        startActivity(
            FlutterActivity
                .createDefaultIntent(this),
        )
    }

    /**
     * 创建全新 FlutterEngine 实例并指定初始路由启动 FlutterActivity
     */
    private fun startWithNewEngine() {
        startActivity(
            FlutterActivity
                .withNewEngine()
                .initialRoute("/")
                .build(this),
        )
    }

    /**
     * 复用已预热的缓存 FlutterEngine 启动 FlutterActivity
     */
    private fun startWithCachedEngine() {
        startActivity(
            FlutterActivity
                .withCachedEngine("cached_engine_id")
                .build(this),
        )
    }
}
