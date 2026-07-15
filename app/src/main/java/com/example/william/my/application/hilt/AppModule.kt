package com.example.william.my.application.hilt

import com.example.william.my.core.base.hilt.interfaces.IAppInit
import com.example.william.my.core.base.hilt.qualifier.AppInit
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @AppInit
    @Binds
    abstract fun init(appInit: AppInitImpl): IAppInit
}