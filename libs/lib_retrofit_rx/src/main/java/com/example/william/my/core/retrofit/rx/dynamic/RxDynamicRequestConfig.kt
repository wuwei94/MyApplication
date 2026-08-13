package com.example.william.my.core.retrofit.rx.dynamic

import androidx.lifecycle.Lifecycle
import com.trello.rxlifecycle4.LifecycleProvider
import io.reactivex.rxjava3.core.Scheduler
import okhttp3.RequestBody
import retrofit2.Retrofit
import java.lang.reflect.Type

/**
 * 单次 RxDynamicRequest 的只读配置快照。
 */
internal class RxDynamicRequestConfig(
    val dataType: Type,
    val api: String,
    val method: RxDynamicHttpMethod,
    val header: Map<String, String>,
    val parameter: Map<String, String>,
    val requestBody: RequestBody?,
    val multipartBody: RequestBody?,
    val lifecycle: LifecycleProvider<Lifecycle.Event>?,
    val retrofit: Retrofit?,
    val observeScheduler: Scheduler?,
)
