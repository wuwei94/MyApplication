package com.example.william.my.module.sse.di

import android.app.Activity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.sse.SseMainActivity
import com.example.william.my.module.sse.ktor.activity.KtorSseClientActivity
import com.example.william.my.module.sse.ktor.activity.KtorSseClientFlowActivity
import com.example.william.my.module.sse.okhttp.activity.OkHttpSseClientActivity
import com.example.william.my.module.sse.okhttp.activity.OkHttpSseClientFlowActivity
import com.example.william.my.module.sse.okhttp.activity.OkHttpSseClientRxActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * module_sse 页面路由 Hilt 多重绑定模块
 */
@Module
@InstallIn(SingletonComponent::class)
object ModuleSsePageModule {

    @Provides
    @IntoMap
    @StringKey(RouterPath.SSE.KtorSseClient)
    fun provideKtorSseClientActivity(): Class<out Activity> = KtorSseClientActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.SSE.KtorSseClientFlow)
    fun provideKtorSseClientFlowActivity(): Class<out Activity> = KtorSseClientFlowActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.SSE.OkHttpSseClient)
    fun provideOkHttpSseClientActivity(): Class<out Activity> = OkHttpSseClientActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.SSE.OkHttpSseClientFlow)
    fun provideOkHttpSseClientFlowActivity(): Class<out Activity> = OkHttpSseClientFlowActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.SSE.OkHttpSseClientRx)
    fun provideOkHttpSseClientRxActivity(): Class<out Activity> = OkHttpSseClientRxActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.SSE.Main)
    fun provideSseMainActivity(): Class<out Activity> = SseMainActivity::class.java
}
