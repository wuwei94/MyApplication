package com.example.william.my.basic.basic_shared.di

import com.example.william.my.basic.basic_shared.router.service.utils.FileIOUtilsService
import com.example.william.my.basic.basic_shared.router.service.utils.ImageUtilsService
import com.example.william.my.basic.basic_shared.router.service.utils.ResourceUtilsService
import com.example.william.my.basic.basic_shared.router.service.utils.impl.FileIOUtilsServiceImpl
import com.example.william.my.basic.basic_shared.router.service.utils.impl.ImageUtilsServiceImpl
import com.example.william.my.basic.basic_shared.router.service.utils.impl.ResourceUtilsServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 共享工具服务 Hilt 绑定模块
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SharedServiceModule {

    @Binds
    @Singleton
    abstract fun bindFileIOUtilsService(impl: FileIOUtilsServiceImpl): FileIOUtilsService

    @Binds
    @Singleton
    abstract fun bindImageUtilsService(impl: ImageUtilsServiceImpl): ImageUtilsService

    @Binds
    @Singleton
    abstract fun bindResourceUtilsService(impl: ResourceUtilsServiceImpl): ResourceUtilsService
}
