package com.example.william.my.module.component.activity

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.component.IMyAidlInterface
import com.example.william.my.module.component.service.MyAIDLService
import com.example.william.my.module.component.service.MyBoundService
import com.example.william.my.module.component.service.MyForegroundService

/**
 * Service 演示
 *
 * 演示四种 Service 模式：
 * - Started Service：通过 startService 启动，独立运行
 * - Bound Service：通过 bindService 绑定，直接调用 Service 方法
 * - AIDL Service：通过 bindService 绑定，跨进程通信
 * - Foreground Service：通过 startForegroundService 启动，有通知栏常驻
 */
@Route(path = RouterPath.Component.Service)
class ServiceActivity : BasicResponseActivity() {

    private var mBoundConnection: ServiceConnection? = null
    private var mAIDLConnection: ServiceConnection? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("Service 演示\n\n点击下方按钮操作服务")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "启动 Started Service",
            "停止 Started Service",
            "绑定 Bound Service",
            "解绑 Bound Service",
            "绑定 AIDL Service",
            "解绑 AIDL Service",
            "启动前台服务",
            "停止前台服务"
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        when (position) {
            0 -> startStartedService()
            1 -> stopStartedService()
            2 -> bindBoundService()
            3 -> unbindBoundService()
            4 -> bindAIDLService()
            5 -> unbindAIDLService()
            6 -> startForegroundService()
            7 -> stopForegroundService()
        }
    }

    private fun startStartedService() {
        val intent = Intent(this, MyBoundService::class.java).apply {
            putExtra(MyBoundService.EXTRA_MESSAGE, "Started Service 收到消息")
        }
        startService(intent)
        appendLog("启动 Started Service")
    }

    private fun stopStartedService() {
        stopService(Intent(this, MyBoundService::class.java))
        appendLog("停止 Started Service")
    }

    private fun bindBoundService() {
        if (mBoundConnection != null) {
            appendLog("Bound Service 已绑定")
            return
        }

        mBoundConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, binder: IBinder) {
                val service = (binder as MyBoundService.LocalBinder).getService()
                appendLog("Bound Service 绑定成功：${service.getMessage()}")
            }

            override fun onServiceDisconnected(name: ComponentName) {
                appendLog("Bound Service 连接断开")
            }
        }

        val result = bindService(
            Intent(this, MyBoundService::class.java),
            mBoundConnection!!,
            BIND_AUTO_CREATE
        )
        appendLog("正在绑定 Bound Service...（result=$result）")
    }

    private fun unbindBoundService() {
        mBoundConnection?.let { conn ->
            unbindService(conn)
            mBoundConnection = null
            appendLog("Bound Service 已解绑")
        } ?: appendLog("Bound Service 未绑定")
    }

    private fun bindAIDLService() {
        if (mAIDLConnection != null) {
            appendLog("AIDL Service 已绑定")
            return
        }

        mAIDLConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, binder: IBinder) {
                val service = IMyAidlInterface.Stub.asInterface(binder)
                service.showToast("AIDL 服务已绑定")
                appendLog("AIDL Service 绑定成功：${service.getMessage()}")
            }

            override fun onServiceDisconnected(name: ComponentName) {
                appendLog("AIDL Service 连接断开")
            }
        }

        val result = bindService(
            Intent(this, MyAIDLService::class.java),
            mAIDLConnection!!,
            BIND_AUTO_CREATE
        )
        appendLog("正在绑定 AIDL Service...（result=$result）")
    }

    private fun unbindAIDLService() {
        mAIDLConnection?.let { conn ->
            unbindService(conn)
            mAIDLConnection = null
            appendLog("AIDL Service 已解绑")
        } ?: appendLog("AIDL Service 未绑定")
    }

    private fun startForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, MyForegroundService::class.java))
        } else {
            startService(Intent(this, MyForegroundService::class.java))
        }
        appendLog("启动前台服务")
    }

    private fun stopForegroundService() {
        stopService(Intent(this, MyForegroundService::class.java))
        appendLog("停止前台服务")
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindBoundService()
        unbindAIDLService()
    }
}
