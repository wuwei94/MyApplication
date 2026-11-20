package com.example.william.my.module.jetpack.di

import android.app.Activity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.jetpack.JetpackMainActivity
import com.example.william.my.module.jetpack.activity.LifecycleActivity
import com.example.william.my.module.jetpack.activity.PagingActivity
import com.example.william.my.module.jetpack.activity.ViewModelActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * module_jetpack 页面路由 Hilt 多重绑定模块
 */
@Module
@InstallIn(SingletonComponent::class)
object ModuleJetpackPageModule {

    @Provides
    @IntoMap
    @StringKey(RouterPath.Jetpack.Lifecycle)
    fun provideLifecycleActivity(): Class<out Activity> = LifecycleActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Jetpack.Paging)
    fun providePagingActivity(): Class<out Activity> = PagingActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Jetpack.ViewModel)
    fun provideViewModelActivity(): Class<out Activity> = ViewModelActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Jetpack.Main)
    fun provideJetpackMainActivity(): Class<out Activity> = JetpackMainActivity::class.java
}
