package com.example.william.my.module.event.activity

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.eventbus.flow.FlowEventBus
import com.example.william.my.module.event.event.GlobalEvent
import com.example.william.my.module.event.event.StickyEvent
import kotlinx.coroutines.Job

/**
 * FlowEventBus — 基于 Kotlin Flow 的事件总线
 *
 * 利用 Kotlin SharedFlow + repeatOnLifecycle 实现的协程事件总线。
 *
 * 核心特性：
 * 1. 协程驱动：基于 Kotlin 协程，性能优秀
 * 2. 生命周期感知：利用 repeatOnLifecycle 自动管理订阅
 * 3. 粘性事件：支持粘性事件，使用 SharedFlow 的 replay 参数
 * 4. 线程安全：协程天然线程安全，无需额外处理
 *
 * 特性对比：
 * 延迟发送: ✅ | 有序接收: ✅ | Sticky: ✅ | 生命周期感知: ✅ | 可跨进程: ❌ | 线程分发: ✅
 *
 * 基本用法：
 * ```kotlin
 * // 订阅事件
 * val job = FlowEventBus.observeEvent<MessageEvent>(this) { event ->
 *     // 处理事件
 * }
 *
 * // 发送事件
 * FlowEventBus.postEvent(this, MessageEvent("Hello"))
 *
 * // 取消订阅
 * job.cancel()
 * ```
 *
 * 适用场景：
 * - Kotlin 项目中的事件通信
 * - 需要协程支持的场景
 * - 现代化的 Android 架构
 *
 * https://github.com/Kotlin/kotlinx.coroutines
 */
@Route(path = RouterPath.Event.FlowEventBus)
class FlowEventBusActivity : BasicResponseActivity() {

    private var mGlobalJob: Job? = null
    private var mStickyJob: Job? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("FlowEventBus 示例\n\n请点击下方按钮注册监听或发送事件")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "注册/注销监听 (Observe/Unobserve)",
            "发送普通事件 (Post Event)",
            "发送粘性事件 (Post Sticky Event)",
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> toggleObserve()
            1 -> {
                appendLog("发送普通事件：GlobalEvent")
                FlowEventBus.postEvent(
                    this,
                    GlobalEvent("FlowEventBus post by Activity")
                )
            }
            2 -> {
                appendLog("发送粘性事件：StickyEvent")
                FlowEventBus.postEvent(
                    this,
                    StickyEvent("FlowEventBus postSticky by Activity")
                )
            }
        }
    }

    private fun toggleObserve() {
        val isObserving = (mGlobalJob?.isActive == true) || (mStickyJob?.isActive == true)
        if (!isObserving) {
            mGlobalJob?.cancel()
            mStickyJob?.cancel()
            mGlobalJob = FlowEventBus.observeEvent<GlobalEvent>(this) {
                appendLog("收到普通事件：${it.message}")
            }
            mStickyJob = FlowEventBus.observeEvent<StickyEvent>(this, isSticky = true) {
                appendLog("收到粘性事件：${it.message}")
            }
            appendLog("已开启 FlowEventBus 监听")
        } else {
            mGlobalJob?.cancel()
            mStickyJob?.cancel()
            mGlobalJob = null
            mStickyJob = null
            appendLog("已取消 FlowEventBus 监听")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mGlobalJob?.cancel()
        mStickyJob?.cancel()
    }
}