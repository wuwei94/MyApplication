package com.example.william.my.module.performance.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.MessageQueue
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * IdleHandler 主线程空闲调度示例
 *
 * 本示例演示 IdleHandler 的标准用法与核心机制：
 * 1. 核心机制：当主线程 MessageQueue 处于空闲状态（无即时消息处理或处于延时等待）时触发回调。
 * 2. 单次执行（返回 false）：执行完毕后自动从队列中移除，常用于延迟初始化次要 SDK、预加载次级数据。
 * 3. 持续监听（返回 true）：执行完毕后仍保留在队列中，每次空闲都会再次触发，常用于分片任务调度与内存清理。
 * 4. 显式注销（removeIdleHandler）：在组件销毁时主动移除持续监听的 IdleHandler，避免内存泄漏。
 */
@Route(path = RouterPath.Performance.IdleHandler)
class IdleHandlerActivity : BasicResponseActivity() {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var continuousIdleHandler: MessageQueue.IdleHandler? = null
    private var continuousCount = 0

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("IdleHandler 主线程空闲调度示例\n演示单次执行、持续空闲监听、生命周期注销与延迟任务调度")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "1. 注册单次 IdleHandler（返回 false，执行后自动注销）",
            "2. 注册持续监听 IdleHandler（返回 true，每次空闲触发）",
            "3. 模拟发送连续 Handler 消息（观察空闲间隙调度）",
            "4. 主动注销持续监听的 IdleHandler（removeIdleHandler）"
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> testOneShotIdleHandler()
            1 -> testContinuousIdleHandler()
            2 -> testPostMessagesAndObserveIdle()
            3 -> removeContinuousIdleHandler()
        }
    }

    /**
     * 示例 1：单次执行 IdleHandler
     * 返回 false 表示该 IdleHandler 执行完一次后自动从队列移除。
     */
    private fun testOneShotIdleHandler() {
        appendLog("【示例 1】注册单次 IdleHandler（用于延迟初始化次要组件）...")

        Looper.myQueue().addIdleHandler {
            appendLogAccent("  [IdleHandler 触发] 当前线程：${Thread.currentThread().name}，主线程空闲，执行次要 SDK 延迟初始化！")
            false // 返回 false：执行一次后自动注销
        }
    }

    /**
     * 示例 2：持续监听 IdleHandler
     * 返回 true 表示每次主线程空闲时都会再次触发。
     */
    private fun testContinuousIdleHandler() {
        if (continuousIdleHandler != null) {
            appendLog("  持续监听 IdleHandler 已经在运行中...")
            return
        }

        appendLog("【示例 2】注册持续监听 IdleHandler（返回 true）...")
        continuousCount = 0

        val handler = MessageQueue.IdleHandler {
            continuousCount++
            updateLog("IDLE_CONTINUOUS", "  [持续空闲监听] 主线程空闲第 $continuousCount 次触发回调...")

            // 示例：触发 5 次后自动停止，或者通过 removeContinuousIdleHandler 手动停止
            if (continuousCount >= 5) {
                appendLogAccent("  已连续触发 5 次空闲回调，自动停止监听。")
                continuousIdleHandler = null
                false // 停止监听
            } else {
                true // 继续保持监听
            }
        }

        continuousIdleHandler = handler
        Looper.myQueue().addIdleHandler(handler)
    }

    /**
     * 示例 3：发送连续 Handler 消息观察调度时机
     */
    private fun testPostMessagesAndObserveIdle() {
        appendLog("【示例 3】主线程投递 3 个耗时 100ms 的任务，随后观察空闲回调...")

        for (i in 1..3) {
            mainHandler.post {
                Thread.sleep(100)
                appendLog("  -> 执行主线程任务 $i")
            }
        }

        // 注册一个空闲任务
        Looper.myQueue().addIdleHandler {
            appendLogAccent("  [空闲任务触发] 所有主线程排队消息执行完毕，队列进入空闲状态！")
            false
        }
    }

    /**
     * 示例 4：主动移除 IdleHandler
     */
    private fun removeContinuousIdleHandler() {
        continuousIdleHandler?.let {
            Looper.myQueue().removeIdleHandler(it)
            continuousIdleHandler = null
            removeUpdatingLog("IDLE_CONTINUOUS")
            appendLogAccent("已调用 Looper.myQueue().removeIdleHandler() 安全注销！")
        } ?: run {
            appendLog("当前未运行持续监听的 IdleHandler")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 页面销毁时，确保未完成的持续 IdleHandler 被安全注销，防止内存泄漏
        continuousIdleHandler?.let {
            Looper.myQueue().removeIdleHandler(it)
            continuousIdleHandler = null
        }
        mainHandler.removeCallbacksAndMessages(null)
    }
}
