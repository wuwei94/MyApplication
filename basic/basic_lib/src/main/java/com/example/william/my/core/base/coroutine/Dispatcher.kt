package com.example.william.my.core.base.coroutine

import javax.inject.Qualifier

/**
 * 协程调度器 Hilt 限定符注解（对齐 Google Now in Android 规范）
 *
 * @param appDispatcher 指定注入的调度器类型 [AppDispatchers]
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val appDispatcher: AppDispatchers)
