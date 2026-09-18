package com.example.william.my.module.bluetooth.nordic_ble.activity

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Bundle
import android.os.ParcelUuid
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.bluetooth.nordic_ble.data.NordicBleManager

/**
 * Nordic 扫描过滤对照 — 系统 BluetoothLeScanner + Nordic 规范 UUID
 *
 * 本页用系统 BluetoothLeScanner 演示 Nordic 规范的服务过滤与 Connectable 区分；
 * Nordic 请求队列与 MTU 分包能力在连接 / 传输页由 BleManager 承担。
 *
 * 核心机制与避坑点：
 * 1. 服务过滤：按 Nordic UART / 标准 GATT Service UUID 配置 ScanFilter，减少无效广播噪音
 * 2. 广播类型：Android O+ 可从 ScanResult.isConnectable 区分可连接设备与 Beacon
 * 3. 高频结果：扫描回调内用 updateLog 按 MAC 原位刷新 RSSI，避免日志爆炸
 * 4. 库栈分工：扫描走系统 API，连接 / 传输走 Nordic BleManager，与原生 / FastBle / Rx 平行对照
 *
 * 官方参考：
 * https://github.com/NordicSemiconductor/Android-BLE-Library
 */
@SuppressLint("MissingPermission")
@Route(path = RouterPath.Bluetooth.NordicScan)
class BleNordicScanActivity : BasicResponseActivity() {

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bleScanner: BluetoothLeScanner? = null
    private var isScanning = false

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result ?: return
            val device = result.device
            val address = device.address ?: "UNKNOWN"
            val name = device.name ?: result.scanRecord?.deviceName ?: "未知设备"
            val rssi = result.rssi
            val isConnectable = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                result.isConnectable
            } else {
                true
            }

            updateLog(
                address,
                "📡 [Nordic] $name ($address) | RSSI: ${rssi}dBm | Connectable: $isConnectable",
            )
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            isScanning = false
            appendLog("✗ Nordic BLE 扫描失败: errorCode=$errorCode")
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        bluetoothAdapter = bluetoothManager?.adapter
        bleScanner = bluetoothAdapter?.bluetoothLeScanner

        showDescription(
            "Nordic BLE 扫描与设备发现示例\n\n" +
                "演示根据 Nordic 规范进行全量扫描、UART 服务过滤扫描与广播包解析\n" +
                "请点击下方操作项",
        )
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 开启全量 BLE 设备扫描 (低延迟模式)",
        "2. 开启过滤扫描 (Nordic UART 0xFFE0 / 0x6E40)",
        "3. 停止扫描",
        "4. 查看 Nordic 广播包最佳实践说明（库特有）",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> startScan(isFilter = false)
            1 -> startScan(isFilter = true)
            2 -> stopScan()
            3 -> showNordicBestPractices()
        }
    }

    private fun startScan(isFilter: Boolean) {
        val scanner = bleScanner
        if (scanner == null) {
            appendLog("✗ 无法获取 BLE 扫描器，请确认蓝牙已开启")
            return
        }

        if (isScanning) {
            appendLog("⚠ 扫描已在进行中")
            return
        }

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        val filters = mutableListOf<ScanFilter>()
        if (isFilter) {
            filters.add(
                ScanFilter.Builder()
                    .setServiceUuid(ParcelUuid(NordicBleManager.SERVICE_UUID))
                    .build(),
            )
            appendLog("正在启动 Nordic UART 服务过滤扫描 (UUID: ${NordicBleManager.SERVICE_UUID})...")
        } else {
            appendLog("正在启动全量 BLE 设备扫描...")
        }

        try {
            scanner.startScan(filters, settings, scanCallback)
            isScanning = true
            appendLog("✓ 扫描已启动，设备信息将在上方实时原位更新")
        } catch (e: Exception) {
            appendLog("✗ 启动扫描异常: ${e.message}")
        }
    }

    private fun stopScan() {
        if (!isScanning) {
            appendLog("当前未在扫描")
            return
        }
        try {
            bleScanner?.stopScan(scanCallback)
            isScanning = false
            appendLog("✓ 已停止扫描")
        } catch (e: Exception) {
            appendLog("✗ 停止扫描异常: ${e.message}")
        }
    }

    private fun showNordicBestPractices() {
        appendLog("── Nordic 官方建议的 BLE 扫描最佳实践 ──")
        appendLog("1. 扫描超时：永远不要无限期扫描，建议在 5~10 秒后自动超时关闭以节省电量。")
        appendLog("2. 过滤优先：尽可能使用 ScanFilter（按 Service UUID 或 Service Data），由蓝牙芯片底层硬件过滤唤醒，减少 CPU 功耗。")
        appendLog("3. 前后台切换：应用进入后台时应降低扫描占空比（SCAN_MODE_LOW_POWER）或暂停扫描。")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isScanning) {
            try {
                bleScanner?.stopScan(scanCallback)
            } catch (_: Exception) {
                // 页面销毁兜底：忽略蓝牙已关闭或未在扫描态时的底层异常
            }
            isScanning = false
        }
    }
}
