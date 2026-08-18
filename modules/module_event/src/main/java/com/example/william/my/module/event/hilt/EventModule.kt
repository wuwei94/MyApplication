package com.example.william.my.module.event.hilt

import com.example.william.my.core.base.hilt.interfaces.IAppInit
import com.example.william.my.core.base.hilt.qualifier.EventInit
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Event 模块 Hilt 依赖注入绑定模块
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class EventModule {

    @EventInit
    @Binds
    abstract fun init(appInit: EventInitImpl): IAppInit
}