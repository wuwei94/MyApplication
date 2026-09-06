package com.example.william.my.core.volley

import android.content.Context
import android.graphics.Bitmap
import android.util.LruCache
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyLog
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.Volley
import com.example.william.my.core.volley.stack.OkHttp3Stack

/**
 * Volley 单例（统一管理请求队列与图片加载器）
 */
class VolleySingleton(context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: VolleySingleton? = null
        fun getInstance(context: Context) = INSTANCE ?: synchronized(this) {
            INSTANCE ?: VolleySingleton(context).also {
                INSTANCE = it
            }
        }

        // ImageLoader 内存缓存大小
        private const val IMAGE_CACHE_SIZE = 20
    }

    init {
        VolleyLog.DEBUG = false
    }

    // true = OkHttp3Stack（OkHttp 通道），false = HurlStack（HttpURLConnection）
    private val useOkHttp = true

    val imageLoader: ImageLoader by lazy {
        ImageLoader(
            requestQueue,
            object : ImageLoader.ImageCache {
                private val cache = LruCache<String, Bitmap>(IMAGE_CACHE_SIZE)
                override fun getBitmap(url: String): Bitmap? = cache.get(url)

                override fun putBitmap(url: String, bitmap: Bitmap) {
                    cache.put(url, bitmap)
                }
            },
        )
    }

    private val requestQueue: RequestQueue by lazy {
        // 使用 applicationContext，避免因传入 Activity/BroadcastReceiver 而引发内存泄漏
        if (useOkHttp) {
            Volley.newRequestQueue(context.applicationContext, OkHttp3Stack())
        } else {
            Volley.newRequestQueue(context.applicationContext, HurlStack())
        }
    }

    fun <T> addToRequestQueue(req: Request<T>) {
        requestQueue.add(req)
    }

    fun cancel(tag: String) {
        requestQueue.cancelAll(tag)
    }
}
