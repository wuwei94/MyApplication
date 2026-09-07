package com.example.william.my.basic.basic_network.api

import com.example.william.my.basic.basic_network.model.ArticleData
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.core.retrofit.response.RetrofitResponse
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * 文章数据 RxJava 响应式网络请求接口（装配了 RxJava3CallAdapterFactory）。
 */
interface ArticleRxApi {

    /**
     * RxJava3 Single：执行网络请求并返回文章列表响应流。
     */
    @GET(Constants.Url_Article_List)
    fun getArticleSingle(
        @Path("page") page: Int,
    ): Single<RetrofitResponse<ArticleData>>
}
