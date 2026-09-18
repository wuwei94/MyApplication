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
 * Arch 模块初始化限定符（module_arch，启动时初始化 Mavericks）
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ArchInit

/**
 * WidgetThirdparty 模块初始化限定符（启动时配置 LoadSir）
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class WidgetThirdpartyInit

/**
 * Flutter 引擎初始化限定符
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FlutterInit
