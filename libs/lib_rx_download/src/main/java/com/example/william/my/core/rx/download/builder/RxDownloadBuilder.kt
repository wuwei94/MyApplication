package com.example.william.my.core.rx.download.builder

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.example.william.my.core.rx.download.config.DownloadConfig
import com.example.william.my.core.rx.download.model.DownloadProgress
import com.example.william.my.core.rx.download.model.DownloadResult
import com.example.william.my.core.rx.download.request.RxDownloadRequest
import com.trello.lifecycle4.android.lifecycle.AndroidLifecycle
import com.trello.rxlifecycle4.LifecycleProvider
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.Retrofit
import java.io.File

/** Rx 下载请求构建器。 */
class RxDownloadBuilder internal constructor() {

    private var url: String? = null
    private var destination: File? = null
    private val headers = linkedMapOf<String, String>()
    private var resume = true
    private var retrofit: Retrofit? = null
    private var lifecycle: LifecycleProvider<Lifecycle.Event>? = null
    private var subscribeScheduler: Scheduler = Schedulers.io()
    private var observeScheduler: Scheduler? = null
    private var progressScheduler: Scheduler? = null
    private var progressIntervalMillis = DEFAULT_PROGRESS_INTERVAL_MILLIS
    private var onProgress: ((DownloadProgress) -> Unit)? = null
    private var onOperationStart: (() -> Boolean)? = null
    private var onFinally: (() -> Unit)? = null

    fun url(url: String) = apply { this.url = url }

    /** 与动态网络请求保持一致的 URL 配置别名。 */
    fun api(api: String) = url(api)

    fun destination(file: File) = apply { destination = file }

    fun destination(directory: File, fileName: String) = apply {
        destination = File(directory, fileName)
    }

    fun addHeader(name: String, value: String) = apply { headers[name] = value }

    fun addHeader(headers: Map<String, String>) = addHeaders(headers)

    fun addHeaders(headers: Map<String, String>) = apply {
        this.headers.clear()
        this.headers.putAll(headers)
    }

    fun resume(enabled: Boolean = true) = apply { resume = enabled }

    /** 为本次下载使用由应用层管理的 Retrofit 实例。 */
    fun retrofit(retrofit: Retrofit) = apply { this.retrofit = retrofit }

    /** 页面销毁时自动取消本次下载。 */
    fun setProvider(owner: LifecycleOwner) = apply {
        lifecycle = AndroidLifecycle.createLifecycleProvider(owner)
    }

    fun subscribeOn(scheduler: Scheduler) = apply { subscribeScheduler = scheduler }

    fun observeOn(scheduler: Scheduler) = apply { observeScheduler = scheduler }

    fun progressOn(scheduler: Scheduler) = apply { progressScheduler = scheduler }

    fun progressIntervalMillis(intervalMillis: Long) = apply {
        require(intervalMillis >= MIN_PROGRESS_INTERVAL_MILLIS) {
            "下载进度间隔不能小于 $MIN_PROGRESS_INTERVAL_MILLIS ms"
        }
        progressIntervalMillis = intervalMillis
    }

    fun onProgress(listener: (DownloadProgress) -> Unit) = apply { onProgress = listener }

    /** 底层网络与文件 I/O 真正退出后调用，包括成功、失败和取消。 */
    fun onFinally(action: () -> Unit) = apply { onFinally = action }

    internal fun onOperationStart(action: () -> Boolean) = apply {
        onOperationStart = action
    }

    fun build(): RxDownloadRequest = RxDownloadRequest(buildConfig())

    fun buildSingle(): Single<DownloadResult> = build().asSingle()

    internal fun buildConfig(): DownloadConfig {
        val requestUrl = requireNotNull(url) { "必须通过 api(...) 配置下载地址" }
        require(requestUrl.isNotBlank()) { "下载地址不能为空" }
        val target = requireNotNull(destination) { "必须通过 destination(...) 配置目标文件" }
        require(!target.isDirectory) { "目标路径不能是目录：${target.path}" }
        return DownloadConfig(
            url = requestUrl,
            destination = target,
            headers = headers.toMap(),
            resume = resume,
            retrofit = retrofit ?: RxDownloadRequest.defaultRetrofit,
            lifecycle = lifecycle,
            subscribeScheduler = subscribeScheduler,
            observeScheduler = observeScheduler,
            progressScheduler = progressScheduler,
            progressIntervalMillis = progressIntervalMillis,
            onProgress = onProgress,
            onOperationStart = onOperationStart,
            onFinally = onFinally,
        )
    }

    private companion object {
        const val DEFAULT_PROGRESS_INTERVAL_MILLIS = 100L
        const val MIN_PROGRESS_INTERVAL_MILLIS = 50L
    }
}
