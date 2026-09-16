package com.example.william.my.core.okhttp.builder

import com.example.william.my.core.okhttp.body.UploadProgressRequestBody
import com.example.william.my.core.okhttp.listener.RequestProgressListener
import com.example.william.my.core.okhttp.media.MediaTypes
import okhttp3.FormBody
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File

/**
 * 请求体构建器。
 *
 * 请直接使用 OkHttp 原生 [FormBody.Builder]、[MultipartBody.Builder]、[toRequestBody]，
 * 上传进度监听请使用 [com.example.william.my.core.okhttp.interceptor.InterceptorUploadProgress]。
 */
@Deprecated(
    message = "请直接使用 OkHttp 原生 API，进度监听使用 InterceptorUploadProgress",
    replaceWith = ReplaceWith(
        "FormBody.Builder()",
        "okhttp3.FormBody",
    ),
)
class RequestBodyBuilder {

    private var progressListener: RequestProgressListener? = null

    @Deprecated(
        message = "请使用 InterceptorUploadProgress 配合 lambda 替代",
        replaceWith = ReplaceWith("InterceptorUploadProgress"),
    )
    fun addListener(listener: RequestProgressListener) {
        progressListener = listener
    }

    private val formBuilder = FormBody.Builder()

    fun addForm(key: String, value: String): RequestBodyBuilder {
        formBuilder.add(key, value)
        return this
    }

    @Deprecated(
        message = "请使用 InterceptorUploadProgress 配合 lambda 替代",
        replaceWith = ReplaceWith("InterceptorUploadProgress"),
    )
    fun buildForm(): RequestBody = wrapWithProgress(formBuilder.build())

    private val multipartBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)

    fun addMultipart(key: String, value: String): RequestBodyBuilder {
        multipartBuilder.addFormDataPart(key, value)
        return this
    }

    fun addFile(name: String, file: File, fileName: String = file.name): RequestBodyBuilder {
        multipartBuilder.addFormDataPart(
            name,
            fileName,
            file.asRequestBody(MediaTypes.MEDIA_TYPE_MULTIPART),
        )
        return this
    }

    @Deprecated(
        message = "请使用 InterceptorUploadProgress 配合 lambda 替代",
        replaceWith = ReplaceWith("InterceptorUploadProgress"),
    )
    fun buildMultipart(): RequestBody = wrapWithProgress(multipartBuilder.build())

    private val jsonBuilder = JSONObject()

    fun addJson(key: String, value: String): RequestBodyBuilder {
        jsonBuilder.put(key, value)
        return this
    }

    @Deprecated(
        message = "请使用 InterceptorUploadProgress 配合 lambda 替代",
        replaceWith = ReplaceWith("InterceptorUploadProgress"),
    )
    fun buildJson(): RequestBody {
        val body = jsonBuilder.toString().toRequestBody(MediaTypes.MEDIA_TYPE_JSON)
        return wrapWithProgress(body)
    }

    private fun wrapWithProgress(body: RequestBody): RequestBody {
        val listener = progressListener ?: return body
        return UploadProgressRequestBody(body, listener::onProgress)
    }
}
