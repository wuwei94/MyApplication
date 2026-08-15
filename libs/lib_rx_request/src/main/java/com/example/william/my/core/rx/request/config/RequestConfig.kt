package com.example.william.my.core.rx.request.config

import androidx.lifecycle.Lifecycle
import com.example.william.my.core.rx.request.method.HttpMethod
import com.trello.rxlifecycle4.LifecycleProvider
import io.reactivex.rxjava3.core.Scheduler
import okhttp3.RequestBody
import retrofit2.Retrofit
import java.lang.reflect.Type

/**
 * 单次 RxRequest 的只读配置快照。
 */
internal class RequestConfig(
    val dataType: Type,
    val api: String,
    val method: HttpMethod,
    val header: Map<String, String>,
    val parameter: Map<String, String>,
    val requestBody: RequestBody?,
    val multipartBody: RequestBody?,
    val lifecycle: LifecycleProvider<Lifecycle.Event>?,
    val retrofit: Retrofit?,
    val observeScheduler: Scheduler?,
)
