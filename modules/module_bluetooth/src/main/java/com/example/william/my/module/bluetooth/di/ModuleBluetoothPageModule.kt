package com.example.william.my.module.bluetooth.di

import android.app.Activity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.bluetooth.BluetoothMainActivity
import com.example.william.my.module.bluetooth.fast_ble.activity.BleFastConnectActivity
import com.example.william.my.module.bluetooth.fast_ble.activity.BleFastScanActivity
import com.example.william.my.module.bluetooth.native_ble.activity.BleNativeConnectActivity
import com.example.william.my.module.bluetooth.native_ble.activity.BleNativeQueueActivity
import com.example.william.my.module.bluetooth.native_ble.activity.BleNativeScanActivity
import com.example.william.my.module.bluetooth.nordic_ble.activity.BleNordicConnectActivity
import com.example.william.my.module.bluetooth.nordic_ble.activity.BleNordicScanActivity
import com.example.william.my.module.bluetooth.nordic_ble.activity.BleNordicTransferActivity
import com.example.william.my.module.bluetooth.rx_ble.activity.BleRxConnectActivity
import com.example.william.my.module.bluetooth.rx_ble.activity.BleRxScanActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * module_bluetooth 页面路由 Hilt 多重绑定模块
 */
@Module
@InstallIn(SingletonComponent::class)
object ModuleBluetoothPageModule {

    @Provides
    @IntoMap
    @StringKey(RouterPath.Bluetooth.Main)
    fun provideBluetoothMainActivity(): Class<out Activity> = BluetoothMainActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Bluetooth.FastConnect)
    fun provideBleFastConnectActivity(): Class<out Activity> = BleFastConnectActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Bluetooth.FastScan)
    fun provideBleFastScanActivity(): Class<out Activity> = BleFastScanActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Bluetooth.NativeConnect)
    fun provideBleNativeConnectActivity(): Class<out Activity> = BleNativeConnectActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Bluetooth.NativeQueue)
    fun provideBleNativeQueueActivity(): Class<out Activity> = BleNativeQueueActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Bluetooth.NativeScan)
    fun provideBleNativeScanActivity(): Class<out Activity> = BleNativeScanActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Bluetooth.NordicConnect)
    fun provideBleNordicConnectActivity(): Class<out Activity> = BleNordicConnectActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Bluetooth.NordicScan)
    fun provideBleNordicScanActivity(): Class<out Activity> = BleNordicScanActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Bluetooth.NordicTransfer)
    fun provideBleNordicTransferActivity(): Class<out Activity> = BleNordicTransferActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Bluetooth.RxConnect)
    fun provideBleRxConnectActivity(): Class<out Activity> = BleRxConnectActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Bluetooth.RxScan)
    fun provideBleRxScanActivity(): Class<out Activity> = BleRxScanActivity::class.java
}
