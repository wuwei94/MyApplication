package com.example.william.my.module.bluetooth.nordic_ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import no.nordicsemi.android.ble.BleManager
import java.util.UUID

/**
 * Nordic BLE 工业级 Manager 封装
 *
 * Nordic Android-BLE-Library 核心设计模式（2.6+ 新版标准）：
 * 1. 继承 [BleManager] 统一管理特定设备或通用外设的 GATT 连接生命周期。
 * 2. 直接在 [BleManager] 子类中重写 [isRequiredServiceSupported]、[initialize] 和 [onServicesInvalidated]，
 *    无需再通过内部匿名类 [BleManagerGattCallback] 实现。
 * 3. 在 [initialize] 中按顺序定义连接建立后的初始化管道，由 Nordic 底层保证原子性与串行执行。
 */
class NordicBleManager(context: Context) : BleManager(context) {

    companion object {
        // 标准测试 UUID 或通用自定义 UUID
        val SERVICE_UUID: UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")
        val CHAR_UUID: UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")
    }

    var targetCharacteristic: BluetoothGattCharacteristic? = null
        private set

    var onLogListener: ((String) -> Unit)? = null
    var onDataReceivedListener: ((String) -> Unit)? = null

    fun read(characteristic: BluetoothGattCharacteristic) = readCharacteristic(characteristic)
    fun write(
        characteristic: BluetoothGattCharacteristic,
        data: ByteArray,
        writeType: Int = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
    ) = writeCharacteristic(characteristic, data, writeType)

    private fun log(message: String) {
        onLogListener?.invoke("[Nordic Log] $message")
    }

    override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
        // 遍历所有服务，尝试捕获可用的特征值
        for (service in gatt.services) {
            for (char in service.characteristics) {
                if (targetCharacteristic == null) {
                    targetCharacteristic = char
                }
            }
        }
        // 返回 true 表示设备服务满足要求，继续执行 initialize 流程
        return true
    }

    override fun initialize() {
        // 工业级流水线初始化操作：Nordic 会自动将以下步骤排入队列串行执行
        requestMtu(512)
            .with { _, mtu -> log("MTU 协商成功: $mtu") }
            .enqueue()

        targetCharacteristic?.let { char ->
            setNotificationCallback(char)
                .with { _, data ->
                    val hex = data.value?.joinToString(" ") { String.format("%02X", it) } ?: ""
                    val text = data.getStringValue(0) ?: ""
                    onDataReceivedListener?.invoke("Hex: $hex | Text: $text")
                }

            enableNotifications(char)
                .done { log("✓ 特征值 Notification 成功使能") }
                .fail { _, status -> log("✗ 特征值 Notification 使能失败, status=$status") }
                .enqueue()
        }
    }

    override fun onServicesInvalidated() {
        targetCharacteristic = null
    }
}
