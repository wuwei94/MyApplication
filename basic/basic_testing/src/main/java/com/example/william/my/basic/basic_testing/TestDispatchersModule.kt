package com.example.william.my.basic.basic_testing

import com.example.william.my.core.base.coroutine.AppDispatchers
import com.example.william.my.core.base.coroutine.Dispatcher
import com.example.william.my.core.base.coroutine.di.DispatchersModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher

/**
 * 测试专用协程调度器 Hilt 模块
 *
 * 通过 `@TestInstallIn` 在仪器化 / Hilt 单测中自动替换生产 [DispatchersModule]，
 * 将 IO / Default / Main 统一重定向到 [UnconfinedTestDispatcher]，消除真实线程调度的不确定性。
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DispatchersModule::class],
)
object TestDispatchersModule {

    @Provides
    @Dispatcher(AppDispatchers.IO)
    fun providesTestIODispatcher(): CoroutineDispatcher = UnconfinedTestDispatcher()

    @Provides
    @Dispatcher(AppDispatchers.Default)
    fun providesTestDefaultDispatcher(): CoroutineDispatcher = UnconfinedTestDispatcher()

    @Provides
    @Dispatcher(AppDispatchers.Main)
    fun providesTestMainDispatcher(): CoroutineDispatcher = UnconfinedTestDispatcher()
}
