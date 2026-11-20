package com.example.william.my.module.async.di

import android.app.Activity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.async.AsyncMainActivity
import com.example.william.my.module.async.activity.AsyncTaskActivity
import com.example.william.my.module.async.activity.HandlerThreadActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * module_async 页面路由 Hilt 多重绑定模块
 */
@Module
@InstallIn(SingletonComponent::class)
object ModuleAsyncPageModule {

    @Provides
    @IntoMap
    @StringKey(RouterPath.Async.AsyncTask)
    fun provideAsyncTaskActivity(): Class<out Activity> = AsyncTaskActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Async.HandlerThread)
    fun provideHandlerThreadActivity(): Class<out Activity> = HandlerThreadActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Async.Main)
    fun provideAsyncMainActivity(): Class<out Activity> = AsyncMainActivity::class.java
}
