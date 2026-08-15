package com.example.william.my.core.rx.upload.builder

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.example.william.my.core.rx.upload.config.UploadConfig
import com.example.william.my.core.rx.upload.config.UploadFilePart
import com.example.william.my.core.rx.upload.model.UploadProgress
import com.example.william.my.core.rx.upload.model.UploadResult
import com.example.william.my.core.rx.upload.request.RxUploadRequest
import com.trello.lifecycle4.android.lifecycle.AndroidLifecycle
import com.trello.rxlifecycle4.LifecycleProvider
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.MediaType
import retrofit2.Retrofit
import java.io.File

/**
 * 固定使用 POST 的 Rx Multipart 文件上传构建器
 */
class RxUploadBuilder internal constructor() {

    private var url: String? = null
    private val headers = linkedMapOf<String, String>()
    private val formFields = mutableListOf<Pair<String, String>>()
    private val files = mutableListOf<UploadFilePart>()
    private var retrofit: Retrofit? = null
    private var lifecycle: LifecycleProvider<Lifecycle.Event>? = null
    private var subscribeScheduler: Scheduler = Schedulers.io()
    private var observeScheduler: Scheduler? = null
    private var progressScheduler: Scheduler? = null
    private var progressIntervalMillis = DEFAULT_PROGRESS_INTERVAL_MILLIS
    private var onProgress: ((UploadProgress) -> Unit)? = null
    private var onFinally: (() -> Unit)? = null

    fun url(url: String) = apply { this.url = url }

    /** 与动态网络请求保持一致的 URL 配置别名。 */
    fun api(api: String) = url(api)

    fun addHeader(name: String, value: String) = apply { headers[name] = value }

    fun addHeader(headers: Map<String, String>) = addHeaders(headers)

    fun addHeaders(headers: Map<String, String>) = apply {
        this.headers.clear()
        this.headers.putAll(headers)
    }

    fun addFormField(name: String, value: String) = apply {
        formFields += name to value
    }

    /** 与动态网络请求保持一致的 Multipart 表单参数配置别名。 */
    fun addParam(name: String, value: String) = addFormField(name, value)

    fun addFormFields(fields: Map<String, String>) = apply {
        formFields.clear()
        formFields += fields.entries.map { it.key to it.value }
    }

    fun addParams(params: Map<String, String>) = addFormFields(params)

    fun addFile(
        name: String,
        file: File,
        mediaType: MediaType? = null,
    ) = addFile(name, file.name, file, mediaType)

    fun addFile(
        name: String,
        fileName: String,
        file: File,
        mediaType: MediaType? = null,
    ) = apply {
        files += UploadFilePart(name, fileName, file, mediaType)
    }

    /** 使用同一个 Multipart 字段名批量追加文件。 */
    fun addFiles(
        name: String,
        files: Iterable<File>,
        mediaType: MediaType? = null,
    ) = apply {
        files.forEach { file -> addFile(name, file, mediaType) }
    }

    /** 为本次上传使用由应用层管理的 Retrofit 实例。 */
    fun retrofit(retrofit: Retrofit) = apply { this.retrofit = retrofit }

    /** 页面销毁时自动取消本次上传。 */
    fun setProvider(owner: LifecycleOwner) = apply {
        lifecycle = AndroidLifecycle.createLifecycleProvider(owner)
    }

    fun subscribeOn(scheduler: Scheduler) = apply { subscribeScheduler = scheduler }

    fun observeOn(scheduler: Scheduler) = apply { observeScheduler = scheduler }

    fun progressOn(scheduler: Scheduler) = apply { progressScheduler = scheduler }

    fun progressIntervalMillis(intervalMillis: Long) = apply {
        require(intervalMillis >= MIN_PROGRESS_INTERVAL_MILLIS) {
            "上传进度间隔不能小于 $MIN_PROGRESS_INTERVAL_MILLIS ms"
        }
        progressIntervalMillis = intervalMillis
    }

    fun onProgress(listener: (UploadProgress) -> Unit) = apply { onProgress = listener }

    /** 底层网络与文件 I/O 真正退出后调用，包括成功、失败和取消。 */
    fun onFinally(action: () -> Unit) = apply { onFinally = action }

    fun build(): RxUploadRequest = RxUploadRequest(buildConfig())

    fun buildSingle(): Single<UploadResult> = build().asSingle()

    internal fun buildConfig(): UploadConfig {
        val requestUrl = requireNotNull(url) { "必须通过 api(...) 配置上传地址" }
        require(requestUrl.isNotBlank()) { "上传地址不能为空" }
        require(files.isNotEmpty()) { "至少需要通过 addFile(...) 添加一个文件" }
        files.forEach { part ->
            require(part.name.isNotBlank()) { "文件字段名不能为空" }
            require(part.fileName.isNotBlank()) { "上传文件名不能为空" }
            require(part.file.isFile) { "上传文件不存在：${part.file.path}" }
        }
        val uploadRetrofit = RxUploadRequest.withoutConnectionFailureRetry(
            retrofit ?: RxUploadRequest.defaultRetrofit
        )
        return UploadConfig(
            url = requestUrl,
            headers = headers.toMap(),
            formFields = formFields.toList(),
            files = files.toList(),
            retrofit = uploadRetrofit,
            lifecycle = lifecycle,
            subscribeScheduler = subscribeScheduler,
            observeScheduler = observeScheduler,
            progressScheduler = progressScheduler,
            progressIntervalMillis = progressIntervalMillis,
            onProgress = onProgress,
            onFinally = onFinally,
        )
    }

    private companion object {
        const val DEFAULT_PROGRESS_INTERVAL_MILLIS = 100L
        const val MIN_PROGRESS_INTERVAL_MILLIS = 50L
    }
}
