package com.example.william.my.core.base.hilt

import com.example.william.my.core.base.hilt.interfaces.IAppInit
import com.example.william.my.core.base.hilt.qualifier.BaseInit
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class BaseModule {

    @BaseInit
    @Binds
    abstract fun init(appInit: BaseInitImpl): IAppInit
}