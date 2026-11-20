package com.example.william.my.module.di.di

import android.app.Activity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.di.DiMainActivity
import com.example.william.my.module.di.hilt.activity.HiltActivity
import com.example.william.my.module.di.koin.activity.KoinActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * module_di 页面路由 Hilt 多重绑定模块
 */
@Module
@InstallIn(SingletonComponent::class)
object ModuleDiPageModule {

    @Provides
    @IntoMap
    @StringKey(RouterPath.Di.Main)
    fun provideDiMainActivity(): Class<out Activity> = DiMainActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Di.Hilt)
    fun provideHiltActivity(): Class<out Activity> = HiltActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Di.Koin)
    fun provideKoinActivity(): Class<out Activity> = KoinActivity::class.java
}
