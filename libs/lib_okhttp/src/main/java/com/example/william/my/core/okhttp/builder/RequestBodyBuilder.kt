package com.example.william.my.core.okhttp.builder

import com.example.william.my.core.okhttp.body.RequestBodyProgress
import com.example.william.my.core.okhttp.listener.RequestProgressListener
import com.example.william.my.core.okhttp.media.MediaType
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
 * 上传进度监听请使用 [com.example.william.my.core.okhttp.interceptor.InterceptorProgressUpload]。
 */
@Deprecated(
    message = "请直接使用 OkHttp 原生 API，进度监听使用 InterceptorProgressUpload",
    replaceWith = ReplaceWith(
        "FormBody.Builder()",
        "okhttp3.FormBody"
    )
)
class RequestBodyBuilder {

    private var mProgressListener: RequestProgressListener? = null

    @Deprecated(
        message = "请使用 InterceptorProgressUpload 配合 lambda 替代",
        replaceWith = ReplaceWith("InterceptorProgressUpload")
    )
    fun addListener(listener: RequestProgressListener) {
        mProgressListener = listener
    }

    private val mFormBuilder = FormBody.Builder()

    fun addForm(key: String, value: String): RequestBodyBuilder {
        mFormBuilder.add(key, value)
        return this
    }

    @Deprecated(
        message = "请使用 InterceptorProgressUpload 配合 lambda 替代",
        replaceWith = ReplaceWith("InterceptorProgressUpload")
    )
    fun buildForm(): RequestBody {
        return RequestBodyProgress(mFormBuilder.build(), mProgressListener)
    }

    private val mMultipartBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)

    fun addMultipart(key: String, value: String): RequestBodyBuilder {
        mMultipartBuilder.addFormDataPart(key, value)
        return this
    }

    fun addFile(name: String, file: File, fileName: String = file.name): RequestBodyBuilder {
        mMultipartBuilder.addFormDataPart(
            name,
            fileName,
            file.asRequestBody(MediaType.MEDIA_TYPE_MULTIPART)
        )
        return this
    }

    @Deprecated(
        message = "请使用 InterceptorProgressUpload 配合 lambda 替代",
        replaceWith = ReplaceWith("InterceptorProgressUpload")
    )
    fun buildMultipart(): RequestBody {
        return RequestBodyProgress(mMultipartBuilder.build(), mProgressListener)
    }

    private val mJsonBuilder = JSONObject()

    fun addJson(key: String, value: String): RequestBodyBuilder {
        mJsonBuilder.put(key, value)
        return this
    }

    @Deprecated(
        message = "请使用 InterceptorProgressUpload 配合 lambda 替代",
        replaceWith = ReplaceWith("InterceptorProgressUpload")
    )
    fun buildJson(): RequestBody {
        val body = mJsonBuilder.toString().toRequestBody(MediaType.MEDIA_TYPE_JSON)
        return RequestBodyProgress(body, mProgressListener)
    }
}
