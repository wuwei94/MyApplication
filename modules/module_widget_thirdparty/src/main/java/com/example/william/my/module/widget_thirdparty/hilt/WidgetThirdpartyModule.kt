package com.example.william.my.module.widget_thirdparty.hilt

import com.example.william.my.core.base.hilt.interfaces.IAppInit
import com.example.william.my.core.base.hilt.qualifier.WidgetThirdpartyInit
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class WidgetThirdpartyModule {

    @WidgetThirdpartyInit
    @Binds
    abstract fun init(appInit: WidgetThirdpartyInitImpl): IAppInit
}
