package com.example.william.my.module.di.hilt.module

import com.example.william.my.module.di.hilt.model.HiltNetworkClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * 演示 @Provides 实例化并配置第三方类
 */
@Module
@InstallIn(SingletonComponent::class)
object HiltNetworkModule {

    @Provides
    fun provideNetworkClient(): HiltNetworkClient = HiltNetworkClient(baseUrl = "https://api.example.com", timeoutSeconds = 30)
}
