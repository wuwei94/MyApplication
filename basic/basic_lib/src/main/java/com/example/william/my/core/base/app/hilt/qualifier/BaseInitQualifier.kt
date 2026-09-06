package com.example.william.my.core.base.app.hilt.qualifier

import javax.inject.Qualifier

/**
 * 基础初始化限定符（标记 [com.example.william.my.core.base.app.hilt.interfaces.IAppInit] 的基础实现）
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseInit

/**
 * 应用初始化限定符（标记 App 主模块的初始化实现）
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppInit

/**
 * 事件总线初始化限定符（标记 EventBus 相关初始化实现）
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class EventInit

/**
 * Mavericks 框架初始化限定符
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MavericksInit

/**
 * LoadSir 状态管理初始化限定符
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LoadSirInit

/**
 * Flutter 引擎初始化限定符
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FlutterInit
