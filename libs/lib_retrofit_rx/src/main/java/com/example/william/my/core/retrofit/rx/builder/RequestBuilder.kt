package com.example.william.my.core.retrofit.rx.builder

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.example.william.my.core.okhttp.media.MediaType
import com.example.william.my.core.retrofit.rx.core.RxRetrofit
import com.example.william.my.core.retrofit.method.Method
import com.example.william.my.core.retrofit.response.RetrofitResponse
import com.trello.lifecycle4.android.lifecycle.AndroidLifecycle
import com.trello.rxlifecycle4.LifecycleProvider
import io.reactivex.rxjava3.core.Single
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File

class RequestBuilder<T> {

    internal lateinit var api: String
        private set
    internal var method: Method = Method.GET
        private set
    internal var header: MutableMap<String, String> = mutableMapOf()
        private set
    internal var parameter: MutableMap<String, String> = mutableMapOf()
        private set

    internal var jsonObject: JSONObject? = null
        private set
    internal var multipartBody: MultipartBody.Builder? = null
        private set

    internal var lifecycle: LifecycleProvider<Lifecycle.Event>? = null
        private set

    fun api(api: String): RequestBuilder<T> {
        this.api = api
        return this
    }

    fun get(): RequestBuilder<T> {
        method = Method.GET
        return this
    }

    fun post(): RequestBuilder<T> {
        method = Method.POST
        return this
    }

    fun delete(): RequestBuilder<T> {
        method = Method.DELETE
        return this
    }

    fun put(): RequestBuilder<T> {
        method = Method.PUT
        return this
    }

    fun addHeader(key: String, value: String): RequestBuilder<T> {
        header[key] = value
        return this
    }

    fun addHeader(header: MutableMap<String, String>): RequestBuilder<T> {
        this.header = header
        return this
    }

    fun addParam(key: String, value: String): RequestBuilder<T> {
        parameter[key] = value
        return this
    }

    fun addParams(params: MutableMap<String, String>): RequestBuilder<T> {
        this.parameter = params
        return this
    }

    fun addJsonObject(jsonObject: JSONObject): RequestBuilder<T> {
        this.jsonObject = jsonObject
        return this
    }

    fun addMultipart(key: String, value: String): RequestBuilder<T> {
        if (this.multipartBody == null) {
            this.multipartBody = MultipartBody.Builder().setType(MultipartBody.FORM)
        }
        this.multipartBody?.addFormDataPart(key, value)
        return this
    }

    fun addFile(key: String, file: File): RequestBuilder<T> {
        if (this.multipartBody == null) {
            this.multipartBody = MultipartBody.Builder().setType(MultipartBody.FORM)
        }
        this.multipartBody?.addFormDataPart(
            key, file.name, file.asRequestBody(MediaType.MEDIA_TYPE_MULTIPART)
        )
        return this
    }

    fun setProvider(owner: LifecycleOwner): RequestBuilder<T> {
        lifecycle = AndroidLifecycle.createLifecycleProvider(owner)
        return this
    }

    fun buildSingle(): Single<RetrofitResponse<T>> {
        return RxRetrofit(this).createResponse()
    }
}
