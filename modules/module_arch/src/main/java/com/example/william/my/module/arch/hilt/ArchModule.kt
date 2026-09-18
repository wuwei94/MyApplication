package com.example.william.my.module.arch.hilt

import com.example.william.my.core.base.app.hilt.interfaces.IAppInit
import com.example.william.my.core.base.app.hilt.qualifier.ArchInit
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Arch 模块 Hilt 依赖注入绑定模块
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ArchModule {

    @ArchInit
    @Binds
    abstract fun init(appInit: ArchInitImpl): IAppInit
}
