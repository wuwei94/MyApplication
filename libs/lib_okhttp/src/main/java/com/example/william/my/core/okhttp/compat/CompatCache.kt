package com.example.william.my.core.okhttp.compat

import android.app.Application
import android.os.Environment
import com.example.william.my.core.okhttp.interceptor.InterceptorCache
import com.example.william.my.core.okhttp.utils.NetworkCheck
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File

object CompatCache {

    fun setCache(
        builder: OkHttpClient.Builder,
        app: Application,
        dirName: String = "cache",
        dirSize: Long = 10L * 1024L * 1024L
    ) {
        val cacheFile = File(getCacheDir(app), dirName)
        builder.cache(Cache(cacheFile, dirSize))
        builder.addNetworkInterceptor(InterceptorCache(NetworkCheck(app)))
    }

    fun setCache(
        builder: OkHttpClient.Builder,
        app: Application,
        dir: File,
        dirSize: Long = 10L * 1024L * 1024L
    ) {
        builder.cache(Cache(dir, dirSize))
        builder.addNetworkInterceptor(InterceptorCache(NetworkCheck(app)))
    }

    private fun getCacheDir(context: Application): File {
        return if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            //外部存储可用
            context.externalCacheDir ?: context.cacheDir
        } else {
            //外部存储不可用
            context.cacheDir
        }
    }
}
