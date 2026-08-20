package com.example.william.my.module.di.hilt.module

import com.example.william.my.module.di.hilt.model.HiltDiskStorageServiceImpl
import com.example.william.my.module.di.hilt.model.HiltStorageService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * 演示 @Binds 将实现类绑定至抽象接口
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class HiltStorageModule {

    @Binds
    abstract fun bindStorageService(impl: HiltDiskStorageServiceImpl): HiltStorageService
}
