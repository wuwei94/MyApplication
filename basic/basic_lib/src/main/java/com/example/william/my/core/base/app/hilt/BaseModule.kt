package com.example.william.my.core.base.app.hilt

import com.example.william.my.core.base.app.hilt.interfaces.IAppInit
import com.example.william.my.core.base.app.hilt.qualifier.BaseInit
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * 应用初始化 Hilt 依赖注入模块
 *
 * 将 [BaseInitImpl] 绑定为 [IAppInit] 的默认实现，供各模块通过 Hilt 注入统一的应用初始化入口。
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class BaseModule {

    @BaseInit
    @Binds
    abstract fun init(appInit: BaseInitImpl): IAppInit
}
