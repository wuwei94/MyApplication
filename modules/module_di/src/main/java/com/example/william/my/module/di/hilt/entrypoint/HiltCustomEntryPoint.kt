package com.example.william.my.module.di.hilt.entrypoint

import android.content.Context
import com.example.william.my.module.di.hilt.model.HiltNetworkClient
import com.example.william.my.module.di.hilt.model.HiltStorageService
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * 自定义入口点接口：用于在非 @AndroidEntryPoint 类（如普通工具类、后台拦截器、ContentProvider）中获取 Hilt 依赖
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface HiltCustomEntryPoint {
    fun getStorageService(): HiltStorageService
    fun getNetworkClient(): HiltNetworkClient
}

/**
 * 模拟无法直接使用 @AndroidEntryPoint 的第三方或普通业务类
 */
class NonHiltComponentHelper(private val context: Context) {

    fun performTask(): String {
        // 从 ApplicationContext 获取 EntryPoint 接口实现
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            context,
            HiltCustomEntryPoint::class.java
        )
        val storage = hiltEntryPoint.getStorageService()
        val network = hiltEntryPoint.getNetworkClient()
        return "NonHiltComponentHelper 动态获取成功 -> [${storage.saveData("entry_point_key", "ok")}] [${network.request("/entry")}]"
    }
}
