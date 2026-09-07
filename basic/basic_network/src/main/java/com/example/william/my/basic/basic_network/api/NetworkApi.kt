package com.example.william.my.basic.basic_network.api

import com.example.william.my.basic.basic_network.model.LoginData
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.core.retrofit.response.RetrofitResponse
import io.reactivex.rxjava3.core.Single
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * 通用网络请求接口（Retrofit API）—— **网络原语与通信测试层**。
 */
interface NetworkApi {

    @POST(Constants.Url_Login)
    fun loginCall(
        @Query(Constants.Key_Username) username: String,
        @Query(Constants.Key_Password) password: String,
    ): Call<ResponseBody>

    @POST(Constants.Url_Login)
    fun loginSingle(
        @Query(Constants.Key_Username) username: String,
        @Query(Constants.Key_Password) password: String,
    ): Single<RetrofitResponse<LoginData>>

    @GET
    fun downloadFile(@Url url: String): Call<ResponseBody>

    @POST
    fun uploadFile(@Url url: String, @Body body: MultipartBody): Call<ResponseBody>

    @Multipart
    @POST
    fun uploadFile(@Url url: String, @Part part: MultipartBody.Part): Call<ResponseBody>

    @Multipart
    @POST
    fun uploadFiles(@Url url: String, @Part parts: List<MultipartBody.Part>): Call<ResponseBody>

    @POST(Constants.Url_Login)
    suspend fun loginSuspend(
        @Query(Constants.Key_Username) username: String,
        @Query(Constants.Key_Password) password: String,
    ): RetrofitResponse<LoginData>
}
