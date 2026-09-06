package com.example.william.my.module.flutter

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import io.flutter.embedding.android.FlutterActivity

/**
 * Flutter 入口页
 *
 * 演示 Flutter 的默认引擎、新引擎与缓存引擎三种启动方式。
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
