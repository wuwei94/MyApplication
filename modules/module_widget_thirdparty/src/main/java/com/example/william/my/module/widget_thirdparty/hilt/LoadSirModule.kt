package com.example.william.my.module.widget_thirdparty.hilt

import com.example.william.my.core.base.app.hilt.interfaces.IAppInit
import com.example.william.my.core.base.app.hilt.qualifier.LoadSirInit
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class LoadSirModule {

    @LoadSirInit
    @Binds
    abstract fun init(appInit: LoadSirInitImpl): IAppInit
}
