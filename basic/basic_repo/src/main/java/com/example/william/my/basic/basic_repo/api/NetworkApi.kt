package com.example.william.my.basic.basic_repo.api

import com.example.william.my.basic.basic_repo.bean.LoginData
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
 *
 * 【分层定位与设计意图】：
 * 本接口属于“底层网络协议原语测试层”，不经过 Repository / ServiceLocator 封装，直接暴露 Retrofit 注解接口形态。
 * 专为验证网络底层通信能力提供原生 API：
 * 1. module_http：验证 OkHttp / Retrofit DSL、拦截器、Call 同步/异步、RxJava Single、协程挂起函数、Multipart 上传与文件下载等原始网络行为。
 * 2. module_kotlin：在 UseCase / ViewModel 中直接使用协程挂起与 Flow DSL 处理原生网络响应。
 *
 * 【访问约定】：
 * 外部各示例页面根据演示目标，自由使用 [com.example.william.my.core.retrofit.createApi] 或 [com.example.william.my.core.retrofit.rx.api.createRxApi] 动态创建实例。
 */
interface NetworkApi {

    /**
     * Call 回调方式登录请求。
     *
     * 供 module_http 的 RetrofitCallActivity、RetrofitCallDslActivity 调用。
     */
    @POST(Constants.Url_Login)
    fun loginCall(
        @Query(Constants.Key_Username) username: String,
        @Query(Constants.Key_Password) password: String
    ): Call<ResponseBody>

    /**
     * RxJava3 Single 响应式登录请求。
     *
     * 供 module_http 的 RetrofitRxActivity、RetrofitRxDslActivity 调用。
     */
    @POST(Constants.Url_Login)
    fun loginSingle(
        @Query(Constants.Key_Username) username: String,
        @Query(Constants.Key_Password) password: String
    ): Single<RetrofitResponse<LoginData>>

    /**
     * 文件下载请求。
     *
     * 供 module_http 的 RetrofitDownloadActivity 调用。
     */
    @GET
    fun downloadFile(@Url url: String): Call<ResponseBody>

    /**
     * MultipartBody 文件上传请求。
     *
     * 供 module_http 的 RetrofitUploadActivity 调用。
     */
    @POST
    fun uploadFile(@Url url: String, @Body body: MultipartBody): Call<ResponseBody>

    /**
     * 单文件 Multipart Part 上传请求。
     *
     * 供 module_http 的 RetrofitUploadActivity 调用。
     */
    @Multipart
    @POST
    fun uploadFile(@Url url: String, @Part part: MultipartBody.Part): Call<ResponseBody>

    /**
     * 多文件 Multipart Part 上传请求。
     *
     * 供 module_http 的 RetrofitUploadActivity 调用。
     */
    @Multipart
    @POST
    fun uploadFiles(@Url url: String, @Part parts: List<MultipartBody.Part>): Call<ResponseBody>

    // =============================================================================================

    /**
     * 协程挂起函数登录请求。
     *
     * 调用方：
     * - module_http：RetrofitCoroutineActivity、RetrofitCoroutineDslActivity
     * - module_kotlin：CoroutinesUseCase、FlowUseCase
     */
    @POST(Constants.Url_Login)
    suspend fun loginSuspend(
        @Query(Constants.Key_Username) username: String,
        @Query(Constants.Key_Password) password: String
    ): RetrofitResponse<LoginData>
}
