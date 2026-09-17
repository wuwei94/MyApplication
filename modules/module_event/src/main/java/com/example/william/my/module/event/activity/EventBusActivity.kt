package com.example.william.my.module.event.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.eventbus.EventBusHelper
import com.example.william.my.module.event.event.GlobalEvent
import com.example.william.my.module.event.event.StickyEvent
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * EventBus 3.x — 基于注解订阅的进程内事件总线
 *
 * 核心机制与避坑点：
 * 1. 注册模型：必须先 EventBus.register(this) 再 post，@Subscribe 标注的订阅方法才能收到；unregister 后回调立即停止
 * 2. 线程分发：@Subscribe(threadMode = ThreadMode.MAIN/BACKGROUND/...) 控制回调线程，post 线程与订阅线程解耦
 * 3. 粘性事件：postSticky 保留最近一条，后注册的 sticky 订阅者仍可收到；不再需要时应及时 removeStickyEvent 清理
 * 4. 生命周期约束：不具备生命周期感知，onDestroy 必须 unregister 保底，避免订阅者泄漏
 *
 * 官方参考：
 * https://github.com/greenrobot/EventBus
 */
@Route(path = RouterPath.Event.EventBus)
class EventBusActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription(
            "EventBus 示例：开启监听 / 手动注销 / 普通事件 / 粘性事件 / 生命周期约束",
        )
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 开启监听 (Register)",
        "2. 手动注销监听 (Unregister)",
        "3. 发送普通事件 (Post Event)",
        "4. 发送粘性事件 (Post Sticky Event)",
        "5. 生命周期绑定说明 (Lifecycle)",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> registerEventBus()
            1 -> unregisterEventBus()
            2 -> postGlobalEvent()
            3 -> postSticky()
            4 -> showLifecyclePolicy()
        }
    }

    private fun registerEventBus() {
        if (EventBusHelper.isRegistered(this)) {
            appendLog("EventBus 已处于注册状态")
            return
        }
        EventBusHelper.register(this)
        appendLog("EventBus 已注册")
    }

    private fun unregisterEventBus() {
        if (!EventBusHelper.isRegistered(this)) {
            appendLog("EventBus 当前未注册")
            return
        }
        EventBusHelper.unregister(this)
        appendLog("EventBus 已注销")
    }

    private fun postGlobalEvent() {
        appendLog("发送普通事件：GlobalEvent")
        EventBusHelper.postEvent(
            GlobalEvent("EventBus post by Activity"),
        )
    }

    private fun postSticky() {
        appendLog("发送粘性事件：StickyEvent")
        EventBusHelper.postStickyEvent(
            StickyEvent("EventBus postSticky by Activity"),
        )
    }

    /**
     * EventBus 无 LifecycleOwner 绑定，销毁时必须手动 unregister。
     */
    private fun showLifecyclePolicy() {
        appendLog("EventBus 不绑定 LifecycleOwner，onDestroy 执行 unregister 保底")
        appendLog("当前注册状态：${EventBusHelper.isRegistered(this)}")
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
