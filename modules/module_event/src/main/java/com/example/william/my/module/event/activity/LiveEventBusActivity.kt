package com.example.william.my.module.event.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.eventbus.livedata.LiveEventBus
import com.example.william.my.module.event.event.GlobalEvent
import com.example.william.my.module.event.event.StickyEvent

/**
 * LiveData 事件总线 — 基于 LiveData 的事件总线
 *
 * 利用 LiveData 的生命周期感知能力实现的事件总线，自动管理订阅生命周期。
 *
 * 核心特性：
 * 1. 生命周期感知：页面销毁后自动取消订阅，无需手动解绑
 * 2. 粘性事件：支持粘性事件，新订阅者也能收到
 * 3. 跨进程：支持跨进程通信（使用 LiveData 的跨进程版本）
 * 4. 简单易用：基于 LiveData，学习成本低
 *
 * 特性对比：
 * 延迟发送: ✅ | 有序接收: ✅ | Sticky: ✅ | 生命周期感知: ✅ | 可跨进程: ✅ | 线程分发: ❌
 *
 * 基本用法：
 * ```kotlin
 * // 订阅事件
 * LiveEventBus.observeEvent<MessageEvent>(this) { event ->
 *     // 处理事件
 * }
 *
 * // 发送事件
 * LiveEventBus.postEvent(this, MessageEvent("Hello"))
 * ```
 *
 * 适用场景：
 * - 需要生命周期感知的事件通信
 * - 替代 EventBus，更安全可靠
 * - 跨组件、跨页面通信
 */
@Route(path = RouterPath.Event.LiveEventBus)
class LiveEventBusActivity : BasicResponseActivity() {

    private var isObserving = false

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("LiveEventBus 示例\n\n请点击下方按钮注册监听或发送事件")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "开启监听 (Observe)",
            "发送普通事件 (Post Event)",
            "发送粘性事件 (Post Sticky Event)",
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> observeLiveEventBus()
            1 -> {
                appendLog("发送普通事件：GlobalEvent")
                LiveEventBus.postEvent(
                    this,
                    GlobalEvent("LiveEventBus post by Activity")
                )
            }
            2 -> {
                appendLog("发送粘性事件：StickyEvent")
                LiveEventBus.postEvent(
                    this,
                    StickyEvent("LiveEventBus postSticky by Activity")
                )
            }
        }
    }

    private fun observeLiveEventBus() {
        if (isObserving) {
            appendLog("LiveEventBus 已处于监听状态")
            return
        }
        LiveEventBus.observeEvent<GlobalEvent>(this) {
            appendLog("收到普通事件：${it.message}")
        }
        LiveEventBus.observeEvent<StickyEvent>(this, isSticky = true) {
            appendLog("收到粘性事件：${it.message}")
        }
        isObserving = true
        appendLog("已开启 LiveEventBus 监听")
    }
}