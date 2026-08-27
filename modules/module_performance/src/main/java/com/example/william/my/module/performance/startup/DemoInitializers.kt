package com.example.william.my.module.performance.startup

import android.annotation.SuppressLint
import android.content.Context
import androidx.startup.Initializer
import com.example.william.my.basic.basic_shared.utils.Utils

/**
 * 模拟日志 SDK 实例
 */
data class LogSdk(val initTimeMillis: Long, val version: String = "1.0.0")

/**
 * 模拟安全加密 SDK 实例（依赖 LogSdk 先行初始化）
 */
data class SecuritySdk(val initTimeMillis: Long, val logSdkRef: LogSdk?)

/**
 * 模拟手动延迟按需初始化的 SDK 实例
 */
data class ManualLazySdk(val initTimeMillis: Long, val state: String = "Initialized on-demand")

/**
 * 1. 日志 SDK 自动初始化器（根节点，无前置依赖）
 */
class LogSdkInitializer : Initializer<LogSdk> {

    override fun create(context: Context): LogSdk {
        val startTime = System.currentTimeMillis()
        Utils.logcat(TAG, "LogSdkInitializer -> create() 执行初始化...")
        return LogSdk(initTimeMillis = startTime)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        // 无依赖，优先执行
        return emptyList()
    }

    companion object {
        private const val TAG = "StartupLogSdk"
    }
}

/**
 * 2. 安全 SDK 自动初始化器（依赖 LogSdkInitializer）
 *
 * App Startup 会根据 dependencies() 自动构建有向无环图（DAG）并进行拓扑排序，
 * 保证 LogSdkInitializer 必然在 SecuritySdkInitializer 之前完成初始化。
 */
class SecuritySdkInitializer : Initializer<SecuritySdk> {

    override fun create(context: Context): SecuritySdk {
        val startTime = System.currentTimeMillis()
        Utils.logcat(TAG, "SecuritySdkInitializer -> create() 执行初始化（依赖 LogSdkInitializer 已就绪）...")
        return SecuritySdk(initTimeMillis = startTime, logSdkRef = null)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        // 声明前置依赖
        return listOf(LogSdkInitializer::class.java)
    }

    companion object {
        private const val TAG = "StartupSecuritySdk"
    }
}

/**
 * 3. 手动延迟按需初始化器
 *
 * 在 AndroidManifest.xml 中不显式暴露给 InitializationProvider（或通过 tools:node="remove" 禁用自动初始化），
 * 仅在业务真正需要时通过 AppInitializer.getInstance(context).initializeComponent(ManualLazyInitializer::class.java) 触发。
 *
 * 注意：本类故意不在 AndroidManifest.xml 中声明 <meta-data>（避免自动初始化），
 * 因此需要抑制 EnsureInitializerMetadata 这条 lint 检查。
 */
@SuppressLint("EnsureInitializerMetadata")
class ManualLazyInitializer : Initializer<ManualLazySdk> {

    override fun create(context: Context): ManualLazySdk {
        val startTime = System.currentTimeMillis()
        Utils.logcat(TAG, "ManualLazyInitializer -> create() 被手动按需调用触发！")
        return ManualLazySdk(initTimeMillis = startTime)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }

    companion object {
        private const val TAG = "StartupLazySdk"
    }
}
