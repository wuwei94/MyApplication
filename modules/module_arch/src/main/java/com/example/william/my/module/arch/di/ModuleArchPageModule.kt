package com.example.william.my.module.arch.di

import android.app.Activity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.arch.ArchMainActivity
import com.example.william.my.module.arch.compose.activity.ComposeMviActivity
import com.example.william.my.module.arch.mvi.activity.MviActivity
import com.example.william.my.module.arch.mvp.activity.MvpActivity
import com.example.william.my.module.arch.mvvm.activity.MvvmActivity
import com.example.william.my.module.arch.ssot.activity.OfflineFirstActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * module_arch 页面路由 Hilt 多重绑定模块
 */
@Module
@InstallIn(SingletonComponent::class)
object ModuleArchPageModule {

    @Provides
    @IntoMap
    @StringKey(RouterPath.Arch.Main)
    fun provideArchMainActivity(): Class<out Activity> = ArchMainActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Arch.ComposeMVI)
    fun provideComposeMviActivity(): Class<out Activity> = ComposeMviActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Arch.MVI)
    fun provideMviActivity(): Class<out Activity> = MviActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Arch.MVP)
    fun provideMvpActivity(): Class<out Activity> = MvpActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Arch.MVVM)
    fun provideMvvmActivity(): Class<out Activity> = MvvmActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Arch.OfflineFirst)
    fun provideOfflineFirstActivity(): Class<out Activity> = OfflineFirstActivity::class.java
}
