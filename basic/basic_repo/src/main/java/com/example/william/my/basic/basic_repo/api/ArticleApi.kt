package com.example.william.my.basic.basic_repo.api

import com.example.william.my.basic.basic_repo.bean.ArticleData
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.core.retrofit.response.RetrofitResponse
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * 文章数据标准网络请求接口（Retrofit 协程 API，未装配 RxJava3CallAdapterFactory）。
 *
 * 统一由 [com.example.william.my.basic.basic_repo.data.ServiceLocator.provideArticleApi] 使用标准 `createApi` 创建并管理单例实例。
 *
 * 适用场景：
 * 1. basic_repo：[com.example.william.my.basic.basic_repo.data.source.remote.ArticleRemoteDataSourceImpl] 的协程挂起函数调用。
 * 2. module_jetpack：Paging 3 协程分页组件（ArticlePagingSource、ArticleRemoteMediator）。
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