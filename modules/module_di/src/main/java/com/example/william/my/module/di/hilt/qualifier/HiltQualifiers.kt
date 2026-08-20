package com.example.william.my.module.di.hilt.qualifier

import javax.inject.Qualifier

/**
 * 生产环境 API 限定符
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ProdApi

/**
 * 测试/沙箱环境 API 限定符
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DevApi
