package com.example.william.my.module.bluetooth.nordic_ble

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import no.nordicsemi.android.ble.ktx.suspend
import no.nordicsemi.android.ble.observer.ConnectionObserver

/**
 * Nordic BLE 连接与挂起调用示例
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
 * 1. [NordicBleManager] 工业级生命周期管理
 * 2. 链式配置：超时控制 (timeout)、失败重试 (retry)、自动重连 (autoConnect)
 * 3. [ConnectionObserver] 连接状态与断开原因细粒度监听
 * 4. 协程挂起扩展 [suspend]：将异步回调转换为 Kotlin 挂起函数，直接同步风格获取结果
 */
@SuppressLint("MissingPermission")
@Route(path = RouterPath.Bluetooth.NordicConnect)
class BleNordicConnectActivity : BasicResponseActivity() {

    private var mBluetoothAdapter: BluetoothAdapter? = null
    private lateinit var mBleManager: NordicBleManager
    private var mTargetDevice: BluetoothDevice? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        mBluetoothAdapter = bluetoothManager?.adapter

        mBleManager = NordicBleManager(this).apply {
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
                "演示 BleManager 状态机、自动重试连接、管道化初始化与 Kotlin suspend 读写\n" +
                "请按顺序点击下方操作项",
        )
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 扫描并使用 Nordic BleManager 连接首个设备 (带3次重试)",
        "2. 挂起读取特征值 (suspend 协程调用)",
        "3. 挂起写入数据 (suspend 协程调用)",
        "4. 断开连接并清理资源",
    )

    override fun onRecyclerClick(position: Int, text: String) {
        when (position) {
            0 -> scanAndConnectNordic()
            1 -> readCharacteristicSuspend()
            2 -> writeCharacteristicSuspend()
            3 -> disconnectNordic()
        }
    }

    private fun scanAndConnectNordic() {
        val scanner = mBluetoothAdapter?.bluetoothLeScanner
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
                    } catch (_: Exception) {}
                    mTargetDevice = device
                    val name = device.name ?: "未知设备"
                    appendLog("找到设备:  ()，使用 Nordic BleManager 发起连接...")

                    // Nordic 工业级连接调用链：设置重试次数、重试延迟、超时时间
                    mBleManager.connect(device)
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
            appendLog("✗ 扫描启动失败: ")
        }
    }

    private fun readCharacteristicSuspend() {
        val char = mBleManager.targetCharacteristic
        if (char == null || !mBleManager.isConnected) {
            appendLog("✗ 设备未连接或无可用特征值")
            return
        }

        lifecycleScope.launch {
            try {
                appendLog("▶ 正在通过 suspend 挂起函数读取特征值...")
                // 使用 Nordic 提供的 suspend 扩展函数
                val data = withContext(Dispatchers.IO) {
                    mBleManager.read(char).suspend()
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
        val char = mBleManager.targetCharacteristic
        if (char == null || !mBleManager.isConnected) {
            appendLog("✗ 设备未连接或无可用特征值")
            return
        }

        lifecycleScope.launch {
            try {
                val sendText = "Hello from Nordic BLE Coroutines!"
                appendLog("▶ 正在通过 suspend 挂起函数写入数据: \"$sendText\"...")
                val sendData = sendText.toByteArray(Charsets.UTF_8)
                withContext(Dispatchers.IO) {
                    mBleManager.write(char, sendData).suspend()
                }
                appendLog("✓ [suspend 写入成功] 数据已由 Nordic 队列安全发送并收到确认")
            } catch (e: Exception) {
                appendLog("✗ [suspend 写入失败] 异常: ")
            }
        }
    }

    private fun disconnectNordic() {
        if (mBleManager.isConnected) {
            appendLog("正在断开 Nordic BLE 连接...")
            mBleManager.disconnect().enqueue()
        } else {
            appendLog("当前未处于连接状态")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mBleManager.close()
    }
}
