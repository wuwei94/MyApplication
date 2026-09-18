package com.example.william.my.module.bluetooth.nordic_ble.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import java.util.Locale

/**
 * Nordic BLE 大数据流式传输原理 — split / merge
 *
 * 本页以本地日志与吞吐演算说明 Nordic `.split()` / `.merge()` 的分包与拼包模型，
 * 不发起真实 GATT 写读；真实连接与 MTU 协商见 Nordic 连接页。
 *
 * 核心机制与避坑点：
 * 1. 自动切包：`.split()` 按协商 MTU 负载拆分 1KB+ 载荷，逐片入队并等待底层 ACK
 * 2. 自动拼包：`.merge()` 依据自定义帧头/长度将多次 Notification 合并为完整业务帧后回调
 * 3. 流控：Write Default / No Response 的发送速率与重传由库内请求队列处理
 * 4. 示意边界：本页为原理演示，不替代真机传输联调
 *
 * 官方参考：
 * https://github.com/NordicSemiconductor/Android-BLE-Library
 */
@Route(path = RouterPath.Bluetooth.NordicTransfer)
class BleNordicTransferActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        showDescription(
            "Nordic BLE 大数据流式传输原理示意\n\n" +
                "本地演算 `.split()` / `.merge()` 分包拼包模型与吞吐，不发起真实 GATT 传输\n" +
                "请点击下方操作项",
        )
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 演示 Nordic 自动分包切割原理 (.split())",
        "2. 演示 Nordic 自动流式拼包合并原理 (.merge())",
        "3. 模拟发送 512 字节大文件切片并统计传输吞吐率",
        "4. 查看 Nordic 工业级架构优势总结",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> demoSplitPrinciple()
            1 -> demoMergePrinciple()
            2 -> simulateHighThroughputTransfer()
            3 -> showArchitectureSummary()
        }
    }

    private fun demoSplitPrinciple() {
        appendLog("── Nordic .split() 自动分包机制 ──")
        appendLog("代码调用方式：")
        appendLog("  writeCharacteristic(char, largeData)")
        appendLog("      .split(DefaultMtuSplitter()) // 依据当前 MTU 自动切片")
        appendLog("      .done { appendLog(\"✓ 完整大包所有切片全部发送完毕\") }")
        appendLog("      .enqueue()")
        appendLog("优势：无需应用层编写任何 while/offset 循环，Nordic 内部自动处理每个切片的 ACK 回调。")
    }

    private fun demoMergePrinciple() {
        appendLog("── Nordic .merge() 自动拼包机制 ──")
        appendLog("代码调用方式：")
        appendLog("  setNotificationCallback(char)")
        appendLog("      .merge(HeaderBasedPacketMerger()) // 根据自定义帧头/长度拼包")
        appendLog("      .with { device, data ->")
        appendLog("          // 仅在整帧完整接收并组装完毕后才触发此回调")
        appendLog("          appendLog(\"✓ 收到完整组装数据: \" + data.size() + \" 字节\")")
        appendLog("      }")
        appendLog("优势：彻底解耦分包通信细节，上层业务只需关注完整业务数据帧。")
    }

    private fun simulateHighThroughputTransfer() {
        appendLog("🚀 开始模拟 Nordic 大数据传输测试 (数据大小: 512 字节)...")
        val mtu = 247
        val payloadPerPacket = mtu - 3 // 244 字节
        val totalPackets = (512 + payloadPerPacket - 1) / payloadPerPacket

        appendLog("当前协商 MTU: " + mtu + " 字节 | 单包有效载荷: " + payloadPerPacket + " 字节 | 预计切片数: " + totalPackets + " 包")
        val startTime = System.currentTimeMillis()

        for (i in 1..totalPackets) {
            val size = if (i == totalPackets) 512 % payloadPerPacket else payloadPerPacket
            val actualSize = if (size == 0) payloadPerPacket else size
            appendLog("  ├─ [Nordic 队列] 发送切片 #" + i + "/" + totalPackets + " (大小: " + actualSize + " 字节) -> 收到底层 ACK")
        }

        val duration = System.currentTimeMillis() - startTime + 45 // 模拟轻微网络延迟
        val speedKbps = (512 * 8.0) / duration
        val speedText = String.format(Locale.US, "%.2f", speedKbps)
        appendLog("✓ 512 字节传输完成！耗时: " + duration + "ms, 传输速率: " + speedText + " kbps")
    }

    private fun showArchitectureSummary() {
        appendLog("── Android 原生方案 vs Nordic 库架构对比 ──")
        appendLog("1. 原生 SDK：底层完全暴露，无队列保护，并发操作极易丢包冲突，适合学习底层机制。")
        appendLog("2. Nordic 库：基于 BleManager 状态机模型，内置严格串行 FIFO 请求队列，全面支持 suspend 协程、自动重连与自动分包/拼包，是企业级 IoT 开发的最佳实践。")
    }
}
