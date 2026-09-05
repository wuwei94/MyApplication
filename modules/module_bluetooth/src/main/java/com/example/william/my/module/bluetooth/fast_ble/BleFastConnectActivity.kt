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
 * FastBle 连接与读写回调示例
 *
 * 【FastBle —— 极简全能型（代步车）】
 * - 功能覆盖：全都有（扫描、连接、读写、Notify、MTU、基础重连）。
 * - 特点：API 最傻瓜、最直接。原生 Android 需要先找 Service 对象，再找 Characteristic 对象，写一堆回调；FastBle 直接传字符串就能读写：read(mac, serviceUUID, charUUID, callback)。
 * - 适合谁：新手入门、中小型项目、业务逻辑简单的蓝牙设备。
 *
 * 演示特性：
 * 1. [BleManager.getInstance().connect] 连接与状态监听
 * 2. [BleManager.getInstance().setMtu] MTU 设置
 * 3. [BleManager.getInstance().read] / [write] 简化读写（无需手动寻找 Characteristic 实例，直接传 UUID 字符串）
 * 4. [BleManager.getInstance().notify] 开启通知
 */
@Route(path = RouterPath.Bluetooth.FastConnect)
class BleFastConnectActivity : BasicResponseActivity() {

    private var mConnectedDevice: BleDevice? = null
    private var mTargetServiceUuid: String? = null
    private var mTargetCharUuid: String? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        BleManager.getInstance().init(application)

        showDescription(
            "FastBle 连接与读写回调示例\n\n" +
                "演示 FastBle 极简的 UUID 驱动读写与 BleGattCallback / BleNotifyCallback\n" +
                "请按顺序点击下方操作项",
        )
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 扫描并使用 FastBle 连接首个设备",
        "2. 设置 MTU 为 512 字节 (setMtu)",
        "3. 读取特征值 (BleReadCallback)",
        "4. 写入测试数据 (BleWriteCallback)",
        "5. 开启 Notify 通知监听 (BleNotifyCallback)",
        "6. 断开连接 (disconnect)",
    )

    override fun onRecyclerClick(position: Int, text: String) {
        when (position) {
            0 -> scanAndConnect()
            1 -> setFastBleMtu()
            2 -> readCharacteristic()
            3 -> writeCharacteristic()
            4 -> enableNotification()
            5 -> disconnect()
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
                if (mConnectedDevice == null) {
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
                    mConnectedDevice = bleDevice
                    appendLog("✓ FastBle 连接成功: ${bleDevice?.mac}")

                    // 自动提取首个可用服务与特征 UUID
                    gatt?.services?.firstOrNull()?.let { s ->
                        mTargetServiceUuid = s.uuid.toString()
                        s.characteristics.firstOrNull()?.let { c ->
                            mTargetCharUuid = c.uuid.toString()
                        }
                    }
                    appendLog("✓ 自动捕获目标 Service: $mTargetServiceUuid, Char: $mTargetCharUuid")
                }

                override fun onDisConnected(isActiveDisConnected: Boolean, device: BleDevice?, gatt: BluetoothGatt?, status: Int) {
                    mConnectedDevice = null
                    appendLog("✓ FastBle 设备已断开 (主动断开=$isActiveDisConnected)")
                }
            },
        )
    }

    private fun setFastBleMtu() {
        val dev = mConnectedDevice
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
        val dev = mConnectedDevice
        val sUuid = mTargetServiceUuid
        val cUuid = mTargetCharUuid
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
        val dev = mConnectedDevice
        val sUuid = mTargetServiceUuid
        val cUuid = mTargetCharUuid
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
        val dev = mConnectedDevice
        val sUuid = mTargetServiceUuid
        val cUuid = mTargetCharUuid
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

    private fun disconnect() {
        mConnectedDevice?.let { dev ->
            BleManager.getInstance().disconnect(dev)
            appendLog("✓ 已发送断开连接指令")
        } ?: run {
            appendLog("当前未连接任何设备")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mConnectedDevice?.let { BleManager.getInstance().disconnect(it) }
    }
}
