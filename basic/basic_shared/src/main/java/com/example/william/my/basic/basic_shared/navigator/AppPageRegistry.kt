package com.example.william.my.basic.basic_shared.navigator

import android.app.Activity
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 全局页面与拦截器注册表
 *
 * 支持由 Hilt `@IntoMap @StringKey` 在应用启动时批量注入路由表，
 * 也支持各模块在初始化阶段显式调用 [registerActivity] 补充注册。
 */
object AppPageRegistry {

    private val activityMap = ConcurrentHashMap<String, Class<out Activity>>()
    private val interceptors = CopyOnWriteArrayList<PageInterceptor>()

    fun registerAll(routes: Map<String, Class<out Activity>>) {
        activityMap.putAll(routes)
    }

    fun registerActivity(path: String, clazz: Class<out Activity>) {
        activityMap[path] = clazz
    }

    fun resolveActivity(path: String): Class<out Activity>? = activityMap[path]

    fun contains(path: String): Boolean = activityMap.containsKey(path)

    fun addInterceptor(interceptor: PageInterceptor) {
        if (!interceptors.contains(interceptor)) {
            interceptors.add(interceptor)
            interceptors.sortByDescending { it.priority }
        }
    }

    fun getInterceptors(): List<PageInterceptor> = interceptors.toList()
}
