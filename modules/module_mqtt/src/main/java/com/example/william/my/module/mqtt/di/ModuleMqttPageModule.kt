package com.example.william.my.module.mqtt.di

import android.app.Activity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.mqtt.MqttMainActivity
import com.example.william.my.module.mqtt.hivemq.activity.HiveMqClientActivity
import com.example.william.my.module.mqtt.paho.activity.PahoServiceClientActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * module_mqtt 页面路由 Hilt 多重绑定模块
 */
@Module
@InstallIn(SingletonComponent::class)
object ModuleMqttPageModule {

    @Provides
    @IntoMap
    @StringKey(RouterPath.Mqtt.HiveMqClient)
    fun provideHiveMqClientActivity(): Class<out Activity> = HiveMqClientActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Mqtt.Main)
    fun provideMqttMainActivity(): Class<out Activity> = MqttMainActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Mqtt.PahoServiceClient)
    fun providePahoServiceClientActivity(): Class<out Activity> = PahoServiceClientActivity::class.java
}
