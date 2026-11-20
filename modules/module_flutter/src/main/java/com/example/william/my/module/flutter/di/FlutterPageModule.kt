package com.example.william.my.module.flutter.di

import android.app.Activity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.flutter.FlutterMainActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * module_flutter 页面路由 Hilt 多重绑定模块
 */
@Module
@InstallIn(SingletonComponent::class)
object FlutterPageModule {

    @Provides
    @IntoMap
    @StringKey(RouterPath.Flutter.Main)
    fun provideFlutterMainActivity(): Class<out Activity> = FlutterMainActivity::class.java
}
