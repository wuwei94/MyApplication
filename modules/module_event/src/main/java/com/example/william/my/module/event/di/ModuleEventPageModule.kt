package com.example.william.my.module.event.di

import android.app.Activity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.event.EventMainActivity
import com.example.william.my.module.event.eventbus.activity.EventBusActivity
import com.example.william.my.module.event.floweventbus.activity.FlowEventBusActivity
import com.example.william.my.module.event.liveeventbus.activity.LiveEventBusActivity
import com.example.william.my.module.event.rxeventbus.activity.RxEventBusActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * module_event 页面路由 Hilt 多重绑定模块
 */
@Module
@InstallIn(SingletonComponent::class)
object ModuleEventPageModule {

    @Provides
    @IntoMap
    @StringKey(RouterPath.Event.EventBus)
    fun provideEventBusActivity(): Class<out Activity> = EventBusActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Event.Main)
    fun provideEventMainActivity(): Class<out Activity> = EventMainActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Event.FlowEventBus)
    fun provideFlowEventBusActivity(): Class<out Activity> = FlowEventBusActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Event.LiveEventBus)
    fun provideLiveEventBusActivity(): Class<out Activity> = LiveEventBusActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Event.RxEventBus)
    fun provideRxEventBusActivity(): Class<out Activity> = RxEventBusActivity::class.java
}
