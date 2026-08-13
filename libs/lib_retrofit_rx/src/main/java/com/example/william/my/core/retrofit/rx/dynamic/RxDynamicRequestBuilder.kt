package com.example.william.my.core.retrofit.rx.dynamic

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.example.william.my.core.okhttp.media.MediaTypes
import com.example.william.my.core.retrofit.response.RetrofitResponse
import com.trello.lifecycle4.android.lifecycle.AndroidLifecycle
import com.trello.rxlifecycle4.LifecycleProvider
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Retrofit
import java.io.File
import java.lang.reflect.Type

class RxDynamicRequestBuilder<T> @PublishedApi internal constructor(
    private val dataType: Type,
) {

    private var api: String? = null
    private var method: RxDynamicHttpMethod = RxDynamicHttpMethod.GET
    private val header = mutableMapOf<String, String>()
    private val parameter = mutableMapOf<String, String>()

    private var jsonObject: JSONObject? = null
    private var requestBody: RequestBody? = null
    private var multipartBody: MultipartBody.Builder? = null

    private var lifecycle: LifecycleProvider<Lifecycle.Event>? = null
    private var retrofit: Retrofit? = null
    private var observeScheduler: Scheduler? = null

    fun api(api: String): RxDynamicRequestBuilder<T> {
        this.api = api
        return this
    }

    fun get(): RxDynamicRequestBuilder<T> {
        method = RxDynamicHttpMethod.GET
        return this
    }

    fun post(): RxDynamicRequestBuilder<T> {
        method = RxDynamicHttpMethod.POST
        return this
    }

    fun put(): RxDynamicRequestBuilder<T> {
        method = RxDynamicHttpMethod.PUT
        return this
    }

    fun patch(): RxDynamicRequestBuilder<T> {
        method = RxDynamicHttpMethod.PATCH
        return this
    }

    fun delete(): RxDynamicRequestBuilder<T> {
        method = RxDynamicHttpMethod.DELETE
        return this
    }

    fun addHeader(key: String, value: String): RxDynamicRequestBuilder<T> {
        header[key] = value
        return this
    }

    fun addHeader(header: Map<String, String>): RxDynamicRequestBuilder<T> {
        this.header.clear()
        this.header.putAll(header)
        return this
    }

    fun addParam(key: String, value: String): RxDynamicRequestBuilder<T> {
        parameter[key] = value
        return this
    }

    fun addParams(params: Map<String, String>): RxDynamicRequestBuilder<T> {
        this.parameter.clear()
        this.parameter.putAll(params)
        return this
    }

    fun addJsonObject(jsonObject: JSONObject): RxDynamicRequestBuilder<T> {
        this.jsonObject = jsonObject
        this.requestBody = null
        return this
    }

    fun addBody(requestBody: RequestBody): RxDynamicRequestBuilder<T> {
        this.requestBody = requestBody
        this.jsonObject = null
        return this
    }

    fun addRawBody(value: String): RxDynamicRequestBuilder<T> {
        return addBody(value.toRequestBody(null))
    }

    fun addJsonBody(value: String): RxDynamicRequestBuilder<T> {
        return addBody(value.toRequestBody(MediaTypes.MEDIA_TYPE_JSON))
    }

    fun addJsonBody(value: JSONObject): RxDynamicRequestBuilder<T> {
        return addJsonBody(value.toString())
    }

    fun addMultipart(key: String, value: String): RxDynamicRequestBuilder<T> {
        if (this.multipartBody == null) {
            this.multipartBody = MultipartBody.Builder().setType(MultipartBody.FORM)
        }
        this.multipartBody?.addFormDataPart(key, value)
        return this
    }

    fun addFile(key: String, file: File): RxDynamicRequestBuilder<T> {
        if (this.multipartBody == null) {
            this.multipartBody = MultipartBody.Builder().setType(MultipartBody.FORM)
        }
        this.multipartBody?.addFormDataPart(
            key, file.name, file.asRequestBody(MediaTypes.MEDIA_TYPE_MULTIPART)
        )
        return this
    }

    fun setProvider(owner: LifecycleOwner): RxDynamicRequestBuilder<T> {
        lifecycle = AndroidLifecycle.createLifecycleProvider(owner)
        return this
    }

    /** 为本次请求使用传入的独立或命名 Retrofit 实例。 */
    fun retrofit(retrofit: Retrofit): RxDynamicRequestBuilder<T> {
        this.retrofit = retrofit
        return this
    }

    /** 覆盖 Android 主线程观察者，主要用于 Worker 与 JVM 测试。 */
    fun observeOn(scheduler: Scheduler): RxDynamicRequestBuilder<T> {
        observeScheduler = scheduler
        return this
    }

    fun buildSingle(): Single<RetrofitResponse<T>> {
        return RxDynamicRequest(this).createResponse()
    }

    internal fun buildConfig(): RxDynamicRequestConfig {
        val requestApi = api
        check(!requestApi.isNullOrBlank()) {
            "Request API must be configured with api(...) before buildSingle()"
        }
        val body = requestBody ?: jsonObject
            ?.toString()
            ?.toRequestBody(MediaTypes.MEDIA_TYPE_JSON)
        return RxDynamicRequestConfig(
            dataType = dataType,
            api = requestApi,
            method = method,
            header = header.toMap(),
            parameter = parameter.toMap(),
            requestBody = body,
            multipartBody = multipartBody?.build(),
            lifecycle = lifecycle,
            retrofit = retrofit,
            observeScheduler = observeScheduler,
        )
    }
}
