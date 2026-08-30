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
 * 原生 BLE 协程队列与分包传输示例
 *
 * 直击 Android 原生 BLE 最核心的技术痛点：
 * 1. 痛点：Android 系统的 BluetoothGatt 实例在底层仅支持单任务串行执行，
 *    并发发起多个 read/write 操作会导致底层返回 false 或直接丢弃。
 * 2. 解决方案：使用 Kotlin 协程 Channel 构建无阻塞的 FIFO GATT 任务执行队列，
 *    确保每个指令在收到底层 onCharacteristicWrite / onCharacteristicRead 回调后，才派发下一个指令。
 * 3. 大包分包机制 (Chunking)：当传输数据大于当前 MTU 负载（如 20 或 244 字节）时，
 *    自动按 MTU 切片拆包，逐包入队排队发送，接收端拼接还原完整包。
 */
@Route(path = RouterPath.Bluetooth.NativeQueue)
class BleNativeQueueActivity : BasicResponseActivity() {

    // 模拟 BLE 操作请求
    data class BleOperation(
        val id: Int,
        val type: String, // READ / WRITE
        val payload: ByteArray,
        val completion: CompletableDeferred<Boolean>
    )

    // GATT 指令串行队列通道
    private val mOperationChannel = Channel<BleOperation>(capacity = Channel.UNLIMITED)
    private val mTaskIdCounter = AtomicInteger(1)
    private var mMtuPayloadSize = 20 // 默认 MTU (23) - ATT Header (3) = 20 字节

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        startQueueDispatcher()

        showDescription(
            "Android 原生 BLE 协程队列与分包传输示例\n\n" +
                    "解决原生 GATT 并发操作冲突、实现 Channel FIFO 指令排队与大包 Chunking 自动分包\n" +
                    "请点击下方操作项触发"
        )
    }

    /**
     * 启动队列调度器：消费 Channel 中的操作并串行执行
     */
    private fun startQueueDispatcher() {
        lifecycleScope.launch(Dispatchers.IO) {
            for (op in mOperationChannel) {
                withContext(Dispatchers.Main) {
                    appendLog("▶ [队列调度器] 开始执行任务 # (, 大小:  字节)...")
                }

                // 模拟底层真实 GATT 异步操作耗时（如等待 onCharacteristicWrite 回调）
                delay(120)

                withContext(Dispatchers.Main) {
                    appendLog("✓ [队列调度器] 任务 # 底层已响应 (ACK)，释放队列锁")
                }
                op.completion.complete(true)
            }
        }
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "1. 模拟并发提交 5 个 GATT 读写指令 (验证串行排队)",
            "2. 模拟设置 MTU 为 247 字节 (Payload: 244 字节)",
            "3. 模拟重置 MTU 为默认 23 字节 (Payload: 20 字节)",
            "4. 发送 128 字节大数据包 (演示自动分包切片入队)",
            "5. 模拟接收端接收 3 个分包并组包校验完整性"
        )
    }

    override fun onRecyclerClick(position: Int, text: String) {
        when (position) {
            0 -> testConcurrentOperations()
            1 -> {
                mMtuPayloadSize = 244
                appendLog("✓ 已将模拟 MTU 设置为 247 字节，单包有效载荷上限调整为: 244 字节")
            }
            2 -> {
                mMtuPayloadSize = 20
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
            val taskId = mTaskIdCounter.getAndIncrement()
            val type = if (i % 2 == 0) "WRITE" else "READ"
            val dummyData = byteArrayOf(0x01, 0x02, i.toByte())
            enqueueOperation(taskId, type, dummyData)
        }
    }

    private fun enqueueOperation(id: Int, type: String, payload: ByteArray) {
        lifecycleScope.launch {
            val deferred = CompletableDeferred<Boolean>()
            val op = BleOperation(id, type, payload, deferred)
            appendLog("📥 [任务投递] 任务 # () 入队等待排队...")
            mOperationChannel.send(op)
            val result = deferred.await()
            appendLog("🏁 [任务完成] 任务 # 返回结果: ")
        }
    }

    /**
     * 4. 大数据包分包切割发送 (Chunking)
     */
    private fun testChunkingSend() {
        val totalBytes = ByteArray(128) { (it % 256).toByte() }
        appendLog("📦 准备发送 128 字节数据，当前分包大小上限:  字节/包")

        var offset = 0
        var packageIndex = 1
        val totalPackages = (totalBytes.size + mMtuPayloadSize - 1) / mMtuPayloadSize

        while (offset < totalBytes.size) {
            val length = min(mMtuPayloadSize, totalBytes.size - offset)
            val chunk = totalBytes.copyOfRange(offset, offset + length)
            val taskId = mTaskIdCounter.getAndIncrement()

            appendLog("  ├─ 切片分包 [/] ( 字节) 正在提交入队...")
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

        appendLog("  ├─ 收到分包 1 ( 字节): ''")
        appendLog("  ├─ 收到分包 2 ( 字节): ''")
        appendLog("  ├─ 收到分包 3 ( 字节): ''")

        val fullData = chunk1 + chunk2 + chunk3
        val resultText = String(fullData)
        appendLog("✓ 组包完成！完整数据长度:  字节, 还原文本: \"\"")
    }

    override fun onDestroy() {
        super.onDestroy()
        mOperationChannel.close()
    }
}
