package com.example.william.my.core.okhttp.header

/**
 * 客户端控制 Header 常量
 *
 * 这些 Header 仅用于在客户端内部传递 BaseUrl 和缓存策略，发送请求前会被对应拦截器移除。
 */
object ControlHeaders {
    /** 指定请求使用的替代 BaseUrl。 */
    const val BASE_URL_REDIRECT = "OkHttp-Url-Redirect"

    /** 指定响应缓存的有效时长，单位为秒。 */
    const val CACHE_ALIVE_SECONDS = "OkHttp-Cache-Alive-Second"
}
