package com.example.william.my.basic.basic_repo.api

import com.example.william.my.basic.basic_repo.bean.ArticleData
import com.example.william.my.basic.basic_shared.base.Constants
import com.example.william.my.core.retrofit.response.RetrofitResponse
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * 文章数据 RxJava 响应式网络请求接口（装配了 RxJava3CallAdapterFactory）。
 *
 * 统一由 [com.example.william.my.basic.basic_repo.data.ServiceLocator.provideArticleRxApi] 使用 `createRxApi` 创建并管理单例实例。
 *
 * 适用场景：
 * 1. basic_repo：[com.example.william.my.basic.basic_repo.data.source.remote.ArticleRemoteDataSourceImpl] 的 RxJava 响应式流调用。
 * 2. module_jetpack：Paging 3 RxJava 分页组件（ArticleRxPagingSource、ArticleRxRemoteMediator）。
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
