package com.example.william.my.application.hilt

import com.example.william.my.core.base.app.hilt.interfaces.IAppInit
import com.example.william.my.core.base.app.hilt.qualifier.AppInit
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * 应用初始化模块。
 *
 * 通过 Hilt 将 AppInitImpl 绑定为 @AppInit 限定符的 IAppInit 实现。
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @AppInit
    @Binds
    abstract fun init(appInit: AppInitImpl): IAppInit
}
