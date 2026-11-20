package com.example.william.my.module.systemservice.di

import android.app.Activity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.systemservice.SystemServiceMainActivity
import com.example.william.my.module.systemservice.activity.floatwindow.EasyFloatActivity
import com.example.william.my.module.systemservice.activity.floatwindow.NativeFloatWindowActivity
import com.example.william.my.module.systemservice.activity.notification.NotificationActivity
import com.example.william.my.module.systemservice.activity.permission.JetpackPermissionActivity
import com.example.william.my.module.systemservice.activity.permission.PermissionXActivity
import com.example.william.my.module.systemservice.activity.timezone.TimeZoneMonitorActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * module_system_service 页面路由 Hilt 多重绑定模块
 */
@Module
@InstallIn(SingletonComponent::class)
object ModuleSystemServicePageModule {

    @Provides
    @IntoMap
    @StringKey(RouterPath.SystemService.EasyFloat)
    fun provideEasyFloatActivity(): Class<out Activity> = EasyFloatActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.SystemService.NativeFloatWindow)
    fun provideNativeFloatWindowActivity(): Class<out Activity> = NativeFloatWindowActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.SystemService.Notification)
    fun provideNotificationActivity(): Class<out Activity> = NotificationActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.SystemService.JetpackPermission)
    fun provideJetpackPermissionActivity(): Class<out Activity> = JetpackPermissionActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.SystemService.PermissionX)
    fun providePermissionXActivity(): Class<out Activity> = PermissionXActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.SystemService.TimeZoneMonitor)
    fun provideTimeZoneMonitorActivity(): Class<out Activity> = TimeZoneMonitorActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.SystemService.Main)
    fun provideSystemServiceMainActivity(): Class<out Activity> = SystemServiceMainActivity::class.java
}
