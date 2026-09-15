package com.example.william.my.module.ipc.activity

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.ipc.IMyAidlInterface
import com.example.william.my.module.ipc.service.MyAIDLService

/**
 * AIDL — 跨进程通信（IPC）
 *
 * AIDL（Android Interface Definition Language）是 Android 提供的跨进程通信方案。
 *
 * 核心机制与避坑点：
 * 1. 跨进程：不同进程间通过 Binder 调用远程方法；
 * 2. 并发处理：支持多线程并发调用（Binder 线程池）；
 * 3. 类型安全：通过 .aidl 接口定义，编译期生成 Stub/Proxy；
 * 4. 双向通信：客户端与服务端可互相调用。
 *
 * 与 Messenger 的区别：
 * - AIDL：支持并发调用，功能强大，适合复杂通信；
 * - Messenger：串行处理，简单易用，适合轻量级通信。
 *
 * https://developer.android.com/guide/components/aidl
 */
@Route(path = RouterPath.Ipc.AIDL)
class AIDLActivity : BasicResponseActivity() {

    private var aidlService: IMyAidlInterface? = null
    private var aidlConnection: ServiceConnection? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("AIDL 跨进程通信\n\n点击下方按钮操作 AIDL Service")
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "绑定 AIDL Service",
        "解绑 AIDL Service",
        "调用 getMessage()",
        "调用 showToast()",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> bindAIDLService()
            1 -> unbindAIDLService()
            2 -> callGetMessage()
            3 -> callShowToast()
        }
    }

    private fun bindAIDLService() {
        if (aidlConnection != null) {
            appendLog("AIDL Service 已绑定")
            return
        }

        aidlConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, binder: IBinder) {
                aidlService = IMyAidlInterface.Stub.asInterface(binder)
                appendLog("AIDL Service 绑定成功：${aidlService?.getMessage()}")
            }

            override fun onServiceDisconnected(name: ComponentName) {
                aidlService = null
                appendLog("AIDL Service 连接断开")
            }
        }

        val result = bindService(
            Intent(this, MyAIDLService::class.java),
            aidlConnection!!,
            BIND_AUTO_CREATE,
        )
        appendLog("正在绑定 AIDL Service...（result=$result）")
    }

    private fun unbindAIDLService() {
        aidlConnection?.let { conn ->
            unbindService(conn)
            aidlConnection = null
            aidlService = null
            appendLog("AIDL Service 已解绑")
        } ?: appendLog("AIDL Service 未绑定")
    }

    private fun callGetMessage() {
        aidlService?.let { appendLog("getMessage() 返回：${it.getMessage()}") }
            ?: appendLog("AIDL Service 未绑定，请先绑定")
    }

    private fun callShowToast() {
        aidlService?.let {
            it.showToast("来自客户端的 AIDL 调用")
            appendLog("已调用 showToast()")
        } ?: appendLog("AIDL Service 未绑定，请先绑定")
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindAIDLService()
    }
}
