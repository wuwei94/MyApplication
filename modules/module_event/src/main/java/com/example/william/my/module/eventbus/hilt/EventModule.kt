package com.example.william.my.module.eventbus.hilt

import com.example.william.my.core.base.hilt.interfaces.IAppInit
import com.example.william.my.core.base.hilt.qualifier.EventInit
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class EventModule {

    @EventInit
    @Binds
    abstract fun init(appInit: EventInitImpl): IAppInit
}