package com.example.william.my.core.base.coroutine.di

import com.example.william.my.core.base.coroutine.AppDispatchers
import com.example.william.my.core.base.coroutine.ApplicationScope
import com.example.william.my.core.base.coroutine.Dispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

/**
 * 全局协程作用域 Hilt 依赖注入模块（对齐 Google Now in Android 规范）
 */
@Module
@InstallIn(SingletonComponent::class)
object CoroutineScopesModule {

    @Provides
    @Singleton
    @ApplicationScope
    fun providesCoroutineScope(
        @Dispatcher(AppDispatchers.Default) dispatcher: CoroutineDispatcher,
    ): CoroutineScope = CoroutineScope(SupervisorJob() + dispatcher)
}
