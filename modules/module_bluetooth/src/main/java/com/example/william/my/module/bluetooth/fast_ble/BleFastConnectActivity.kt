package com.example.william.my.module.bluetooth.fast_ble

import android.bluetooth.BluetoothGatt
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.callback.BleMtuChangedCallback
import com.clj.fastble.callback.BleNotifyCallback
import com.clj.fastble.callback.BleReadCallback
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.callback.BleWriteCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * FastBle 连接与读写回调 — 字符串 UUID 直调
 *
 * 连接、MTU、read/write/notify 均以 serviceUUID + charUUID 字符串直调，省去手动查找 Characteristic，与原生 / Nordic / Rx 连接栈平行对照。
 *
 * 核心机制与避坑点：
 * 1. 连接：`BleManager.connect` + BleGattCallback 状态回调，连接成功后才可读写
 * 2. MTU：`setMtu` 需在大包传输前协商，默认 23 字节有效载荷仅 20 字节
 * 3. 读写：`read` / `write` 以字符串 UUID 定位特征值，写失败依赖 onWriteFailure 回调感知
 * 4. 通知：`notify` 订阅特征值后在 BleNotifyCallback 收流，断开连接时应停止订阅
 * 5. 全局初始化：`BleManager.getInstance().init(application)` 与 reConnectCount / operateTimeout 可在页面入口统一配置
 *
 * 官方参考：
 * https://github.com/Jasonchenlijian/FastBle
 */
@Route(path = RouterPath.Bluetooth.FastConnect)
class BleFastConnectActivity : BasicResponseActivity() {

    private var connectedDevice: BleDevice? = null
    private var targetServiceUuid: String? = null
    private var targetCharUuid: String? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        BleManager.getInstance().init(application)

        showDescription(
            "FastBle 连接与读写回调示例\n\n" +
                "覆盖扫描连接、MTU、读写 Notify、大包写入与断开释放\n" +
                "请按顺序点击下方操作项",
        )
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 扫描并使用 FastBle 连接首个设备",
        "2. 设置 MTU 为 512 字节 (setMtu)",
        "3. 读取特征值 (BleReadCallback)",
        "4. 写入测试数据 (BleWriteCallback)",
        "5. 开启 Notify 通知监听 (BleNotifyCallback)",
        "6. 发送大包数据 (write 分包 current/total)",
        "7. 断开连接 (disconnect)",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> scanAndConnect()
            1 -> setFastBleMtu()
            2 -> readCharacteristic()
            3 -> writeCharacteristic()
            4 -> enableNotification()
            5 -> writeLargePayload()
            6 -> disconnect()
        }
    }

    private fun scanAndConnect() {
        appendLog("正在临时扫描 4 秒以寻找首个 BLE 设备...")
        BleManager.getInstance().scan(object : BleScanCallback() {
            override fun onScanStarted(success: Boolean) {}

            override fun onScanning(bleDevice: BleDevice?) {
                bleDevice ?: return
                BleManager.getInstance().cancelScan()
                appendLog("找到设备: ${bleDevice.name} (${bleDevice.mac})，发起 FastBle 连接...")
                connectDevice(bleDevice)
            }

            override fun onScanFinished(scanResultList: MutableList<BleDevice>?) {
                if (connectedDevice == null) {
                    appendLog("未扫描到可用设备")
                }
            }
        })
    }

    private fun connectDevice(bleDevice: BleDevice) {
        BleManager.getInstance().connect(
            bleDevice,
            object : BleGattCallback() {
                override fun onStartConnect() {
                    appendLog("⏳ FastBle 正在连接...")
                }

                override fun onConnectFail(bleDevice: BleDevice?, exception: BleException?) {
                    appendLog("✗ FastBle 连接失败: ${exception?.description}")
                }

                override fun onConnectSuccess(bleDevice: BleDevice?, gatt: BluetoothGatt?, status: Int) {
                    connectedDevice = bleDevice
                    appendLog("✓ FastBle 连接成功: ${bleDevice?.mac}")

                    // 自动提取首个可用服务与特征 UUID
                    gatt?.services?.firstOrNull()?.let { s ->
                        targetServiceUuid = s.uuid.toString()
                        s.characteristics.firstOrNull()?.let { c ->
                            targetCharUuid = c.uuid.toString()
                        }
                    }
                    appendLog("✓ 自动捕获目标 Service: $targetServiceUuid, Char: $targetCharUuid")
                }

                override fun onDisConnected(isActiveDisConnected: Boolean, device: BleDevice?, gatt: BluetoothGatt?, status: Int) {
                    connectedDevice = null
                    appendLog("✓ FastBle 设备已断开 (主动断开=$isActiveDisConnected)")
                }
            },
        )
    }

    private fun setFastBleMtu() {
        val dev = connectedDevice
        if (dev == null) {
            appendLog("✗ 设备尚未连接")
            return
        }
        appendLog("正在请求设置 MTU 为 512 字节...")
        BleManager.getInstance().setMtu(
            dev,
            512,
            object : BleMtuChangedCallback() {
                override fun onSetMTUFailure(exception: BleException?) {
                    appendLog("✗ MTU 设置失败: ${exception?.description}")
                }

                override fun onMtuChanged(mtu: Int) {
                    appendLog("✓ MTU 调整成功: 当前 MTU = $mtu 字节")
                }
            },
        )
    }

    private fun readCharacteristic() {
        val dev = connectedDevice
        val sUuid = targetServiceUuid
        val cUuid = targetCharUuid
        if (dev == null || sUuid == null || cUuid == null) {
            appendLog("✗ 设备未连接或无可用特征 UUID")
            return
        }

        appendLog("正在读取特征值: $cUuid...")
        BleManager.getInstance().read(
            dev,
            sUuid,
            cUuid,
            object : BleReadCallback() {
                override fun onReadSuccess(data: ByteArray?) {
                    val hex = data?.joinToString(" ") { String.format("%02X", it) } ?: ""
                    val text = if (data != null) String(data) else ""
                    appendLog("✓ [FastBle 读成功] Hex=[$hex] | Text=[$text]")
                }

                override fun onReadFailure(exception: BleException?) {
                    appendLog("✗ [FastBle 读失败] ${exception?.description}")
                }
            },
        )
    }

    private fun writeCharacteristic() {
        val dev = connectedDevice
        val sUuid = targetServiceUuid
        val cUuid = targetCharUuid
        if (dev == null || sUuid == null || cUuid == null) {
            appendLog("✗ 设备未连接或无可用特征 UUID")
            return
        }

        val sendBytes = "Hello FastBle!".toByteArray(Charsets.UTF_8)
        appendLog("正在写入数据 (${sendBytes.size} 字节)...")
        BleManager.getInstance().write(
            dev,
            sUuid,
            cUuid,
            sendBytes,
            object : BleWriteCallback() {
                override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                    appendLog("✓ [FastBle 写成功] 进度: [$current/$total]")
                }

                override fun onWriteFailure(exception: BleException?) {
                    appendLog("✗ [FastBle 写失败] ${exception?.description}")
                }
            },
        )
    }

    private fun enableNotification() {
        val dev = connectedDevice
        val sUuid = targetServiceUuid
        val cUuid = targetCharUuid
        if (dev == null || sUuid == null || cUuid == null) {
            appendLog("✗ 设备未连接或无可用特征 UUID")
            return
        }

        appendLog("正在开启 Notify 通知: $cUuid...")
        BleManager.getInstance().notify(
            dev,
            sUuid,
            cUuid,
            object : BleNotifyCallback() {
                override fun onNotifySuccess() {
                    appendLog("✓ [FastBle Notify] 通知监听已成功使能")
                }

                override fun onNotifyFailure(exception: BleException?) {
                    appendLog("✗ [FastBle Notify] 通知监听使能失败: ${exception?.description}")
                }

                override fun onCharacteristicChanged(data: ByteArray?) {
                    val hex = data?.joinToString(" ") { String.format("%02X", it) } ?: ""
                    appendLog("🔔 [FastBle 收到 Notify] Hex=[$hex]")
                }
            },
        )
    }

    /**
     * 写入超过单包有效载荷的数据，观察 FastBle 回调中的分包进度。
     */
    private fun writeLargePayload() {
        val dev = connectedDevice
        val sUuid = targetServiceUuid
        val cUuid = targetCharUuid
        if (dev == null || sUuid == null || cUuid == null) {
            appendLog("✗ 设备未连接或无可用特征 UUID")
            return
        }

        val sendBytes = ByteArray(128) { index -> ('A' + (index % 26)).code.toByte() }
        appendLog("→ [大包写入] 发送 ${sendBytes.size} 字节，观察 current/total 分包进度...")
        BleManager.getInstance().write(
            dev,
            sUuid,
            cUuid,
            sendBytes,
            object : BleWriteCallback() {
                override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                    appendLog("✓ [大包写入] 进度: [$current/$total]")
                }

                override fun onWriteFailure(exception: BleException?) {
                    appendLog("✗ [大包写入] ${exception?.description}")
                }
            },
        )
    }

    private fun disconnect() {
        connectedDevice?.let { dev ->
            BleManager.getInstance().disconnect(dev)
            appendLog("✓ 已发送断开连接指令")
        } ?: run {
            appendLog("当前未连接任何设备")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        connectedDevice?.let { BleManager.getInstance().disconnect(it) }
    }
}
