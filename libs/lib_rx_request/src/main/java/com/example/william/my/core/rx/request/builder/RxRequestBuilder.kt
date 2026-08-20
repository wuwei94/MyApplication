package com.example.william.my.core.rx.request.builder

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.example.william.my.core.okhttp.media.MediaTypes
import com.example.william.my.core.retrofit.response.RetrofitResponse
import com.example.william.my.core.rx.request.RxRequest
import com.example.william.my.core.rx.request.config.RequestConfig
import com.example.william.my.core.rx.request.method.HttpMethod
import com.trello.lifecycle4.android.lifecycle.AndroidLifecycle
import com.trello.rxlifecycle4.LifecycleProvider
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Retrofit
import java.io.File
import java.lang.reflect.Type

class RxRequestBuilder<T> @PublishedApi internal constructor(
    private val dataType: Type,
) {

    private var api: String? = null
    private var method: HttpMethod = HttpMethod.GET
    private val header = mutableMapOf<String, String>()
    private val parameter = mutableMapOf<String, String>()

    private var jsonObject: JSONObject? = null
    private var requestBody: RequestBody? = null
    private var multipartBody: MultipartBody.Builder? = null

    private var lifecycle: LifecycleProvider<Lifecycle.Event>? = null
    private var retrofit: Retrofit? = null
    private var observeScheduler: Scheduler? = null

    fun api(api: String): RxRequestBuilder<T> {
        this.api = api
        return this
    }

    fun get(): RxRequestBuilder<T> {
        method = HttpMethod.GET
        return this
    }

    fun post(): RxRequestBuilder<T> {
        method = HttpMethod.POST
        return this
    }

    fun put(): RxRequestBuilder<T> {
        method = HttpMethod.PUT
        return this
    }

    fun patch(): RxRequestBuilder<T> {
        method = HttpMethod.PATCH
        return this
    }

    fun delete(): RxRequestBuilder<T> {
        method = HttpMethod.DELETE
        return this
    }

    fun addHeader(key: String, value: String): RxRequestBuilder<T> {
        header[key] = value
        return this
    }

    fun addHeader(header: Map<String, String>): RxRequestBuilder<T> {
        this.header.putAll(header)
        return this
    }

    fun addHeaders(headers: Map<String, String>): RxRequestBuilder<T> {
        this.header.putAll(headers)
        return this
    }

    fun setHeaders(headers: Map<String, String>): RxRequestBuilder<T> {
        this.header.clear()
        this.header.putAll(headers)
        return this
    }

    fun addParam(key: String, value: String): RxRequestBuilder<T> {
        parameter[key] = value
        return this
    }

    fun addParams(params: Map<String, String>): RxRequestBuilder<T> {
        this.parameter.putAll(params)
        return this
    }

    fun setParams(params: Map<String, String>): RxRequestBuilder<T> {
        this.parameter.clear()
        this.parameter.putAll(params)
        return this
    }

    fun addJsonObject(jsonObject: JSONObject): RxRequestBuilder<T> {
        this.jsonObject = jsonObject
        this.requestBody = null
        this.multipartBody = null
        return this
    }

    fun addBody(requestBody: RequestBody): RxRequestBuilder<T> {
        this.requestBody = requestBody
        this.jsonObject = null
        this.multipartBody = null
        return this
    }

    fun addRawBody(value: String): RxRequestBuilder<T> {
        return addBody(value.toRequestBody(null))
    }

    fun addJsonBody(value: String): RxRequestBuilder<T> {
        return addBody(value.toRequestBody(MediaTypes.MEDIA_TYPE_JSON))
    }

    fun addJsonBody(value: JSONObject): RxRequestBuilder<T> {
        return addJsonBody(value.toString())
    }

    fun addMultipartField(key: String, value: String): RxRequestBuilder<T> {
        clearRawBody()
        if (this.multipartBody == null) {
            this.multipartBody = MultipartBody.Builder().setType(MultipartBody.FORM)
        }
        this.multipartBody?.addFormDataPart(key, value)
        return this
    }

    fun addMultipartFields(fields: Map<String, String>): RxRequestBuilder<T> {
        fields.forEach { (key, value) -> addMultipartField(key, value) }
        return this
    }

    @JvmOverloads
    fun addFile(
        key: String,
        file: File,
        mediaType: MediaType = DEFAULT_FILE_MEDIA_TYPE,
    ): RxRequestBuilder<T> {
        clearRawBody()
        if (this.multipartBody == null) {
            this.multipartBody = MultipartBody.Builder().setType(MultipartBody.FORM)
        }
        this.multipartBody?.addFormDataPart(
            key,
            file.name,
            file.asRequestBody(mediaType),
        )
        return this
    }

    fun setProvider(owner: LifecycleOwner): RxRequestBuilder<T> {
        lifecycle = AndroidLifecycle.createLifecycleProvider(owner)
        return this
    }

    /** 为本次请求使用传入的独立或命名 Retrofit 实例。 */
    fun retrofit(retrofit: Retrofit): RxRequestBuilder<T> {
        this.retrofit = retrofit
        return this
    }

    /** 覆盖 Android 主线程观察者，主要用于 Worker 与 JVM 测试。 */
    fun observeOn(scheduler: Scheduler): RxRequestBuilder<T> {
        observeScheduler = scheduler
        return this
    }

    fun buildSingle(): Single<RetrofitResponse<T>> {
        return RxRequest(this).createResponse()
    }

    internal fun buildConfig(): RequestConfig {
        val requestApi = api
        check(!requestApi.isNullOrBlank()) {
            "Request API must be configured with api(...) before buildSingle()"
        }
        val body = requestBody ?: jsonObject
            ?.toString()
            ?.toRequestBody(MediaTypes.MEDIA_TYPE_JSON)
        val multipart = multipartBody?.build()
        require(method != HttpMethod.GET || body == null) {
            "GET 请求不支持 RequestBody"
        }
        val supportsMultipart = method == HttpMethod.POST ||
            method == HttpMethod.PUT ||
            method == HttpMethod.PATCH
        require(multipart == null || supportsMultipart) {
            "Multipart 请求仅支持 POST、PUT 或 PATCH"
        }
        return RequestConfig(
            dataType = dataType,
            api = requestApi,
            method = method,
            header = header.toMap(),
            parameter = parameter.toMap(),
            requestBody = body,
            multipartBody = multipart,
            lifecycle = lifecycle,
            retrofit = retrofit,
            observeScheduler = observeScheduler,
        )
    }

    private fun clearRawBody() {
        jsonObject = null
        requestBody = null
    }

    private companion object {
        val DEFAULT_FILE_MEDIA_TYPE = "application/octet-stream".toMediaType()
    }
}
