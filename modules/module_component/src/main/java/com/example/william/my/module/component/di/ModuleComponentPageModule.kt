package com.example.william.my.module.component.di

import android.app.Activity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.component.ComponentMainActivity
import com.example.william.my.module.component.activity.ActivityResultActivity
import com.example.william.my.module.component.activity.BroadcastActivity
import com.example.william.my.module.component.activity.OnBackPressedActivity
import com.example.william.my.module.component.activity.ServiceActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * module_component 页面路由 Hilt 多重绑定模块
 */
@Module
@InstallIn(SingletonComponent::class)
object ModuleComponentPageModule {

    @Provides
    @IntoMap
    @StringKey(RouterPath.Component.ActivityResult)
    fun provideActivityResultActivity(): Class<out Activity> = ActivityResultActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Component.Broadcast)
    fun provideBroadcastActivity(): Class<out Activity> = BroadcastActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Component.OnBackPressed)
    fun provideOnBackPressedActivity(): Class<out Activity> = OnBackPressedActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Component.Service)
    fun provideServiceActivity(): Class<out Activity> = ServiceActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Component.Main)
    fun provideComponentMainActivity(): Class<out Activity> = ComponentMainActivity::class.java
}
