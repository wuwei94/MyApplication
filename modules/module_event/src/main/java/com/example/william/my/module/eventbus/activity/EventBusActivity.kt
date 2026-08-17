package com.example.william.my.module.eventbus.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.eventbus.EventBusHelper
import com.example.william.my.module.eventbus.event.GlobalEvent
import com.example.william.my.module.eventbus.event.StickyEvent
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * EventBus 3.x 示例
 *
 * 演示基于 GreenRobot EventBus 的事件注册、注销、普通事件与粘性事件分发。
 *
 * 特性对比：
 * 延迟发送: ❌ | 有序接收: ✅ | Sticky: ✅ | 生命周期感知: ❌ | 可跨进程: ❌ | 线程分发: ✅
 *
 * 注意：必须在 onDestroy 中执行 unregister 保底，避免内存泄漏。
 */
@Route(path = RouterPath.Event.EventBus)
class EventBusActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("EventBus 示例\n\n请点击下方按钮注册监听或发送事件")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "注册/注销 (Register/Unregister)",
            "发送普通事件 (Post Event)",
            "发送粘性事件 (Post Sticky Event)",
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> registerEventBus()
            1 -> {
                appendLog("发送普通事件：GlobalEvent")
                EventBusHelper.postEvent(
                    GlobalEvent("EventBus post by Activity")
                )
            }
            2 -> {
                appendLog("发送粘性事件：StickyEvent")
                EventBusHelper.postStickyEvent(
                    StickyEvent("EventBus postSticky by Activity")
                )
            }
        }
    }

    private fun registerEventBus() {
        if (!EventBusHelper.isRegistered(this@EventBusActivity)) {
            EventBusHelper.register(this@EventBusActivity)
            appendLog("EventBus 已注册")
        } else {
            EventBusHelper.unregister(this@EventBusActivity)
            appendLog("EventBus 已注销")
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onGlobalEvent(event: GlobalEvent) {
        appendLog("收到普通事件：${event.message}")
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onStickyEvent(event: StickyEvent) {
        appendLog("收到粘性事件：${event.message}")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (EventBusHelper.isRegistered(this)) {
            EventBusHelper.unregister(this)
        }
    }
}