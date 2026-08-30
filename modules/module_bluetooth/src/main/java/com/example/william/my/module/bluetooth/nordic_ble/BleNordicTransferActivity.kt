package com.example.william.my.module.bluetooth.nordic_ble

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import java.util.Locale

/**
 * Nordic BLE 大数据流式传输示例
 *
 * 【Nordic Android-BLE-Library —— 工业级全能型（重型卡车）】
 * - 功能覆盖：全都有，而且在稳定性、边缘异常处理上最强。
 * - 特点：由蓝牙芯片原厂（Nordic）官方维护。
 *   • 队列最稳：彻底解决多任务并发冲突。
 *   • 大包自动化：内置了自动按 MTU 切包（.split()）和自动拼包（.merge()），不用自己算 offset。
 *   • 现代化：支持 Kotlin 协程 suspend 挂起调用，代码不用写一层层回调。
 * - 适合谁：智能硬件大厂、医疗设备、车载、OTA 固件升级、对稳定性要求极高的项目。
 *
 * 演示特性：
 * 1. 自动分包切割 (.split()): 发送 1KB+ 大数据时，Nordic 会根据当前协商的 MTU，
 *    自动将整块数据切片并依次排队发送，无需开发者手动计算 offset 与分包循环。
 * 2. 自动拼包合并 (.merge()): 接收到外设多次推送的分包时，通过 PacketMerger 自动拼接为完整数据帧。
 * 3. 流量控制与拥塞避免：底层自动处理 Write Response / No Response 的传输速率控制。
 */
@Route(path = RouterPath.Bluetooth.NordicTransfer)
class BleNordicTransferActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        showDescription(
            "Nordic BLE 大数据流式传输示例\n\n" +
                    "演示 Nordic 内置的自动分包切割 (.split())、流式拼包合并 (.merge()) 与可靠传输\n" +
                    "请点击下方操作项"
        )
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "1. 演示 Nordic 自动分包切割原理 (.split())",
            "2. 演示 Nordic 自动流式拼包合并原理 (.merge())",
            "3. 模拟发送 512 字节大文件切片并统计传输吞吐率",
            "4. 查看 Nordic 工业级架构优势总结"
        )
    }

    override fun onRecyclerClick(position: Int, text: String) {
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
