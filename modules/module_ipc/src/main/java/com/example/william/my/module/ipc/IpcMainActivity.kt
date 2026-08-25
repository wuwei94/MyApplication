package com.example.william.my.module.ipc

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * 跨进程通信模块入口 — 导航到 AIDL、Messenger 两个 IPC 示例页面。
 *
 * 本模块聚焦「跨进程通信（IPC）」主题：AIDL 与 Messenger 两种基于 Binder 的 IPC 方案，
 * 与 module_component（四大组件交互）区分开。
 */
@Route(path = RouterPath.Ipc.Main)
class IpcMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        return arrayListOf(
            RouterItem("AIDL", RouterPath.Ipc.AIDL),
            RouterItem("Messenger", RouterPath.Ipc.Messenger)
        )
    }
}
