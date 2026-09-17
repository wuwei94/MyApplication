package com.example.william.my.module.bluetooth.fast_ble

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.scan.BleScanRuleConfig
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import java.util.UUID

/**
 * FastBle 扫描与规则过滤 — 链式 API
 *
 * FastBle 以字符串 UUID 与三段回调简化 BLE 扫描，与原生 / Nordic / Rx 扫描栈平行对照。
 *
 * 核心机制与避坑点：
 * 1. 规则链：`BleScanRuleConfig` 统一配置 Service UUID / 设备名 / MAC / 超时，扫描前需先 initScanRule
 * 2. 一键扫描：`BleManager.getInstance().scan(...)` 内部处理适配器与权限前置检查
 * 3. 生命周期回调：onScanStarted / onScanning / onScanFinished 三段，onScanning 内适合 updateLog
 * 4. 全局初始化：`BleManager.getInstance().init(application)` 必须在扫描前调用，否则空指针
 *
 * 官方参考：
 * https://github.com/Jasonchenlijian/FastBle
 */
@Route(path = RouterPath.Bluetooth.FastScan)
class BleFastScanActivity : BasicResponseActivity() {

    private var isScanning = false

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        BleManager.getInstance().init(application)
        BleManager.getInstance()
            .enableLog(true)
            .setReConnectCount(1, 2000)
            .setOperateTimeout(5000)

        showDescription(
            "FastBle 扫描与规则过滤示例\n\n" +
                "演示 FastBle 的 BleScanRuleConfig 链式配置与 BleScanCallback 设备发现\n" +
                "请点击下方操作项",
        )
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 配置全局扫描规则 (超时 10 秒 / 无过滤)",
        "2. 配置带 Service UUID 过滤的规则 (0xFFE0)",
        "3. 开启 FastBle 扫描",
        "4. 取消/停止扫描",
        "5. 查看 FastBle 设计特点说明",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> configScanRule(isFilter = false)
            1 -> configScanRule(isFilter = true)
            2 -> startFastScan()
            3 -> stopFastScan()
            4 -> showFastBleCharacteristics()
        }
    }

    private fun configScanRule(isFilter: Boolean) {
        val builder = BleScanRuleConfig.Builder()
            .setScanTimeOut(10000)
            .setAutoConnect(false)

        if (isFilter) {
            val serviceUuids = arrayOf(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"))
            builder.setServiceUuids(serviceUuids)
            appendLog("✓ 已配置 FastBle 过滤扫描规则 (Service UUID: 0xFFE0, 超时: 10s)")
        } else {
            appendLog("✓ 已配置 FastBle 全量扫描规则 (无过滤, 超时: 10s)")
        }

        val scanRuleConfig = builder.build()
        BleManager.getInstance().initScanRule(scanRuleConfig)
    }

    private fun startFastScan() {
        if (isScanning) {
            appendLog("⚠ 扫描正在进行中")
            return
        }

        appendLog("🚀 启动 FastBle 扫描...")
        BleManager.getInstance().scan(object : BleScanCallback() {
            override fun onScanStarted(success: Boolean) {
                isScanning = success
                if (success) {
                    appendLog("✓ FastBle 扫描已启动，监听中...")
                } else {
                    appendLog("✗ FastBle 启动扫描失败")
                }
            }

            override fun onScanning(bleDevice: BleDevice?) {
                bleDevice ?: return
                val name = if (bleDevice.name.isNullOrEmpty()) "未知设备" else bleDevice.name
                val mac = bleDevice.mac ?: "UNKNOWN"
                val rssi = bleDevice.rssi

                updateLog(
                    mac,
                    "📡 [FastBle] $name ($mac) | RSSI: ${rssi}dBm",
                )
            }

            override fun onScanFinished(scanResultList: MutableList<BleDevice>?) {
                isScanning = false
                val count = scanResultList?.size ?: 0
                appendLog("🏁 FastBle 扫描结束，共发现 $count 个设备")
            }
        })
    }

    private fun stopFastScan() {
        if (!isScanning) {
            appendLog("当前未在扫描")
            return
        }
        BleManager.getInstance().cancelScan()
        isScanning = false
        appendLog("✓ 已取消 FastBle 扫描")
    }

    private fun showFastBleCharacteristics() {
        appendLog("── FastBle 设计特点与核心 API ──")
        appendLog("1. 单例模式：通过 BleManager.getInstance() 全局统一管理，API 上手极快。")
        appendLog("2. 链式配置：BleScanRuleConfig 支持设置 Service UUIDs、Device Names、Device MAC、AutoConnect 等。")
        appendLog("3. 回调简化：提供 BleGattCallback、BleReadCallback、BleWriteCallback、BleNotifyCallback 等专有回调，隐藏了大部分原生底层模板代码。")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isScanning) {
            BleManager.getInstance().cancelScan()
            isScanning = false
        }
    }
}
