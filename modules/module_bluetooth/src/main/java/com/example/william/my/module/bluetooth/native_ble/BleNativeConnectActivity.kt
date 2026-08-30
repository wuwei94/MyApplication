package com.example.william.my.module.bluetooth.native_ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Build
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import java.util.UUID

/**
 * 原生 BLE 连接与 GATT 交互示例
 *
 * 演示 Android 原生 BluetoothGatt 的全生命周期与数据读写机制：
 * 1. 设备连接 (connectGatt / autoConnect / 状态机监听)
 * 2. 服务发现 (discoverServices / GATT 树结构解析)
 * 3. MTU 协商 (requestMtu / 突破 23 字节上限)
 * 4. 特征值读取 (readCharacteristic)
 * 5. 特征值写入 (writeCharacteristic / WRITE_TYPE_DEFAULT vs NO_RESPONSE)
 * 6. Notify/Indicate 订阅 (setCharacteristicNotification + CCCD Descriptor 写入)
 * 7. 资源释放与断开连接
 */
@SuppressLint("MissingPermission")
@Route(path = RouterPath.Bluetooth.NativeConnect)
class BleNativeConnectActivity : BasicResponseActivity() {

    companion object {
        // 标准客户端特性配置描述符 (Client Characteristic Configuration Descriptor, CCCD)
        private val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }

    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mBluetoothGatt: BluetoothGatt? = null
    private var mTargetDevice: BluetoothDevice? = null

    private var mReadableChar: BluetoothGattCharacteristic? = null
    private var mWritableChar: BluetoothGattCharacteristic? = null
    private var mNotifiableChar: BluetoothGattCharacteristic? = null

    private val mGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            val deviceAddress = gatt?.device?.address ?: "Unknown"
            if (status == BluetoothGatt.GATT_SUCCESS) {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        appendLog("✓ 已连接到设备: ，准备发现服务...")
                        // 连接成功后，官方推荐调用 discoverServices()
                        gatt?.discoverServices()
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        appendLog("✓ 设备已断开连接: ")
                        mReadableChar = null
                        mWritableChar = null
                        mNotifiableChar = null
                    }
                }
            } else {
                appendLog("✗ 连接状态异常 (status=, newState=)")
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS && gatt != null) {
                appendLog("✓ 成功发现 GATT 服务列表 ( 个服务):")
                for (service in gatt.services) {
                    val sUuid = service.uuid.toString()
                    val sType = if (service.type == BluetoothGattService.SERVICE_TYPE_PRIMARY) "Primary" else "Secondary"
                    appendLog("  ├─ Service:  ()")

                    for (char in service.characteristics) {
                        val cUuid = char.uuid.toString()
                        val props = parseProperties(char.properties)
                        appendLog("  │   ├─ Char:  | 属性: []")

                        // 自动记录首个可读/可写/可Notify的特征，方便快速演示
                        if (mReadableChar == null && (char.properties and BluetoothGattCharacteristic.PROPERTY_READ) != 0) {
                            mReadableChar = char
                        }
                        if (mWritableChar == null && (char.properties and (BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) != 0) {
                            mWritableChar = char
                        }
                        if (mNotifiableChar == null && (char.properties and (BluetoothGattCharacteristic.PROPERTY_NOTIFY or BluetoothGattCharacteristic.PROPERTY_INDICATE)) != 0) {
                            mNotifiableChar = char
                        }
                    }
                }
                appendLog("✓ 已自动捕获可交互的特征值引用")
            } else {
                appendLog("✗ 发现服务失败 (status=)")
            }
        }

        // Android 13- 回调
        @Deprecated("Deprecated in Java")
        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS && characteristic != null) {
                val data = characteristic.value ?: byteArrayOf()
                val hexStr = bytesToHex(data)
                val utf8Str = String(data)
                appendLog("✓ 读取特征值成功 [...]: Hex= | String=")
            } else {
                appendLog("✗ 读取特征值失败 (status=)")
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                appendLog("✓ 写入特征值成功 [...]")
            } else {
                appendLog("✗ 写入特征值失败 (status=)")
            }
        }

        // Android 13- 回调
        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            characteristic ?: return
            val data = characteristic.value ?: byteArrayOf()
            val hexStr = bytesToHex(data)
            appendLog("🔔 收到 Notify/Indicate 推送: Hex= (长度:  字节)")
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                appendLog("✓ MTU 协商成功，当前单包最大有效负载 payload =  字节 (MTU: )")
            } else {
                appendLog("✗ MTU 协商失败 (status=)")
            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                appendLog("✓ 描述符 CCCD 写入成功，通知监听已正式生效！")
            } else {
                appendLog("✗ 描述符 CCCD 写入失败 (status=)")
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        mBluetoothAdapter = bluetoothManager?.adapter

        showDescription(
            "Android 原生 BLE 连接与 GATT 交互示例\n\n" +
                    "演示 connectGatt、服务发现、MTU 扩展、读写交互与 CCCD Notify 监听\n" +
                    "请按顺序点击下方操作项"
        )
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "1. 扫描并自动连接发现的第一个 BLE 设备",
            "2. 手动发现 GATT 服务树 (discoverServices)",
            "3. 请求扩展 MTU 至 512 字节 (requestMtu)",
            "4. 读取首个可读特征值 (readCharacteristic)",
            "5. 写入测试文本到首个可写特征值 (writeCharacteristic)",
            "6. 开启首个特征值 Notify 通知监听 (CCCD 写入)",
            "7. 断开并释放 GATT 连接 (disconnect & close)"
        )
    }

    override fun onRecyclerClick(position: Int, text: String) {
        when (position) {
            0 -> scanAndConnectFirstDevice()
            1 -> discoverServices()
            2 -> requestMtu()
            3 -> readCharacteristic()
            4 -> writeCharacteristic()
            5 -> enableNotification()
            6 -> disconnectAndClose()
        }
    }

    private fun scanAndConnectFirstDevice() {
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
                    appendLog("找到设备:  ()，发起连接...")
                    connectDevice(device)
                }
            }
        }
        try {
            scanner.startScan(scanCallback)
        } catch (e: Exception) {
            appendLog("✗ 扫描启动失败: ")
        }
    }

    private fun connectDevice(device: BluetoothDevice) {
        disconnectAndClose()
        appendLog("正在建立 GATT 连接 (autoConnect = false)...")
        mBluetoothGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            device.connectGatt(this, false, mGattCallback, BluetoothDevice.TRANSPORT_LE)
        } else {
            device.connectGatt(this, false, mGattCallback)
        }
    }

    private fun discoverServices() {
        val gatt = mBluetoothGatt
        if (gatt == null) {
            appendLog("✗ GATT 尚未连接")
            return
        }
        appendLog("正在发起服务发现 (discoverServices)...")
        gatt.discoverServices()
    }

    private fun requestMtu() {
        val gatt = mBluetoothGatt
        if (gatt == null) {
            appendLog("✗ GATT 尚未连接")
            return
        }
        appendLog("正在请求将 MTU 调整为 512 字节...")
        val success = gatt.requestMtu(512)
        appendLog("发起 requestMtu(512) 结果: ")
    }

    private fun readCharacteristic() {
        val gatt = mBluetoothGatt
        val char = mReadableChar
        if (gatt == null || char == null) {
            appendLog("✗ 无可用的可读特征值或未连接")
            return
        }
        appendLog("正在读取特征值: ...")
        gatt.readCharacteristic(char)
    }

    private fun writeCharacteristic() {
        val gatt = mBluetoothGatt
        val char = mWritableChar
        if (gatt == null || char == null) {
            appendLog("✗ 无可用的可写特征值或未连接")
            return
        }
        val sendBytes = "Hello BLE from Android Native!".toByteArray(Charsets.UTF_8)
        char.value = sendBytes
        char.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        appendLog("正在写入数据 ( 字节) 到特征值: ...")
        val success = gatt.writeCharacteristic(char)
        appendLog("发起 writeCharacteristic 结果: ")
    }

    private fun enableNotification() {
        val gatt = mBluetoothGatt
        val char = mNotifiableChar
        if (gatt == null || char == null) {
            appendLog("✗ 无可用的 Notify 特征值或未连接")
            return
        }

        // 步骤 1：在系统层注册通知
        val registered = gatt.setCharacteristicNotification(char, true)
        appendLog("本地 setCharacteristicNotification 状态: ")

        // 步骤 2：向物理从机写入 Client Characteristic Configuration Descriptor (CCCD, 0x2902)
        val descriptor = char.getDescriptor(CCCD_UUID)
        if (descriptor != null) {
            val isIndicate = (char.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0
            val descriptorValue = if (isIndicate) {
                BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
            } else {
                BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            }
            descriptor.value = descriptorValue
            val success = gatt.writeDescriptor(descriptor)
            appendLog("向 CCCD 描述符写入使能值 (Indicate=): ")
        } else {
            appendLog("⚠ 未在特征值中找到标准 CCCD (0x2902) 描述符")
        }
    }

    private fun disconnectAndClose() {
        mBluetoothGatt?.let { gatt ->
            try {
                gatt.disconnect()
                gatt.close()
            } catch (_: Exception) {}
            appendLog("✓ 已断开并释放 BluetoothGatt 资源")
        }
        mBluetoothGatt = null
        mReadableChar = null
        mWritableChar = null
        mNotifiableChar = null
    }

    private fun parseProperties(props: Int): String {
        val list = mutableListOf<String>()
        if ((props and BluetoothGattCharacteristic.PROPERTY_READ) != 0) list.add("Read")
        if ((props and BluetoothGattCharacteristic.PROPERTY_WRITE) != 0) list.add("Write")
        if ((props and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) list.add("WriteNoResp")
        if ((props and BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) list.add("Notify")
        if ((props and BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) list.add("Indicate")
        return list.joinToString("/")
    }

    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString(" ") { String.format("%02X", it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnectAndClose()
    }
}
