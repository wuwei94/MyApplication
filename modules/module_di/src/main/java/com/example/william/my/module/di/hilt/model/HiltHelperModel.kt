package com.example.william.my.module.di.hilt.model

import android.content.Context
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 演示 ApplicationContext 与 ActivityContext 预置限定符注入
 */
class HiltContextHelper @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    @param:ActivityContext private val activityContext: Context
) {
    fun getContextInfo(): String {
        return "AppContext: ${appContext.javaClass.simpleName}, ActivityContext: ${activityContext.javaClass.simpleName}"
    }
}

/**
 * 全局单例作用域对象
 */
@Singleton
class HiltGlobalSingleton @Inject constructor() {
    val info: String = "GlobalSingleton 初始化完成"
}

/**
 * Activity 生命周期作用域对象
 */
@ActivityScoped
class HiltActivityScopedHelper @Inject constructor() {
    val info: String = "ActivityScopedHelper (当前 Activity 生命周期内单例)"
}

/**
 * 瞬态对象（无作用域，每次注入创建新实例）
 */
class HiltTransientHelper @Inject constructor() {
    val info: String = "TransientHelper (未指定作用域，每次注入新建)"
}
