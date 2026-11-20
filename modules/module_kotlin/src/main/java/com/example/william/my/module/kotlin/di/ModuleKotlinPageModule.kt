package com.example.william.my.module.kotlin.di

import android.app.Activity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.kotlin.KotlinMainActivity
import com.example.william.my.module.kotlin.activity.ChannelActivity
import com.example.william.my.module.kotlin.activity.ConcurrencyActivity
import com.example.william.my.module.kotlin.activity.CoroutinesActivity
import com.example.william.my.module.kotlin.activity.DelegateActivity
import com.example.william.my.module.kotlin.activity.FlowActivity
import com.example.william.my.module.kotlin.activity.InlineActivity
import com.example.william.my.module.kotlin.activity.SyntaxActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * module_kotlin 页面路由 Hilt 多重绑定模块
 */
@Module
@InstallIn(SingletonComponent::class)
object ModuleKotlinPageModule {

    @Provides
    @IntoMap
    @StringKey(RouterPath.Kotlin.Channel)
    fun provideChannelActivity(): Class<out Activity> = ChannelActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Kotlin.Concurrency)
    fun provideConcurrencyActivity(): Class<out Activity> = ConcurrencyActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Kotlin.Coroutines)
    fun provideCoroutinesActivity(): Class<out Activity> = CoroutinesActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Kotlin.Delegate)
    fun provideDelegateActivity(): Class<out Activity> = DelegateActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Kotlin.Flow)
    fun provideFlowActivity(): Class<out Activity> = FlowActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Kotlin.Inline)
    fun provideInlineActivity(): Class<out Activity> = InlineActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Kotlin.Syntax)
    fun provideSyntaxActivity(): Class<out Activity> = SyntaxActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Kotlin.Main)
    fun provideKotlinMainActivity(): Class<out Activity> = KotlinMainActivity::class.java
}
