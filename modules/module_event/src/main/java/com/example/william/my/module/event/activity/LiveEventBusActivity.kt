package com.example.william.my.module.event.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.eventbus.livedata.LiveEventBus
import com.example.william.my.module.event.event.GlobalEvent
import com.example.william.my.module.event.event.StickyEvent
import kotlinx.coroutines.Job

/**
 * LiveEventBus — 基于 LiveData 生命周期感知的事件总线
 *
 * 核心机制与避坑点：
 * 1. 生命周期感知：observe 绑定 LifecycleOwner，页面 DESTROYED 后自动取消订阅
 * 2. 手动注销：observeEvent 返回 Job，cancel Job 可立即停止回调，与自动注销形成对照
 * 3. 粘性与延迟：粘性通道保留最近值供新订阅者接收；非粘性通道仅投递给已激活的观察者
 * 4. 线程约束：post 可在任意线程发起，回调始终分发到主线程；不适合做工作线程间通信
 *
 * 官方参考：
 * https://github.com/JeremyLiao/LiveEventBus
 */
@Route(path = RouterPath.Event.LiveEventBus)
class LiveEventBusActivity : BasicResponseActivity() {

    private var globalJob: Job? = null
    private var stickyJob: Job? = null
    private var isObserving = false

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription(
            "LiveEventBus 示例：开启监听 / 手动注销 / 普通事件 / 粘性事件 / 生命周期感知",
        )
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 开启监听 (Observe)",
        "2. 手动注销监听 (Cancel Job)",
        "3. 发送普通事件 (Post Event)",
        "4. 发送粘性事件 (Post Sticky Event)",
        "5. 生命周期绑定说明 (Lifecycle)",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> observeLiveEventBus()
            1 -> unobserveLiveEventBus()
            2 -> postGlobalEvent()
            3 -> postSticky()
            4 -> showLifecyclePolicy()
        }
    }

    private fun observeLiveEventBus() {
        if (isObserving) {
            appendLog("LiveEventBus 已处于监听状态")
            return
        }
        globalJob = LiveEventBus.observeEvent<GlobalEvent>(this) {
            appendLog("收到普通事件：${it.message}")
        }
        stickyJob = LiveEventBus.observeEvent<StickyEvent>(this, isSticky = true) {
            appendLog("收到粘性事件：${it.message}")
        }
        isObserving = true
        appendLog("已开启 LiveEventBus 监听")
    }

    private fun unobserveLiveEventBus() {
        if (!isObserving) {
            appendLog("LiveEventBus 当前未监听")
            return
        }
        globalJob?.cancel()
        stickyJob?.cancel()
        globalJob = null
        stickyJob = null
        isObserving = false
        appendLog("已取消 LiveEventBus 监听")
    }

    private fun postGlobalEvent() {
        appendLog("发送普通事件：GlobalEvent")
        LiveEventBus.postEvent(
            this,
            GlobalEvent("LiveEventBus post by Activity"),
        )
    }

    private fun postSticky() {
        appendLog("发送粘性事件：StickyEvent")
        LiveEventBus.postEvent(
            this,
            StickyEvent("LiveEventBus postSticky by Activity"),
        )
    }

    private fun showLifecyclePolicy() {
        appendLog("LiveEventBus.observe 绑定 LifecycleOwner，DESTROYED 自动取消订阅")
        appendLog("手动 cancel Job 可提前注销；当前监听状态：$isObserving")
    }

    override fun onDestroy() {
        super.onDestroy()
        globalJob?.cancel()
        stickyJob?.cancel()
        isObserving = false
    }
}
