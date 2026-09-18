package com.example.william.my.module.bluetooth.nordic_ble.activity

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.bluetooth.nordic_ble.data.NordicBleManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import no.nordicsemi.android.ble.ktx.suspend
import no.nordicsemi.android.ble.observer.ConnectionObserver

/**
 * Nordic BLE 连接与挂起调用 — BleManager + suspend
 *
 * 基于 NordicBleManager 演示连接生命周期、链式重试与协程 suspend API，与原生 / FastBle / Rx 连接栈平行对照。
 *
 * 核心机制与避坑点：
 * 1. Manager 管道：[NordicBleManager] 统一 GATT 请求队列，多任务并发由库内串行化
 * 2. 链式策略：connect(...).timeout(...).retry(...).enqueue() 可配置超时与重连次数
 * 3. 断开原因：[ConnectionObserver] 区分主动断开、连接超时、链路丢失等状态，便于重连决策
 * 4. 挂起扩展：`suspend` 将 enqueue 回调转为同步风格，需在 Dispatchers.IO 上调用避免阻塞主线程
 * 5. 资源释放：连接建立与断开均走 Manager 生命周期，页面销毁前应完成 disconnect
 *
 * 官方参考：
 * https://github.com/NordicSemiconductor/Android-BLE-Library
 */
@SuppressLint("MissingPermission")
@Route(path = RouterPath.Bluetooth.NordicConnect)
class BleNordicConnectActivity : BasicResponseActivity() {

    private var bluetoothAdapter: BluetoothAdapter? = null
    private lateinit var bleManager: NordicBleManager
    private var targetDevice: BluetoothDevice? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        bluetoothAdapter = bluetoothManager?.adapter

        bleManager = NordicBleManager(this).apply {
            onLogListener = { logMsg -> appendLog(logMsg) }
            onDataReceivedListener = { dataMsg -> appendLog("🔔 [Nordic Notify] ") }
            setConnectionObserver(object : ConnectionObserver {
                override fun onDeviceConnecting(device: BluetoothDevice) {
                    appendLog("⏳ 正在连接设备: ...")
                }

                override fun onDeviceConnected(device: BluetoothDevice) {
                    appendLog("✓ 设备已连接 (Connected): ")
                }

                override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
                    appendLog("✗ 连接失败 (reason=)")
                }

                override fun onDeviceReady(device: BluetoothDevice) {
                    appendLog("🚀 设备已就绪 (Ready)! 服务已发现、MTU与Notification初始化已自动完成")
                }

                override fun onDeviceDisconnecting(device: BluetoothDevice) {
                    appendLog("⏳ 正在断开连接: ...")
                }

                override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
                    appendLog("✓ 设备已断开 (Disconnected, reason=)")
                }
            })
        }

        showDescription(
            "Nordic BLE 工业级连接与挂起调用示例\n\n" +
                "覆盖连接状态、suspend 读写、Notify 订阅与资源释放\n" +
                "initialize 管道在连接就绪时自动协商 MTU 并使能 Notification\n" +
                "请按顺序点击下方操作项",
        )
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 扫描并使用 Nordic BleManager 连接首个设备 (带3次重试)",
        "2. 挂起读取特征值 (suspend 协程调用)",
        "3. 挂起写入数据 (suspend 协程调用)",
        "4. 使能 Notify 通知订阅 (enableNotifications)",
        "5. 断开连接并清理资源",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> scanAndConnectNordic()
            1 -> readCharacteristicSuspend()
            2 -> writeCharacteristicSuspend()
            3 -> enableNotificationSuspend()
            4 -> disconnectNordic()
        }
    }

    private fun scanAndConnectNordic() {
        val scanner = bluetoothAdapter?.bluetoothLeScanner
        if (scanner == null) {
            appendLog("✗ 无法获取 BLE 扫描器，请确认蓝牙已开启")
            return
        }

        appendLog("正在临时扫描 3 秒以寻找周围首个 BLE 设备...")
        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                result?.device?.let { device ->
                    try {
                        scanner.stopScan(this)
                    } catch (_: Exception) {
                        // 保护性调用：忽略停止已结束扫描时的底层平台异常
                    }
                    targetDevice = device
                    val name = device.name ?: "未知设备"
                    appendLog("找到设备: $name (${device.address})，使用 Nordic BleManager 发起连接...")

                    // Nordic 工业级连接调用链：设置重试次数、重试延迟、超时时间
                    bleManager.connect(device)
                        .retry(3, 200)
                        .timeout(10000)
                        .useAutoConnect(false)
                        .enqueue()
                }
            }
        }
        try {
            scanner.startScan(scanCallback)
        } catch (e: Exception) {
            appendLog("✗ 扫描启动失败: ${e.message}")
        }
    }

    private fun readCharacteristicSuspend() {
        val char = bleManager.targetCharacteristic
        if (char == null || !bleManager.isConnected) {
            appendLog("✗ 设备未连接或无可用特征值")
            return
        }

        lifecycleScope.launch {
            try {
                appendLog("▶ 正在通过 suspend 挂起函数读取特征值...")
                // 使用 Nordic 提供的 suspend 扩展函数
                val data = withContext(Dispatchers.IO) {
                    bleManager.read(char).suspend()
                }
                val hex = data.value?.joinToString(" ") { String.format("%02X", it) } ?: ""
                val text = data.getStringValue(0) ?: ""
                appendLog("✓ [suspend 读取成功] Hex: $hex | Text: $text")
            } catch (e: Exception) {
                appendLog("✗ [suspend 读取失败] 异常: ${e.message}")
            }
        }
    }

    private fun writeCharacteristicSuspend() {
        val char = bleManager.targetCharacteristic
        if (char == null || !bleManager.isConnected) {
            appendLog("✗ 设备未连接或无可用特征值")
            return
        }

        lifecycleScope.launch {
            try {
                val sendText = "Hello from Nordic BLE Coroutines!"
                appendLog("▶ 正在通过 suspend 挂起函数写入数据: \"$sendText\"...")
                val sendData = sendText.toByteArray(Charsets.UTF_8)
                withContext(Dispatchers.IO) {
                    bleManager.write(char, sendData).suspend()
                }
                appendLog("✓ [suspend 写入成功] 数据已由 Nordic 队列安全发送并收到确认")
            } catch (e: Exception) {
                appendLog("✗ [suspend 写入失败] 异常: ${e.message}")
            }
        }
    }

    /**
     * 对照 initialize 管道中的自动 Notify，手动再走一遍 enableNotifications 请求。
     */
    private fun enableNotificationSuspend() {
        val char = bleManager.targetCharacteristic
        if (char == null || !bleManager.isConnected) {
            appendLog("✗ 设备未连接或无可用特征值")
            return
        }
        appendLog("→ [Notify] 请求使能特征值通知订阅...")
        bleManager.enableNotify(char)
            .done { appendLog("✓ [Notify] 通知订阅已使能，下行数据经 Notification 回调") }
            .fail { _, status -> appendLog("✗ [Notify] 使能失败 status=$status") }
            .enqueue()
    }

    private fun disconnectNordic() {
        if (bleManager.isConnected) {
            appendLog("正在断开 Nordic BLE 连接...")
            bleManager.disconnect().enqueue()
        } else {
            appendLog("当前未处于连接状态")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bleManager.close()
    }
}
