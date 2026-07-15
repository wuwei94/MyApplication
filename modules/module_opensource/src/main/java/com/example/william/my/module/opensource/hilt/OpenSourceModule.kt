package com.example.william.my.module.opensource.hilt

import com.example.william.my.core.base.hilt.interfaces.IAppInit
import com.example.william.my.core.base.hilt.qualifier.OpenSourceInit
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class OpenSourceModule {

    @OpenSourceInit    @Binds
    abstract fun init(appInit: OpenSourceInitImpl): IAppInit
}