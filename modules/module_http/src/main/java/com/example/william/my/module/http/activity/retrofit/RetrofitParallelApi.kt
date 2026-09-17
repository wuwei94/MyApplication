package com.example.william.my.module.http.activity.retrofit

import com.example.william.my.basic.basic_shared.constant.Constants
import io.reactivex.rxjava3.core.Single
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

/**
 * Retrofit 平行页方法族接口 — 覆盖 GET / Form / JSON / Raw / Multipart 操作轴
 *
 * 同一资源路径分别暴露 Call / suspend / Single 返回值，
 * 供 Call / Coroutine / Rx 平行页按各自异步模型直调 Retrofit 公开 API。
 */
internal interface RetrofitParallelApi {

    @GET(Constants.Url_Article_List)
    fun getCall(@Path("page") page: Int): Call<ResponseBody>

    @FormUrlEncoded
    @POST(Constants.Url_Login)
    fun postFormCall(
        @Field(Constants.Key_Username) username: String,
        @Field(Constants.Key_Password) password: String,
    ): Call<ResponseBody>

    @POST(Constants.Url_Login)
    fun postJsonCall(@Body body: RequestBody): Call<ResponseBody>

    @POST(Constants.Url_Login)
    fun postRawCall(@Body body: RequestBody): Call<ResponseBody>

    @Multipart
    @POST(Constants.Url_Login)
    fun postMultipartCall(
        @Part(Constants.Key_Username) username: String,
        @Part(Constants.Key_Password) password: String,
    ): Call<ResponseBody>

    @GET(Constants.Url_Article_List)
    suspend fun getSuspend(@Path("page") page: Int): ResponseBody

    @FormUrlEncoded
    @POST(Constants.Url_Login)
    suspend fun postFormSuspend(
        @Field(Constants.Key_Username) username: String,
        @Field(Constants.Key_Password) password: String,
    ): ResponseBody

    @POST(Constants.Url_Login)
    suspend fun postJsonSuspend(@Body body: RequestBody): ResponseBody

    @POST(Constants.Url_Login)
    suspend fun postRawSuspend(@Body body: RequestBody): ResponseBody

    @Multipart
    @POST(Constants.Url_Login)
    suspend fun postMultipartSuspend(
        @Part(Constants.Key_Username) username: String,
        @Part(Constants.Key_Password) password: String,
    ): ResponseBody

    @GET(Constants.Url_Article_List)
    fun getSingle(@Path("page") page: Int): Single<ResponseBody>

    @FormUrlEncoded
    @POST(Constants.Url_Login)
    fun postFormSingle(
        @Field(Constants.Key_Username) username: String,
        @Field(Constants.Key_Password) password: String,
    ): Single<ResponseBody>

    @POST(Constants.Url_Login)
    fun postJsonSingle(@Body body: RequestBody): Single<ResponseBody>

    @POST(Constants.Url_Login)
    fun postRawSingle(@Body body: RequestBody): Single<ResponseBody>

    @Multipart
    @POST(Constants.Url_Login)
    fun postMultipartSingle(
        @Part(Constants.Key_Username) username: String,
        @Part(Constants.Key_Password) password: String,
    ): Single<ResponseBody>
}
