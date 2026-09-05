package com.example.william.my.core.okhttp.compat

import android.app.Application
import android.os.Environment
import com.example.william.my.core.okhttp.interceptor.InterceptorCacheRequest
import com.example.william.my.core.okhttp.interceptor.InterceptorCacheResponse
import com.example.william.my.core.okhttp.utils.NetworkCheck
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File

/**
 * 缓存配置
 */
object CompatCache {

    fun setCache(
        builder: OkHttpClient.Builder,
        app: Application,
        dirName: String = "cache",
        dirSize: Long = 10L * 1024L * 1024L,
    ) {
        val cacheFile = File(getCacheDir(app), dirName)
        val networkCheck = NetworkCheck(app)
        builder.cache(Cache(cacheFile, dirSize))
        builder.addInterceptor(InterceptorCacheRequest(networkCheck::isConnected))
        builder.addNetworkInterceptor(InterceptorCacheResponse())
    }

    fun setCache(
        builder: OkHttpClient.Builder,
        app: Application,
        dir: File,
        dirSize: Long = 10L * 1024L * 1024L,
    ) {
        val networkCheck = NetworkCheck(app)
        builder.cache(Cache(dir, dirSize))
        builder.addInterceptor(InterceptorCacheRequest(networkCheck::isConnected))
        builder.addNetworkInterceptor(InterceptorCacheResponse())
    }

    private fun getCacheDir(context: Application): File = if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
        // 外部存储可用
        context.externalCacheDir ?: context.cacheDir
    } else {
        // 外部存储不可用
        context.cacheDir
    }
}
