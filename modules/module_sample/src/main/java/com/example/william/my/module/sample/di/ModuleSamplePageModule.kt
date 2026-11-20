package com.example.william.my.module.sample.di

import android.app.Activity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.sample.SampleMainActivity
import com.example.william.my.module.sample.activity.HookActivity
import com.example.william.my.module.sample.activity.TypefaceActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * module_sample 页面路由 Hilt 多重绑定模块
 */
@Module
@InstallIn(SingletonComponent::class)
object ModuleSamplePageModule {

    @Provides
    @IntoMap
    @StringKey(RouterPath.Sample.Hook)
    fun provideHookActivity(): Class<out Activity> = HookActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Sample.Typeface)
    fun provideTypefaceActivity(): Class<out Activity> = TypefaceActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Sample.Main)
    fun provideSampleMainActivity(): Class<out Activity> = SampleMainActivity::class.java
}
