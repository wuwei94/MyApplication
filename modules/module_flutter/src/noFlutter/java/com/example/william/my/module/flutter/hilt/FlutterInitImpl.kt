package com.example.william.my.module.flutter.hilt

import android.app.Application
import com.example.william.my.core.base.app.hilt.interfaces.IAppInit
import javax.inject.Inject

/**
 * Flutter 初始化实现（无 Flutter 变体）
 *
 * 未启用 Flutter 时仅持有 Application。
 */
class FlutterInitImpl @Inject constructor() : IAppInit {

    private lateinit var app: Application

    override fun init(app: Application) {
        this.app = app
    }

    override fun getApp(): Application = app
}
