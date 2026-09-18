package com.example.william.my.module.widget_thirdparty.hilt

import com.example.william.my.core.base.app.hilt.interfaces.IAppInit
import com.example.william.my.core.base.app.hilt.qualifier.WidgetThirdpartyInit
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * WidgetThirdparty 模块 Hilt 依赖注入绑定模块
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class WidgetThirdpartyModule {

    @WidgetThirdpartyInit
    @Binds
    abstract fun init(appInit: WidgetThirdpartyInitImpl): IAppInit
}
