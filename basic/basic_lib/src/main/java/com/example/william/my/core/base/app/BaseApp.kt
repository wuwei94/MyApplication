package com.example.william.my.core.base.app

import android.app.Application
import android.content.res.Configuration
import android.os.Process
import com.blankj.utilcode.util.Utils
import java.util.concurrent.Executors

/**
 * 应用基类
 *
 * 统一调度各模块 BaseAppInit 的应用级初始化逻辑。
 */
abstract class BaseApp : Application() {

    private val appInitList: MutableList<BaseAppInit> = ArrayList()
    private val classInitList: MutableList<Class<out BaseAppInit>> = ArrayList()

    override fun onCreate() {
        super.onCreate()
        setApp(this)
        Utils.init(this)

        initApp()

        initModuleApp()

        modulesApplicationInit()

        modulesApplicationInitAsync()
    }

    private fun initModuleApp() {
        for (classInit in classInitList) {
            try {
                val appInit = classInit.newInstance()
                appInit.setApplication(this)
                appInitList.add(appInit)
            } catch (e: InstantiationException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
        }
    }

    private fun modulesApplicationInit() {
        // Module类的APP初始化
        for (app in appInitList) {
            app.init()
        }
    }

    private fun modulesApplicationInitAsync() {
        Executors.newSingleThreadExecutor().execute {
            // 设置线程的优先级，不与主线程抢资源
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
            // Module类的APP初始化异步
            for (app in appInitList) {
                app.initAsync()
            }
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        for (app in appInitList) {
            app.onLowMemory()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        for (app in appInitList) {
            app.onConfigurationChanged(newConfig)
        }
    }

    protected abstract fun initApp()

    protected fun registerAppInit(classInit: Class<out BaseAppInit>) {
        classInitList.add(classInit)
    }

    companion object {

        private var baseApp: Application? = null

        val app: Application
            get() = baseApp ?: throw IllegalArgumentException("Application 不能为空")

        fun setApp(app: Application) {
            baseApp = app
        }
    }
}
