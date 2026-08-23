package com.example.william.my.module.mavericks.hilt

import com.example.william.my.core.base.hilt.interfaces.IAppInit
import com.example.william.my.core.base.hilt.qualifier.MavericksInit
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Mavericks 模块 Hilt 依赖注入绑定模块
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class MavericksModule {

    @MavericksInit
    @Binds
    abstract fun init(appInit: MavericksInitImpl): IAppInit
}
