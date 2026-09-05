package com.example.william.my.module.kotlin.activity

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

/**
 * Kotlin Channel 与回调桥接 Flow
 *
 * Channel 是协程之间的热通信管道（Hot Stream），支持多个协程安全地传递数据流。
 *
 * 核心特性：
 * 1. 缓冲模式：RENDEZVOUS（无缓冲握手）、BUFFERED（固定缓冲）、CONFLATED（保留最新值）、UNLIMITED（无界缓冲）
 * 2. 生产消费：produce 协程构建器与多消费者竞争消费
 * 3. callbackFlow：传统回调监听器（Listener）转 Flow 的标准桥梁，配合 awaitClose 安全释放资源
 * 4. channelFlow：支持在流构建器内部并发启动多个子协程进行 send 发射
 *
 * https://kotlinlang.org/docs/channels.html
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Route(path = RouterPath.Kotlin.Channel)
class ChannelActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("演示 Kotlin Channel 通道通信、缓冲策略、生产消费模型、callbackFlow 与 channelFlow")
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. Channel 4 种缓冲模式（RENDEZVOUS / BUFFERED / CONFLATED / UNLIMITED）",
        "2. 生产-消费模型（produce 构建器与多消费者竞争）",
        "3. callbackFlow 传统回调桥接（awaitClose 优雅反注册防泄漏）",
        "4. channelFlow 跨子协程并发发射与数据合并",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> testChannelBufferModes()
            1 -> testProduceAndConsume()
            2 -> testCallbackFlow()
            3 -> testChannelFlow()
        }
    }

    // ─────────────────────────────────────────────
    // 1. Channel 4 种缓冲模式对比
    // ─────────────────────────────────────────────
    private fun testChannelBufferModes() {
        lifecycleScope.launch {
            appendLog("【1. 缓冲模式】测试 CONFLATED 通道（仅保留最新值，丢弃溢出数据）...")
            val conflatedChannel = Channel<Int>(capacity = Channel.CONFLATED)

            // 快速发送 1..5
            for (i in 1..5) {
                conflatedChannel.send(i)
                appendLog("【CONFLATED 发送】send: $i")
            }
            conflatedChannel.close()

            // 延迟接收
            delay(100)
            for (item in conflatedChannel) {
                appendLog("【CONFLATED 接收】receive: $item（历史被覆盖，仅收到最新值）")
            }

            appendLog("【1. 缓冲模式】测试 BUFFERED 通道（容量 = 2，溢出时挂起发送方）...")
            val bufferedChannel = Channel<String>(capacity = 2, onBufferOverflow = BufferOverflow.SUSPEND)
            bufferedChannel.send("Msg-1")
            bufferedChannel.send("Msg-2")
            appendLog("【BUFFERED 发送】成功写入 2 条数据（通道已满）")
            bufferedChannel.close()

            for (msg in bufferedChannel) {
                appendLog("【BUFFERED 接收】receive: $msg")
            }
        }
    }

    // ─────────────────────────────────────────────
    // 2. 生产-消费模型 (produce 构建器与多消费者竞争)
    // ─────────────────────────────────────────────
    private fun testProduceAndConsume() {
        lifecycleScope.launch {
            appendLog("【2. 生产-消费】启动 produce 生产者协程，发射 Task 1..6...")

            val producer = produce(capacity = Channel.BUFFERED) {
                for (x in 1..6) {
                    send("Task #$x")
                    delay(100)
                }
            }

            // 启动 2 个消费者工作协程竞争消费
            val worker1 = launch {
                for (task in producer) {
                    appendLog("【Worker-1 消费】处理 -> $task")
                    delay(150)
                }
            }
            val worker2 = launch {
                for (task in producer) {
                    appendLog("【Worker-2 消费】处理 -> $task")
                    delay(120)
                }
            }

            worker1.join()
            worker2.join()
            appendLog("【2. 生产-消费】所有任务已被两个工作协程协同消费完毕。")
        }
    }

    // ─────────────────────────────────────────────
    // 3. callbackFlow 传统回调桥接与 awaitClose 安全注销
    // ─────────────────────────────────────────────
    interface MockLocationListener {
        fun onLocationUpdate(latitude: Double, longitude: Double)
    }

    class MockLocationManager {
        private var listener: MockLocationListener? = null
        private var isRunning = false

        fun register(listener: MockLocationListener) {
            this.listener = listener
            isRunning = true
        }

        fun unregister() {
            this.listener = null
            isRunning = false
        }

        fun triggerLocation(lat: Double, lng: Double) {
            if (isRunning) {
                listener?.onLocationUpdate(lat, lng)
            }
        }
    }

    private fun testCallbackFlow() {
        val locationManager = MockLocationManager()

        // 使用 callbackFlow 封装传统回调
        val locationFlow = callbackFlow {
            val listener = object : MockLocationListener {
                override fun onLocationUpdate(latitude: Double, longitude: Double) {
                    trySend("GPS 定位: (纬度=$latitude, 经度=$longitude)")
                }
            }

            appendLog("【callbackFlow】注册 MockLocationListener")
            locationManager.register(listener)

            // 挂起协程直到 Flow 被下游取消或关闭，在此处执行反注册以彻底防止内存泄漏
            awaitClose {
                appendLog("【callbackFlow】触发 awaitClose: 安全注销 MockLocationListener")
                locationManager.unregister()
            }
        }

        lifecycleScope.launch {
            appendLog("【3. callbackFlow】启动收集前 2 次定位数据（take(2) 后自动取消）...")

            // 模拟后台周期回调
            val mockGpsJob = launch {
                delay(100)
                locationManager.triggerLocation(39.9042, 116.4074)
                delay(100)
                locationManager.triggerLocation(31.2304, 121.4737)
                delay(100)
                locationManager.triggerLocation(23.1291, 113.2644)
            }

            locationFlow.take(2).collect { locationStr ->
                appendLog("【3. callbackFlow 收集到】$locationStr")
            }

            mockGpsJob.cancel()
        }
    }

    // ─────────────────────────────────────────────
    // 4. channelFlow 跨子协程并发发射
    // ─────────────────────────────────────────────
    private fun testChannelFlow() {
        lifecycleScope.launch {
            appendLog("【4. channelFlow】在 channelFlow 内部启动多个子协程并发发射数据...")

            val combinedFlow = channelFlow {
                // 子任务 1
                launch {
                    delay(150)
                    send("来源 A (用户中心): 状态正常")
                }

                // 子任务 2
                launch {
                    delay(80)
                    send("来源 B (订单系统): 待支付 2 笔")
                }

                // 子任务 3
                launch {
                    delay(200)
                    send("来源 C (消息中心): 新通知 5 条")
                }
            }

            combinedFlow.collect { item ->
                appendLog("【4. channelFlow 接收】$item")
            }
            appendLog("【4. channelFlow】所有并发子协程的数据发射已合并收集完成。")
        }
    }
}
