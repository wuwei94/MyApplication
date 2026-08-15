package com.example.william.my.core.rx.upload.request

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.Url

/** Retrofit POST 上传接口。 */
internal interface UploadApi {

    @POST
    fun upload(
        @Url url: String,
        @HeaderMap headers: Map<String, String>,
        @Body body: RequestBody,
    ): Call<ResponseBody>
}
