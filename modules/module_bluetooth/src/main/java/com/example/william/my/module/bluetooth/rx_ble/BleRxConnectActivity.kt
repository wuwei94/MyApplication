package com.example.william.my.module.bluetooth.rx_ble

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.polidea.rxandroidble3.RxBleClient
import com.polidea.rxandroidble3.RxBleConnection
import com.polidea.rxandroidble3.RxBleDevice
import com.polidea.rxandroidble3.scan.ScanFilter
import com.polidea.rxandroidble3.scan.ScanSettings
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import java.util.UUID

/**
 * RxAndroidBle 响应式连接与流控示例
 *
 * 【RxAndroidBle —— 响应式全能型（高铁网络）】
 * - 功能覆盖：全都有。
 * - 特点：把所有蓝牙操作全变成了 RxJava 的 Observable 数据流。
 *   • 扫描、连接、读写、数据流推送全可以用 RxJava 操作符（filter 过滤微弱信号、throttle 节流、combineLatest 多设备合并）。
 *   • 取消订阅（dispose()）时，连接自动断开、通知自动注销，不容易内存泄漏。
 * - 适合谁：项目本身重度使用 RxJava 架构，或者需要对连续传感器数据做复杂流控的场景。
 *
 * 演示特性：
 * 1. [RxBleDevice.establishConnection] 转换为 Observable<RxBleConnection>
 * 2. 链式 flatMap 编排：连接 ➔ 协商 MTU ➔ 订阅 Notification ➔ 写入特征
 * 3. 响应式优雅释放：调用 connection.dispose() 瞬间自动完成注销 Notify 与断开 GATT 连接
 */
@Route(path = RouterPath.Bluetooth.RxConnect)
class BleRxConnectActivity : BasicResponseActivity() {

    private lateinit var mRxBleClient: RxBleClient
    private var mRxBleDevice: RxBleDevice? = null
    private var mConnectionObservable: Observable<RxBleConnection>? = null
    private val mDisposables = CompositeDisposable()
    private var mConnectionDisposable: Disposable? = null

    private var mTargetCharUuid: UUID? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mRxBleClient = RxBleClient.create(applicationContext)

        showDescription(
            "RxAndroidBle 响应式连接与流控示例\n\n" +
                "演示 establishConnection 响应式连接管道与 flatMap 链式读写与 Notify\n" +
                "请按顺序点击下方操作项",
        )
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 扫描并响应式连接首个设备 (establishConnection)",
        "2. 链式协商 MTU 为 512 字节 (requestMtu)",
        "3. 读取特征值 (readCharacteristic)",
        "4. 写入测试数据 (writeCharacteristic)",
        "5. 开启响应式 Notify 数据流 (setupNotification)",
        "6. 断开连接并释放所有流 (dispose)",
    )

    override fun onRecyclerClick(position: Int, text: String) {
        when (position) {
            0 -> scanAndEstablishConnection()
            1 -> requestMtu()
            2 -> readCharacteristic()
            3 -> writeCharacteristic()
            4 -> setupNotification()
            5 -> disconnect()
        }
    }

    private fun scanAndEstablishConnection() {
        appendLog("正在临时扫描寻找首个 BLE 设备...")
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        val scanDisposable = mRxBleClient.scanBleDevices(scanSettings, ScanFilter.empty())
            .take(1)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { scanResult ->
                    val device = scanResult.bleDevice
                    mRxBleDevice = device
                    val name = device.name ?: "未知设备"
                    appendLog("找到设备: $name (${device.macAddress})，准备建立响应式连接...")
                    connect(device)
                },
                { e -> appendLog("✗ 扫描异常: ${e.message}") },
            )
        mDisposables.add(scanDisposable)
    }

    private fun connect(device: RxBleDevice) {
        disconnect()

        // establishConnection 返回一个共享的连接 Observable (replay(1).refCount())
        val connectionObservable = device.establishConnection(false)
            .replay(1)
            .refCount()

        mConnectionObservable = connectionObservable

        mConnectionDisposable = connectionObservable
            .observeOn(AndroidSchedulers.mainThread())
            .flatMapSingle { connection ->
                appendLog("✓ 已建立 GATT 连接，正在发现服务...")
                connection.discoverServices()
            }
            .subscribe(
                { rxBleDeviceServices ->
                    appendLog("✓ 成功发现 ${rxBleDeviceServices.bluetoothGattServices.size} 个服务")
                    rxBleDeviceServices.bluetoothGattServices.firstOrNull()?.let { s ->
                        s.characteristics.firstOrNull()?.let { c ->
                            mTargetCharUuid = c.uuid
                            appendLog("✓ 自动捕获目标特征值: $mTargetCharUuid")
                        }
                    }
                },
                { throwable ->
                    appendLog("✗ 连接或服务发现异常: ${throwable.message}")
                },
            )
    }

    private fun requestMtu() {
        val connObs = mConnectionObservable
        if (connObs == null) {
            appendLog("✗ 当前未处于连接状态")
            return
        }

        appendLog("正在通过响应式流请求协商 MTU 为 512 字节...")
        val d = connObs
            .firstOrError()
            .flatMap { connection -> connection.requestMtu(512) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { mtu -> appendLog("✓ [RxBle MTU] 协商成功，当前 MTU = $mtu 字节") },
                { e -> appendLog("✗ [RxBle MTU] 协商失败: ${e.message}") },
            )
        mDisposables.add(d)
    }

    private fun readCharacteristic() {
        val connObs = mConnectionObservable
        val charUuid = mTargetCharUuid
        if (connObs == null || charUuid == null) {
            appendLog("✗ 设备未连接或无目标特征 UUID")
            return
        }

        appendLog("正在响应式读取特征值: $charUuid...")
        val d = connObs
            .firstOrError()
            .flatMap { connection -> connection.readCharacteristic(charUuid) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { bytes ->
                    val hex = bytes.joinToString(" ") { String.format("%02X", it) }
                    val text = String(bytes)
                    appendLog("✓ [RxBle 读成功] Hex=[$hex] | Text=[$text]")
                },
                { e -> appendLog("✗ [RxBle 读失败] ${e.message}") },
            )
        mDisposables.add(d)
    }

    private fun writeCharacteristic() {
        val connObs = mConnectionObservable
        val charUuid = mTargetCharUuid
        if (connObs == null || charUuid == null) {
            appendLog("✗ 设备未连接或无目标特征 UUID")
            return
        }

        val sendBytes = "Hello from RxAndroidBle!".toByteArray(Charsets.UTF_8)
        appendLog("正在响应式写入数据 (${sendBytes.size} 字节)...")
        val d = connObs
            .firstOrError()
            .flatMap { connection -> connection.writeCharacteristic(charUuid, sendBytes) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { bytes ->
                    appendLog("✓ [RxBle 写成功] 已发送 ${bytes.size} 字节并收到底层确认")
                },
                { e -> appendLog("✗ [RxBle 写失败] ${e.message}") },
            )
        mDisposables.add(d)
    }

    private fun setupNotification() {
        val connObs = mConnectionObservable
        val charUuid = mTargetCharUuid
        if (connObs == null || charUuid == null) {
            appendLog("✗ 设备未连接或无目标特征 UUID")
            return
        }

        appendLog("正在开启响应式 Notify 数据流: $charUuid...")
        val d = connObs
            .firstOrError()
            .flatMapObservable { connection -> connection.setupNotification(charUuid) }
            .flatMap { notificationObservable -> notificationObservable }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { bytes ->
                    val hex = bytes.joinToString(" ") { String.format("%02X", it) }
                    appendLog("🔔 [RxBle 收到 Notify] Hex=[$hex]")
                },
                { e -> appendLog("✗ [RxBle Notify] 失败: ${e.message}") },
            )
        mDisposables.add(d)
    }

    private fun disconnect() {
        mConnectionDisposable?.dispose()
        mConnectionDisposable = null
        mConnectionObservable = null
        mDisposables.clear()
        appendLog("✓ 已释放连接与所有 RxJava 订阅")
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnect()
    }
}
