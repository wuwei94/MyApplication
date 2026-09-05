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
 * EventBus 3.x — 事件总线框架
 *
 * EventBus 是一个 Android 事件总线库，简化组件间通信，替代 Intent、Handler 等传统方式。
 *
 * 核心特性：
 * 1. 简化通信：组件间解耦，无需直接引用
 * 2. 线程切换：支持主线程、后台线程、新线程等多种线程模式
 * 3. 粘性事件：支持发送粘性事件，新订阅者也能收到
 * 4. 高性能：基于注解处理，性能优秀
 *
 * 特性对比：
 * 延迟发送: ❌ | 有序接收: ✅ | Sticky: ✅ | 生命周期感知: ❌ | 可跨进程: ❌ | 线程分发: ✅
 *
 * 基本用法：
 * ```kotlin
 * // 注册
 * EventBus.getDefault().register(this)
 *
 * // 发送事件
 * EventBus.getDefault().post(MessageEvent("Hello"))
 *
 * // 订阅事件
 * @Subscribe(threadMode = ThreadMode.MAIN)
 * fun onMessageEvent(event: MessageEvent) {
 *     // 处理事件
 * }
 *
 * // 注销
 * EventBus.getDefault().unregister(this)
 * ```
 *
 * 注意：必须在 onDestroy 中执行 unregister 保底，避免内存泄漏。
 *
 * https://github.com/greenrobot/EventBus
 */
@Route(path = RouterPath.Event.EventBus)
class EventBusActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("EventBus 示例\n\n请点击下方按钮注册监听或发送事件")
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "注册/注销 (Register/Unregister)",
        "发送普通事件 (Post Event)",
        "发送粘性事件 (Post Sticky Event)",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> registerEventBus()
            1 -> {
                appendLog("发送普通事件：GlobalEvent")
                EventBusHelper.postEvent(
                    GlobalEvent("EventBus post by Activity"),
                )
            }
            2 -> {
                appendLog("发送粘性事件：StickyEvent")
                EventBusHelper.postStickyEvent(
                    StickyEvent("EventBus postSticky by Activity"),
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
