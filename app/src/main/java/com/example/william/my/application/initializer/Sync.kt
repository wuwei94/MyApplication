package com.example.william.my.application.initializer

import android.content.Context
import androidx.startup.AppInitializer
import androidx.startup.Initializer
import com.example.william.my.basic.basic_shared.utils.Utils

/**
 * 同步进程手动初始化入口。
 *
 * 提供绕过 AndroidX Startup 自动初始化的手动初始化方式。
 */
object Sync {
    /**
     * 手动初始化同步进程的兜底方案。
     *
     * 不依赖 AndroidX Startup 的自动初始化，由 app 模块的 Application.onCreate() 调用，且只应执行一次。
     */
    fun initialize(context: Context) {
        AppInitializer.getInstance(context)
            .initializeComponent(StartupInitializer::class.java)
    }
}

/**
 * 注册周期性同步数据层的工作，在应用启动时执行。
 */
class SyncInitializer : Initializer<Sync> {

    private val TAG = this.javaClass.simpleName

    override fun create(context: Context): Sync {
        Utils.logcat(TAG, "SyncInitializer init")
        return Sync
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = listOf(StartupInitializer::class.java)
}

/**
 * 启动初始化器。
 *
 * 作为同步链路的首个依赖项，确保初始化顺序。
 */
class StartupInitializer : Initializer<String> {

    private val TAG = this.javaClass.simpleName

    override fun create(context: Context): String {
        Utils.logcat(TAG, "StartupInitializer init")
        return "Startup Init"
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        // 不依赖其他库。
        return emptyList()
    }
}
