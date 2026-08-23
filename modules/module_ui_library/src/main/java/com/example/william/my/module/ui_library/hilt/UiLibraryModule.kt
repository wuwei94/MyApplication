package com.example.william.my.module.ui_library.hilt

import com.example.william.my.core.base.hilt.interfaces.IAppInit
import com.example.william.my.core.base.hilt.qualifier.UiLibraryInit
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class UiLibraryModule {

    @UiLibraryInit
    @Binds
    abstract fun init(appInit: UiLibraryInitImpl): IAppInit
}
