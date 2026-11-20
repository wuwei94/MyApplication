package com.example.william.my.module.flutter

import android.os.Bundle
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity

/**
 * Flutter 模块入口 — 未启用 Flutter 时的占位页，提示先执行 flutter pub get。
 *
 * 未启用 Flutter 时展示占位提示。
 */
class FlutterMainActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("Flutter is disabled. \nSet enableFlutter=true to restore the Flutter page.")
    }
}
