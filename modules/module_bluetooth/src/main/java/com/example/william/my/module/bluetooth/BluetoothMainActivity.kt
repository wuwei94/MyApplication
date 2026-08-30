package com.example.william.my.module.bluetooth

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * 蓝牙示例入口（BLE 客户端为主）
 *
 * 演示 Android 低功耗蓝牙（Bluetooth Low Energy, BLE）开发中的核心技术链路：
 * 设备扫描与过滤、GATT 服务发现、特征值读写、Notify 订阅、MTU 协商与大包分包传输。
 *
 * 提供两种方案对比：
 * - Android 原生 SDK 方案（BluetoothLeScanner + BluetoothGatt + 协程队列）
 * - Nordic Android-BLE-Library 方案（BleManager 工业级架构 + suspend 挂起操作）
 */
@Route(path = RouterPath.Bluetooth.Main)
class BluetoothMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems = arrayListOf<RouterItem>()
        routerItems.add(RouterItem("── Android 原生 SDK 方案 ──", ""))
        routerItems.add(RouterItem("原生 BLE 扫描与过滤", RouterPath.Bluetooth.NativeScan))
        routerItems.add(RouterItem("原生 BLE 连接与 GATT 交互", RouterPath.Bluetooth.NativeConnect))
        routerItems.add(RouterItem("原生 BLE 协程队列与分包传输", RouterPath.Bluetooth.NativeQueue))
        routerItems.add(RouterItem("", ""))
        routerItems.add(RouterItem("── Nordic BLE 官方库方案 ──", ""))
        routerItems.add(RouterItem("Nordic BLE 扫描与过滤", RouterPath.Bluetooth.NordicScan))
        routerItems.add(RouterItem("Nordic BLE 连接与挂起调用", RouterPath.Bluetooth.NordicConnect))
        routerItems.add(RouterItem("Nordic BLE 大数据流式传输", RouterPath.Bluetooth.NordicTransfer))
        routerItems.add(RouterItem("", ""))
        routerItems.add(RouterItem("── FastBle 链式封装方案 ──", ""))
        routerItems.add(RouterItem("FastBle 扫描与规则过滤", RouterPath.Bluetooth.FastScan))
        routerItems.add(RouterItem("FastBle 连接与读写回调", RouterPath.Bluetooth.FastConnect))
        routerItems.add(RouterItem("", ""))
        routerItems.add(RouterItem("── RxAndroidBle 响应式流方案 ──", ""))
        routerItems.add(RouterItem("RxAndroidBle 响应式扫描与过滤", RouterPath.Bluetooth.RxScan))
        routerItems.add(RouterItem("RxAndroidBle 响应式连接与流控", RouterPath.Bluetooth.RxConnect))
        return routerItems
    }
}
