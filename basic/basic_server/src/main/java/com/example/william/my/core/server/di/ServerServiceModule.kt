package com.example.william.my.core.server.di

import com.example.william.my.basic.basic_shared.router.service.server.JavaWebSocketServerService
import com.example.william.my.basic.basic_shared.router.service.server.NanoServerService
import com.example.william.my.basic.basic_shared.router.service.server.NettyServerService
import com.example.william.my.core.server.service.JavaWebSocketServerServiceImpl
import com.example.william.my.core.server.service.NanoServerServiceImpl
import com.example.william.my.core.server.service.NettyServerServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 本地服务端 Hilt 绑定模块
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ServerServiceModule {

    @Binds
    @Singleton
    abstract fun bindJavaWebSocketServerService(impl: JavaWebSocketServerServiceImpl): JavaWebSocketServerService

    @Binds
    @Singleton
    abstract fun bindNanoServerService(impl: NanoServerServiceImpl): NanoServerService

    @Binds
    @Singleton
    abstract fun bindNettyServerService(impl: NettyServerServiceImpl): NettyServerService
}
