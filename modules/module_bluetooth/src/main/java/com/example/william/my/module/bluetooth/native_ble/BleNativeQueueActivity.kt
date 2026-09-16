package com.example.william.my.module.bluetooth.native_ble

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.min

/**
 * 原生 BLE 协程队列与分包传输 — GATT 串行化
 *
 * BluetoothGatt 底层单任务：并发 read/write 会失败或被丢弃。本页用协程 Channel 做 FIFO 队列，并演示 MTU 分包。
 *
 * 核心机制与避坑点：
 * 1. 串行队列：Channel 缓冲 GATT 指令，回调完成后再派发下一条
 * 2. 协程等待：CompletableDeferred 挂起至 onCharacteristicRead/Write
 * 3. 自动分包：按当前 MTU 负载切片入队，接收侧拼接
 * 4. 可取消：生命周期内取消 Job 即停止派发
 *
 * https://developer.android.google.cn/guide/topics/connectivity/bluetooth/ble
 */
@Route(path = RouterPath.Bluetooth.NativeQueue)
class BleNativeQueueActivity : BasicResponseActivity() {

    // 模拟 BLE 操作请求
    /**
     * BLE 操作描述
     *
     * 封装一次 BLE 读写的操作类型与目标数据。
     */
    data class BleOperation(
        val id: Int,
        val type: String, // READ / WRITE
        val payload: ByteArray,
        val completion: CompletableDeferred<Boolean>,
    )

    // GATT 指令串行队列通道
    private val operationChannel = Channel<BleOperation>(capacity = Channel.UNLIMITED)
    private val taskIdCounter = AtomicInteger(1)
    private var mtuPayloadSize = 20 // 默认 MTU (23) - ATT Header (3) = 20 字节

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        startQueueDispatcher()

        showDescription(
            "Android 原生 BLE 协程队列与分包传输示例\n\n" +
                "解决原生 GATT 并发操作冲突、实现 Channel FIFO 指令排队与大包 Chunking 自动分包\n" +
                "请点击下方操作项触发",
        )
    }

    /**
     * 启动队列调度器：消费 Channel 中的操作并串行执行
     */
    private fun startQueueDispatcher() {
        lifecycleScope.launch(Dispatchers.IO) {
            for (op in operationChannel) {
                withContext(Dispatchers.Main) {
                    appendLog("▶ [队列调度器] 开始执行任务 #${op.id} (${op.type}, 大小: ${op.payload.size} 字节)...")
                }

                // 模拟底层真实 GATT 异步操作耗时（如等待 onCharacteristicWrite 回调）
                delay(120)

                withContext(Dispatchers.Main) {
                    appendLog("✓ [队列调度器] 任务 #${op.id} 底层已响应 (ACK)，释放队列锁")
                }
                op.completion.complete(true)
            }
        }
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 模拟并发提交 5 个 GATT 读写指令 (验证串行排队)",
        "2. 模拟设置 MTU 为 247 字节 (Payload: 244 字节)",
        "3. 模拟重置 MTU 为默认 23 字节 (Payload: 20 字节)",
        "4. 发送 128 字节大数据包 (演示自动分包切片入队)",
        "5. 模拟接收端接收 3 个分包并组包校验完整性",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> testConcurrentOperations()
            1 -> {
                mtuPayloadSize = 244
                appendLog("✓ 已将模拟 MTU 设置为 247 字节，单包有效载荷上限调整为: 244 字节")
            }
            2 -> {
                mtuPayloadSize = 20
                appendLog("✓ 已将模拟 MTU 重置为 23 字节，单包有效载荷上限调整为: 20 字节")
            }
            3 -> testChunkingSend()
            4 -> testChunkingReceive()
        }
    }

    /**
     * 1. 模拟瞬间并发提交 5 个指令，通过 Channel 保证串行安全执行
     */
    private fun testConcurrentOperations() {
        appendLog("🚀 瞬间并发提交 5 个 GATT 操作到协程队列...")
        for (i in 1..5) {
            val taskId = taskIdCounter.getAndIncrement()
            val type = if (i % 2 == 0) "WRITE" else "READ"
            val dummyData = byteArrayOf(0x01, 0x02, i.toByte())
            enqueueOperation(taskId, type, dummyData)
        }
    }

    private fun enqueueOperation(id: Int, type: String, payload: ByteArray) {
        lifecycleScope.launch {
            val deferred = CompletableDeferred<Boolean>()
            val op = BleOperation(id, type, payload, deferred)
            appendLog("📥 [任务投递] 任务 #$id ($type) 入队等待排队...")
            operationChannel.send(op)
            val result = deferred.await()
            appendLog("🏁 [任务完成] 任务 #$id 返回结果: $result")
        }
    }

    /**
     * 4. 大数据包分包切割发送 (Chunking)
     */
    private fun testChunkingSend() {
        val totalBytes = ByteArray(128) { (it % 256).toByte() }
        appendLog("📦 准备发送 128 字节数据，当前分包大小上限: $mtuPayloadSize 字节/包")

        var offset = 0
        var packageIndex = 1
        val totalPackages = (totalBytes.size + mtuPayloadSize - 1) / mtuPayloadSize

        while (offset < totalBytes.size) {
            val length = min(mtuPayloadSize, totalBytes.size - offset)
            val chunk = totalBytes.copyOfRange(offset, offset + length)
            val taskId = taskIdCounter.getAndIncrement()

            appendLog("  ├─ 切片分包 [$packageIndex/$totalPackages] (${chunk.size} 字节) 正在提交入队...")
            enqueueOperation(taskId, "WRITE_CHUNK", chunk)

            offset += length
            packageIndex++
        }
    }

    /**
     * 5. 模拟接收端组包还原
     */
    private fun testChunkingReceive() {
        appendLog("📥 模拟接收连续 3 个 Notify 切片包并合并还原:")
        val chunk1 = "Hello, ".toByteArray()
        val chunk2 = "Reactive BLE Queue ".toByteArray()
        val chunk3 = "Chunking Success!".toByteArray()

        appendLog("  ├─ 收到分包 1 (${chunk1.size} 字节): '${String(chunk1)}'")
        appendLog("  ├─ 收到分包 2 (${chunk2.size} 字节): '${String(chunk2)}'")
        appendLog("  ├─ 收到分包 3 (${chunk3.size} 字节): '${String(chunk3)}'")

        val fullData = chunk1 + chunk2 + chunk3
        val resultText = String(fullData)
        appendLog("✓ 组包完成！完整数据长度: ${fullData.size} 字节, 还原文本: \"$resultText\"")
    }

    override fun onDestroy() {
        super.onDestroy()
        operationChannel.close()
    }
}
