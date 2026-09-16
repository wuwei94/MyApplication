package com.example.william.my.module.bluetooth.native_ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.ParcelUuid
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import java.util.UUID

/**
 * 原生 BLE 扫描与过滤 — BluetoothLeScanner
 *
 * 演示 Android 原生 BLE 扫描生命周期与广播数据解析，不依赖第三方蓝牙库。
 *
 * 库选型定位（四栈横评中的原生）：
 * - 维护方：Android 系统 SDK，零第三方依赖
 * - 能力要点：ScanFilter / ScanSettings / 广播包结构完全暴露，可控性最强但模板代码最多
 * - 适合：不想引入三方库、需要精确控制扫描参数或学习底层机制的项目
 *
 * 核心特性：
 * 1. 运行时权限：兼容 Android 12+ BLUETOOTH_SCAN 与 Android 11- ACCESS_FINE_LOCATION
 * 2. 适配器状态：BluetoothAdapter 可用性感知
 * 3. 扫描模式：SCAN_MODE_LOW_LATENCY / LOW_POWER 等
 * 4. 规则过滤：ScanFilter（Service UUID、设备名）
 * 5. 广播解析：RSSI、Service UUIDs、Manufacturer Data
 *
 * 基本用法：
 * ```kotlin
 * scanner.startScan(filters, settings, scanCallback)
 * // onScanResult 中解析 result.scanRecord 后 updateLog(key, ...)
 * scanner.stopScan(scanCallback)
 * ```
 *
 * 适用场景：
 * - 发现周边 BLE 设备并解析广播
 * - 需要系统原生 API、不引入三方库的项目
 * - 与连接/队列示例串联的扫描入口
 *
 * https://developer.android.google.cn/guide/topics/connectivity/bluetooth/ble
 */
@SuppressLint("MissingPermission")
@Route(path = RouterPath.Bluetooth.NativeScan)
class BleNativeScanActivity : BasicResponseActivity() {

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bleScanner: BluetoothLeScanner? = null
    private var isScanning = false

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        val allGranted = result.values.all { it }
        if (allGranted) {
            appendLog("✓ 蓝牙与位置权限全部已授予")
        } else {
            appendLog("✗ 部分权限被拒绝，可能无法正常扫描 BLE 设备")
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result ?: return
            val device = result.device
            val address = device.address ?: "UNKNOWN"
            val name = device.name ?: result.scanRecord?.deviceName ?: "未知设备"
            val rssi = result.rssi
            val serviceUuids = result.scanRecord?.serviceUuids?.joinToString { it.uuid.toString().substring(0, 8) } ?: "无"

            // 使用 updateLog 原位更新设备列表，避免高频刷屏
            updateLog(
                address,
                "📡 [$name] ($address) | RSSI: ${rssi}dBm | UUIDs: $serviceUuids",
            )
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            appendLog("收到批量扫描结果，数量: ${results?.size ?: 0}")
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            isScanning = false
            val errorMsg = when (errorCode) {
                SCAN_FAILED_ALREADY_STARTED -> "扫描已在运行中"
                SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> "应用注册失败"
                SCAN_FAILED_FEATURE_UNSUPPORTED -> "设备不支持此扫描模式"
                SCAN_FAILED_INTERNAL_ERROR -> "蓝牙底层内部错误"
                else -> "未知错误 ($errorCode)"
            }
            appendLog("✗ 扫描失败: $errorMsg")
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        bluetoothAdapter = bluetoothManager?.adapter
        bleScanner = bluetoothAdapter?.bluetoothLeScanner

        showDescription(
            "Android 原生 BLE 扫描示例\n\n" +
                "支持权限检测、低延迟/过滤扫描、高频 RSSI 原位刷新与广播数据解析\n" +
                "请点击下方操作项开始",
        )
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 检查并申请蓝牙/位置权限",
        "2. 检查蓝牙适配器开关状态",
        "3. 开启 BLE 连续扫描 (低延迟模式)",
        "4. 开启 BLE 过滤扫描 (带 UUID 过滤)",
        "5. 停止 BLE 扫描",
        "6. 模拟解析 BLE 广播包结构",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> checkAndRequestPermissions()
            1 -> checkBluetoothAdapterState()
            2 -> startBleScan(isFilter = false)
            3 -> startBleScan(isFilter = true)
            4 -> stopBleScan()
            5 -> simulateParseScanRecord()
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        }

        val ungranted = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (ungranted.isEmpty()) {
            appendLog("✓ 当前所需权限均已授予")
        } else {
            appendLog("正在申请权限: ")
            permissionLauncher.launch(ungranted.toTypedArray())
        }
    }

    private fun checkBluetoothAdapterState() {
        val adapter = bluetoothAdapter
        if (adapter == null) {
            appendLog("✗ 当前设备不支持蓝牙功能")
            return
        }
        val isEnabled = adapter.isEnabled
        val stateDesc = if (isEnabled) "已开启 (ON)" else "已关闭 (OFF)"
        appendLog("蓝牙适配器状态: $stateDesc, 扫描器实例: $bleScanner")
    }

    private fun startBleScan(isFilter: Boolean) {
        if (bleScanner == null) {
            val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            bluetoothAdapter = bluetoothManager?.adapter
            bleScanner = bluetoothAdapter?.bluetoothLeScanner
        }

        val scanner = bleScanner
        if (scanner == null) {
            appendLog("✗ 无法获取 BluetoothLeScanner，请确认蓝牙已开启")
            return
        }

        if (isScanning) {
            appendLog("⚠ 扫描已在运行中，请先停止再启动")
            return
        }

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(0)
            .build()

        val filters = mutableListOf<ScanFilter>()
        if (isFilter) {
            // 演示添加通用的 Service UUID 过滤（如心率服务 0x180D 或标准测试服务）
            val testUuid = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
            filters.add(
                ScanFilter.Builder()
                    .setServiceUuid(ParcelUuid(testUuid))
                    .build(),
            )
            appendLog("启动带过滤条件的 BLE 扫描 (UUID: 0x180D)...")
        } else {
            appendLog("启动全量 BLE 扫描 (低延迟模式)...")
        }

        try {
            scanner.startScan(filters, settings, scanCallback)
            isScanning = true
            appendLog("✓ 扫描已开启，正在监听广播信号 (设备条目将在上方原位更新)...")
        } catch (e: Exception) {
            appendLog("✗ 启动扫描异常: ${e.message}")
        }
    }

    private fun stopBleScan() {
        if (!isScanning) {
            appendLog("当前未处于扫描状态")
            return
        }
        try {
            bleScanner?.stopScan(scanCallback)
            isScanning = false
            appendLog("✓ 已停止 BLE 扫描")
        } catch (e: Exception) {
            appendLog("✗ 停止扫描异常: ${e.message}")
        }
    }

    private fun simulateParseScanRecord() {
        appendLog("── BLE 广播包 (AdvData / ScanRecord) 结构说明 ──")
        appendLog("1. Flags (AD Type 0x01): 发现模式与 BR/EDR 支持标志")
        appendLog("2. Service UUIDs (0x02/0x03/0x06/0x07): 16/32/128-bit 服务 UUID 列表")
        appendLog("3. Local Name (0x08/0x09): 设备广播名称 (Shortened / Complete)")
        appendLog("4. TX Power Level (0x0A): 发射功率 (用于近场距离测算)")
        appendLog("5. Manufacturer Specific Data (0xFF): 厂商自定义数据 (包含 Vendor ID + 负载，iBeacon 即存于此)")
        appendLog("✓ ScanRecord.getManufacturerSpecificData(id) 可直接提取自定义私有协议数据")
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
