/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.william.my.basic.basic_repo.data.source

import androidx.lifecycle.LiveData
import com.example.william.my.basic.basic_repo.bean.ArticleData
import com.example.william.my.basic.basic_repo.bean.ArticleDetailData
import com.example.william.my.basic.basic_repo.data.NetworkResult
import com.example.william.my.core.retrofit.response.RetrofitResponse
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

/**
 * Default implementation of [ArticleRepository]. Single entry point for managing Articles' data.
 */
class DefaultArticleRepository(
    private val articlesRemoteDataSource: ArticleDataSource<ArticleData, ArticleDetailData>,
    private val articlesLocalDataSource: ArticleDataSource<ArticleData, ArticleDetailData>,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ArticleRepository<ArticleData, ArticleDetailData> {

    // =========================================================================
    // 1. 传统回调 API（教学对比演示）
    // =========================================================================

    override fun getArticle(
        page: Int,
        callback: ArticleRepository.LoadArticleCallback<ArticleDetailData>
    ) {
        articlesRemoteDataSource.getArticleCallback(
            page, object : ArticleDataSource.LoadArticleCallback<ArticleDetailData> {
                override fun onArticleLoaded(articles: List<ArticleDetailData>) {
                    callback.onArticleLoaded(articles)
                }

                override fun onDataNotAvailable() {
                    callback.onDataNotAvailable()
                }
            })
    }

    // =========================================================================
    // 2. 基础请求 API（Single 与 挂起函数）
    // =========================================================================

    /**
     * RxJava3 Single：执行网络请求并返回单次响应流。
     */
    override fun getArticleSingle(page: Int): Single<RetrofitResponse<ArticleData>> {
        return articlesRemoteDataSource.getArticleSingle(page)
    }

    /**
     * 协程挂起函数：执行网络请求并返回业务响应。
     */
    override suspend fun getArticleSuspend(page: Int): RetrofitResponse<ArticleData> {
        return articlesRemoteDataSource.getArticleSuspend(page)
    }

    // =========================================================================
    // 3. Flow 响应式数据流及互转 API
    // =========================================================================

    /**
     * RxJava 转 Flow：将 Single 转换为 Kotlin 响应式 Flow (asFlow)。
     */
    override fun getArticleFlowByRx(page: Int): Flow<RetrofitResponse<ArticleData>> {
        return articlesRemoteDataSource.getArticleFlowByRx(page)
    }

    /**
     * 纯协程 Flow 构建：通过 flow { ... } 发送 loading/error/success 状态。
     */
    override fun getArticleFlow(page: Int): Flow<RetrofitResponse<ArticleData>> {
        return articlesRemoteDataSource.getArticleFlow(page)
    }

    /**
     * LiveData 转 Flow：将 LiveData 转换为 Kotlin 响应式 Flow (asFlow)。
     */
    override fun getArticleFlowByLiveData(page: Int): Flow<RetrofitResponse<ArticleData>> {
        return articlesRemoteDataSource.getArticleFlowByLiveData(page)
    }

    // =========================================================================
    // 4. LiveData 响应式数据流及互转 API
    // =========================================================================

    /**
     * RxJava 转 LiveData：遵循 ReactiveStreams 规范将 Single 桥接为 LiveData。
     */
    override fun getArticleLiveDataByRx(page: Int): LiveData<RetrofitResponse<ArticleData>> {
        return articlesRemoteDataSource.getArticleLiveDataByRx(page)
    }

    /**
     * 官方 liveData 协程构建器：通过 liveData(Dispatchers.IO) { ... } 构建生命周期感知的 LiveData。
     */
    override fun getArticleLiveData(page: Int): LiveData<RetrofitResponse<ArticleData>> {
        return articlesRemoteDataSource.getArticleLiveData(page)
    }

    /**
     * Flow 转 LiveData：通过 asLiveData() 将 Kotlin Flow 桥接为 LiveData。
     */
    override fun getArticleLiveDataByFlow(page: Int): LiveData<RetrofitResponse<ArticleData>> {
        return articlesRemoteDataSource.getArticleLiveDataByFlow(page)
    }

    override suspend fun getArticleResult(
        page: Int,
        forceUpdate: Boolean
    ): NetworkResult<List<ArticleDetailData>> {
        // Set app as busy while this function executes.
        if (forceUpdate) {
            try {
                updateArticlesFromRemoteDataSource(page)
            } catch (ex: Exception) {
                return NetworkResult.Error(ex)
            }
        }
        return articlesLocalDataSource.getArticleResult(page)
    }

    private suspend fun updateArticlesFromRemoteDataSource(page: Int) {
        val remoteArticles = articlesRemoteDataSource.getArticleResult(page)
        if (remoteArticles is NetworkResult.Success) {
            // Real apps might want to do a proper sync, deleting, modifying or adding each task.
            articlesLocalDataSource.deleteAllArticles()
            articlesLocalDataSource.saveArticles(remoteArticles.data)
        } else if (remoteArticles is NetworkResult.Error) {
            throw remoteArticles.exception
        }
    }
}
