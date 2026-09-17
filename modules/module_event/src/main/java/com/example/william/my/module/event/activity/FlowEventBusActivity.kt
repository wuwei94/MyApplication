package com.example.william.my.module.event.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.eventbus.flow.FlowEventBus
import com.example.william.my.module.event.event.GlobalEvent
import com.example.william.my.module.event.event.StickyEvent
import kotlinx.coroutines.Job

/**
 * FlowEventBus — 基于 Kotlin SharedFlow 的协程事件总线
 *
 * 核心机制与避坑点：
 * 1. SharedFlow 通道：普通事件 replay=0 仅投递活跃收集者；粘性通道通过 replay 保留最近一条供新订阅者读取
 * 2. 生命周期绑定：observe 在 lifecycleScope + repeatOnLifecycle 下收集，页面 STOP 后暂停、DESTROY 自动取消 Job
 * 3. 手动注销：observeEvent 返回 Job，cancel Job 立即停止收集，与生命周期自动取消对照
 * 4. 线程模型：post 任意线程均可；回调在收集协程的 Dispatcher 上执行，需要时自行 withContext(Dispatchers.Main)
 *
 * 官方参考：
 * https://github.com/Kotlin/kotlinx.coroutines
 */
@Route(path = RouterPath.Event.FlowEventBus)
class FlowEventBusActivity : BasicResponseActivity() {

    private var globalJob: Job? = null
    private var stickyJob: Job? = null
    private var isObserving = false

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription(
            "FlowEventBus 示例：开启监听 / 手动注销 / 普通事件 / 粘性事件 / 生命周期感知",
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
            0 -> observeFlowEventBus()
            1 -> unobserveFlowEventBus()
            2 -> postGlobalEvent()
            3 -> postSticky()
            4 -> showLifecyclePolicy()
        }
    }

    private fun observeFlowEventBus() {
        if (isObserving) {
            appendLog("FlowEventBus 已处于监听状态")
            return
        }
        globalJob?.cancel()
        stickyJob?.cancel()
        globalJob = FlowEventBus.observeEvent<GlobalEvent>(this) {
            appendLog("收到普通事件：${it.message}")
        }
        stickyJob = FlowEventBus.observeEvent<StickyEvent>(this, isSticky = true) {
            appendLog("收到粘性事件：${it.message}")
        }
        isObserving = true
        appendLog("已开启 FlowEventBus 监听")
    }

    private fun unobserveFlowEventBus() {
        if (!isObserving) {
            appendLog("FlowEventBus 当前未监听")
            return
        }
        globalJob?.cancel()
        stickyJob?.cancel()
        globalJob = null
        stickyJob = null
        isObserving = false
        appendLog("已取消 FlowEventBus 监听")
    }

    private fun postGlobalEvent() {
        appendLog("发送普通事件：GlobalEvent")
        FlowEventBus.postEvent(
            this,
            GlobalEvent("FlowEventBus post by Activity"),
        )
    }

    private fun postSticky() {
        appendLog("发送粘性事件：StickyEvent")
        FlowEventBus.postEvent(
            this,
            StickyEvent("FlowEventBus postSticky by Activity"),
        )
    }

    private fun showLifecyclePolicy() {
        appendLog("FlowEventBus.observe 绑定 lifecycleScope + repeatOnLifecycle")
        appendLog("STOP 暂停收集、DESTROY 自动取消 Job；当前监听状态：$isObserving")
    }

    override fun onDestroy() {
        super.onDestroy()
        globalJob?.cancel()
        stickyJob?.cancel()
        isObserving = false
    }
}
