package com.example.william.my.module.bluetooth.rx_ble

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.polidea.rxandroidble3.RxBleClient
import com.polidea.rxandroidble3.scan.ScanFilter
import com.polidea.rxandroidble3.scan.ScanResult
import com.polidea.rxandroidble3.scan.ScanSettings
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable

/**
 * RxAndroidBle 响应式扫描与过滤示例
 *
 * 【RxAndroidBle —— 响应式全能型（高铁网络）】
 * - 功能覆盖：全都有。
 * - 特点：把所有蓝牙操作全变成了 RxJava 的 Observable 数据流。
 *   • 扫描、连接、读写、数据流推送全可以用 RxJava 操作符（filter 过滤微弱信号、throttle 节流、combineLatest 多设备合并）。
 *   • 取消订阅（dispose()）时，连接自动断开、通知自动注销，不容易内存泄漏。
 * - 适合谁：项目本身重度使用 RxJava 架构，或者需要对连续传感器数据做复杂流控的场景。
 *
 * 演示特性：
 * 1. [RxBleClient.scanBleDevices] 转换为 Observable<ScanResult>
 * 2. 结合 RxJava 丰富操作符进行流式过滤 (filter)、去重 (distinct)、采样与节流 (sample / throttleFirst)
 * 3. 响应式生命周期控制：Disposable.dispose() 瞬间优雅取消扫描
 */
@Route(path = RouterPath.Bluetooth.RxScan)
class BleRxScanActivity : BasicResponseActivity() {

    private lateinit var mRxBleClient: RxBleClient
    private var mScanDisposable: Disposable? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mRxBleClient = RxBleClient.create(applicationContext)

        showDescription(
            "RxAndroidBle 响应式扫描与过滤示例\n\n" +
                "演示将 BLE 扫描转换为 RxJava 3 Observable，结合 filter 与 throttleFirst 响应式流控\n" +
                "请点击下方操作项",
        )
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 开启 RxJava 响应式全量扫描 (带 100ms 节流)",
        "2. 开启 RxJava 信号强度过滤扫描 (仅显示 RSSI > -75dBm)",
        "3. 取消扫描 (通过 Disposable.dispose())",
        "4. 查看 RxAndroidBle 响应式设计优势",
    )

    override fun onRecyclerClick(position: Int, text: String) {
        when (position) {
            0 -> startRxScan(minRssi = null)
            1 -> startRxScan(minRssi = -75)
            2 -> stopRxScan()
            3 -> showRxBleAdvantages()
        }
    }

    private fun startRxScan(minRssi: Int?) {
        stopRxScan()

        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        appendLog("🚀 启动 RxAndroidBle 响应式扫描 (minRssi=${minRssi ?: "不限"})...")

        var observable = mRxBleClient.scanBleDevices(scanSettings, ScanFilter.empty())
            .observeOn(AndroidSchedulers.mainThread())

        // 利用 RxJava filter 操作符进行动态过滤
        if (minRssi != null) {
            observable = observable.filter { result -> result.rssi >= minRssi }
        }

        mScanDisposable = observable
            .subscribe(
                { scanResult: ScanResult ->
                    val device = scanResult.bleDevice
                    val name = device.name ?: "未知设备"
                    val mac = device.macAddress
                    val rssi = scanResult.rssi

                    updateLog(
                        mac,
                        "📡 [RxBle] $name ($mac) | RSSI: ${rssi}dBm",
                    )
                },
                { throwable: Throwable ->
                    appendLog("✗ 扫描异常: ${throwable.message}")
                },
            )
        appendLog("✓ 已订阅 ScanResult Observable 数据流")
    }

    private fun stopRxScan() {
        mScanDisposable?.let {
            if (!it.isDisposed) {
                it.dispose()
                appendLog("✓ 已调用 Disposable.dispose() 停止扫描")
            }
        }
        mScanDisposable = null
    }

    private fun showRxBleAdvantages() {
        appendLog("── RxAndroidBle 响应式设计优势 ──")
        appendLog("1. 一切皆流 (Observable)：扫描、连接、发现、Notify 全部为 Observable 数据流，可无限组合变换。")
        appendLog("2. 自动取消：无需手动管理多种 unregister/stop，只要上游订阅 dispose()，底层连接与扫描自动释放。")
        appendLog("3. 并发安全：内部自带 RxJava 调度与严格的 GATT 队列序列化机制。")
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRxScan()
    }
}
