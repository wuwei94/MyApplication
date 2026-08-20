package com.example.william.my.module.di.hilt.module

import com.example.william.my.module.di.hilt.model.HiltApiService
import com.example.william.my.module.di.hilt.model.HiltDevApiServiceImpl
import com.example.william.my.module.di.hilt.model.HiltProdApiServiceImpl
import com.example.william.my.module.di.hilt.qualifier.DevApi
import com.example.william.my.module.di.hilt.qualifier.ProdApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * 演示通过限定符注解 (@ProdApi / @DevApi) 为同一接口提供多个不同实现
 */
@Module
@InstallIn(SingletonComponent::class)
object HiltApiModule {

    @Provides
    @ProdApi
    fun provideProdApi(): HiltApiService = HiltProdApiServiceImpl()

    @Provides
    @DevApi
    fun provideDevApi(): HiltApiService = HiltDevApiServiceImpl()
}
