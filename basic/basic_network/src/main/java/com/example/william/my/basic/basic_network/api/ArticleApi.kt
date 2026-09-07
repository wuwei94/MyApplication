package com.example.william.my.basic.basic_network.api

import com.example.william.my.basic.basic_network.model.ArticleData
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.core.retrofit.response.RetrofitResponse
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * 文章数据标准网络请求接口（Retrofit 协程 API，未装配 RxJava3CallAdapterFactory）。
 */
interface ArticleApi {

    /**
     * 协程挂起函数：执行网络请求并返回文章列表业务响应。
     */
    @GET(Constants.Url_Article_List)
    suspend fun getArticleSuspend(
        @Path("page") page: Int,
    ): RetrofitResponse<ArticleData>
}
