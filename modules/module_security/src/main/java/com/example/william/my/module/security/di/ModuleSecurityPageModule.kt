package com.example.william.my.module.security.di

import android.app.Activity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.security.SecurityMainActivity
import com.example.william.my.module.security.activity.SecureKeyActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * module_security 页面路由 Hilt 多重绑定模块
 */
@Module
@InstallIn(SingletonComponent::class)
object ModuleSecurityPageModule {

    @Provides
    @IntoMap
    @StringKey(RouterPath.Security.SecureKey)
    fun provideSecureKeyActivity(): Class<out Activity> = SecureKeyActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Security.Main)
    fun provideSecurityMainActivity(): Class<out Activity> = SecurityMainActivity::class.java
}
