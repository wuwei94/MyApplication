package com.example.william.my.core.rx.download.request

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Streaming
import retrofit2.http.Url

/** Retrofit 流式下载接口。 */
internal interface DownloadApi {

    @Streaming
    @GET
    fun download(
        @Url url: String,
        @HeaderMap headers: Map<String, String>,
    ): Call<ResponseBody>
}
