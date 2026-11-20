package com.example.william.my.module.ipc.di

import android.app.Activity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.ipc.IpcMainActivity
import com.example.william.my.module.ipc.activity.AIDLActivity
import com.example.william.my.module.ipc.activity.MessengerActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * module_ipc 页面路由 Hilt 多重绑定模块
 */
@Module
@InstallIn(SingletonComponent::class)
object ModuleIpcPageModule {

    @Provides
    @IntoMap
    @StringKey(RouterPath.Ipc.AIDL)
    fun provideAIDLActivity(): Class<out Activity> = AIDLActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Ipc.Messenger)
    fun provideMessengerActivity(): Class<out Activity> = MessengerActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Ipc.Main)
    fun provideIpcMainActivity(): Class<out Activity> = IpcMainActivity::class.java
}
