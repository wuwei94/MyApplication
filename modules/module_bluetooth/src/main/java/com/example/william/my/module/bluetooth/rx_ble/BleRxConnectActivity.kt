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
 * RxAndroidBle 响应式连接与流控 — establishConnection
 *
 * 用 Rx 链式编排连接 → MTU → Notify → 写入；dispose 时自动断开并注销订阅。
 *
 * 库选型定位（四栈横评中的 RxAndroidBle）：
 * - 编程模型：连接、读写、通知全部建模为 Observable，可用 flatMap / switchMap 等操作符编排
 * - 生命周期：dispose 时自动断开连接并注销通知，降低泄漏风险
 * - 适合：项目本身重度使用 RxJava，或需要对连续传感器数据做复杂流控的场景
 *
 * 核心机制与避坑点：
 * 1. 连接流：`establishConnection` → `Observable<RxBleConnection>`
 * 2. 操作符编排：flatMap 串联协商与读写
 * 3. 通知流：Notification 特征值作为 Observable
 * 4. 资源释放：dispose 一次完成断开与清理
 *
 * 基本用法：
 * ```kotlin
 * device.establishConnection(false)
 *     .flatMapSingle { it.discoverServices() }
 *     .flatMap { conn -> conn.setupNotification(charUuid) }
 *     .flatMap { it }
 *     .subscribe({ appendLog("Notify: ...") }, { appendLog(it.message) })
 * // 断开：disposable.dispose()
 * ```
 *
 * 适用场景：
 * - 响应式 BLE 业务层，连接与读写需与其它 Rx 流组合
 * - 多设备并发或需要 dispose 即释放的生命周期模型
 * - 与原生 / Nordic / FastBle 连接模型横向对比
 *
 * https://github.com/dariuszseweryn/RxAndroidBle
 */
@Route(path = RouterPath.Bluetooth.RxConnect)
class BleRxConnectActivity : BasicResponseActivity() {

    private lateinit var rxBleClient: RxBleClient
    private var rxBleDevice: RxBleDevice? = null
    private var connectionObservable: Observable<RxBleConnection>? = null
    private val disposables = CompositeDisposable()
    private var connectionDisposable: Disposable? = null

    private var targetCharUuid: UUID? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        rxBleClient = RxBleClient.create(applicationContext)

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

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
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

        val scanDisposable = rxBleClient.scanBleDevices(scanSettings, ScanFilter.empty())
            .take(1)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { scanResult ->
                    val device = scanResult.bleDevice
                    rxBleDevice = device
                    val name = device.name ?: "未知设备"
                    appendLog("找到设备: $name (${device.macAddress})，准备建立响应式连接...")
                    connect(device)
                },
                { e -> appendLog("✗ 扫描异常: ${e.message}") },
            )
        disposables.add(scanDisposable)
    }

    private fun connect(device: RxBleDevice) {
        disconnect()

        // establishConnection 返回一个共享的连接 Observable (replay(1).refCount())
        val connectionObservable = device.establishConnection(false)
            .replay(1)
            .refCount()

        this.connectionObservable = connectionObservable

        connectionDisposable = connectionObservable
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
                            targetCharUuid = c.uuid
                            appendLog("✓ 自动捕获目标特征值: $targetCharUuid")
                        }
                    }
                },
                { throwable ->
                    appendLog("✗ 连接或服务发现异常: ${throwable.message}")
                },
            )
    }

    private fun requestMtu() {
        val connObs = connectionObservable
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
        disposables.add(d)
    }

    private fun readCharacteristic() {
        val connObs = connectionObservable
        val charUuid = targetCharUuid
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
        disposables.add(d)
    }

    private fun writeCharacteristic() {
        val connObs = connectionObservable
        val charUuid = targetCharUuid
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
        disposables.add(d)
    }

    private fun setupNotification() {
        val connObs = connectionObservable
        val charUuid = targetCharUuid
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
        disposables.add(d)
    }

    private fun disconnect() {
        connectionDisposable?.dispose()
        connectionDisposable = null
        connectionObservable = null
        disposables.clear()
        appendLog("✓ 已释放连接与所有 RxJava 订阅")
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnect()
    }
}
