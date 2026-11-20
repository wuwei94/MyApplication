package com.example.william.my.module.reactive.di

import android.app.Activity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.reactive.ReactiveMainActivity
import com.example.william.my.module.reactive.activity.FlowOperatorsActivity
import com.example.william.my.module.reactive.activity.RxJavaOperatorsActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * module_reactive 页面路由 Hilt 多重绑定模块
 */
@Module
@InstallIn(SingletonComponent::class)
object ModuleReactivePageModule {

    @Provides
    @IntoMap
    @StringKey(RouterPath.Reactive.FlowOperators)
    fun provideFlowOperatorsActivity(): Class<out Activity> = FlowOperatorsActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Reactive.RxJavaOperators)
    fun provideRxJavaOperatorsActivity(): Class<out Activity> = RxJavaOperatorsActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Reactive.Main)
    fun provideReactiveMainActivity(): Class<out Activity> = ReactiveMainActivity::class.java
}
