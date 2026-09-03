package com.example.william.my.core.base.coroutine

import javax.inject.Qualifier

/**
 * Application 全局生命周期协程作用域限定符注解（对齐 Google Now in Android 规范）
 *
 * 适用于不绑定特定页面生命周期的全局单例后台任务，内部由 [kotlinx.coroutines.SupervisorJob] 守护，
 * 避免单个子任务失败导致整个作用域失效。
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope
