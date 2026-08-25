package com.example.william.my.module.ipc.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.william.my.basic.basic_shared.utils.Utils
import com.example.william.my.module.ipc.IMyAidlInterface

/**
 * AIDL Service（跨进程通信）
 *
 * 通过 AIDL 接口定义跨进程通信方法，客户端通过 Stub.asInterface() 获取接口实例。
 * 与 LocalBinder 的区别：LocalBinder 只能同进程使用，AIDL 支持跨进程。
 */
class MyAIDLService : Service() {

    private val binder = object : IMyAidlInterface.Stub() {
        override fun showToast(message: String?) {
            Utils.toast(message)
        }

        override fun getMessage(): String {
            return "来自 AIDL Service 的消息"
        }
    }

    override fun onBind(intent: Intent): IBinder = binder
}
