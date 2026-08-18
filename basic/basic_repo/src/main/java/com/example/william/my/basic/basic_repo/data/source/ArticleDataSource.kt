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
import com.example.william.my.basic.basic_repo.data.NetworkResult
import com.example.william.my.core.retrofit.response.RetrofitResponse
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.flow.Flow

/**
 * Main entry point for accessing tasks data.
 */
interface ArticleDataSource<ArticleList, ArticleDetail> {

    interface LoadArticleCallback<ArticleDetail> {
        fun onArticleLoaded(articles: List<ArticleDetail>)
        fun onDataNotAvailable()
    }

    // =========================================================================
    // 1. 传统回调 API（教学对比演示）
    // =========================================================================

    /**
     * 传统异步回调方式加载数据。
     */
    fun getArticleCallback(
        page: Int, callback: LoadArticleCallback<ArticleDetail>
    ) {
        throw UnsupportedOperationException()
    }

    // =========================================================================
    // 2. 基础请求 API（Single 与 挂起函数）
    // =========================================================================

    /**
     * RxJava3 Single：执行网络请求并返回单次响应流。
     */
    fun getArticleSingle(page: Int): Single<RetrofitResponse<ArticleList>> {
        throw UnsupportedOperationException()
    }

    /**
     * 协程挂起函数：执行网络请求并返回业务响应。
     */
    suspend fun getArticleSuspend(page: Int): RetrofitResponse<ArticleList> {
        throw UnsupportedOperationException()
    }

    // =========================================================================
    // 3. Flow 响应式数据流及互转 API
    // =========================================================================

    /**
     * RxJava 转 Flow：将 Single 转换为 Kotlin 响应式 Flow (asFlow)。
     */
    fun getArticleFlowByRx(page: Int): Flow<RetrofitResponse<ArticleList>> {
        throw UnsupportedOperationException()
    }

    /**
     * 纯协程 Flow 构建：通过 flow { ... } 发送 loading/error/success 状态。
     */
    fun getArticleFlow(page: Int): Flow<RetrofitResponse<ArticleList>> {
        throw UnsupportedOperationException()
    }

    /**
     * LiveData 转 Flow：将 LiveData 转换为 Kotlin 响应式 Flow (asFlow)。
     */
    fun getArticleFlowByLiveData(page: Int): Flow<RetrofitResponse<ArticleList>> {
        throw UnsupportedOperationException()
    }

    // =========================================================================
    // 4. LiveData 响应式数据流及互转 API
    // =========================================================================

    /**
     * RxJava 转 LiveData：遵循 ReactiveStreams 规范将 Single 桥接为 LiveData。
     */
    fun getArticleLiveDataByRx(page: Int): LiveData<RetrofitResponse<ArticleList>> {
        throw UnsupportedOperationException()
    }

    /**
     * 官方 liveData 协程构建器：通过 liveData(Dispatchers.IO) { ... } 构建生命周期感知的 LiveData。
     */
    fun getArticleLiveData(page: Int): LiveData<RetrofitResponse<ArticleList>> {
        throw UnsupportedOperationException()
    }

    /**
     * Flow 转 LiveData：通过 asLiveData() 将 Kotlin Flow 桥接为 LiveData。
     */
    fun getArticleLiveDataByFlow(page: Int): LiveData<RetrofitResponse<ArticleList>> {
        throw UnsupportedOperationException()
    }

    // =========================================================================
    // 5. 本地持久化与数据仓库业务 API
    // =========================================================================

    suspend fun getArticleResult(page: Int): NetworkResult<List<ArticleDetail>> {
        throw UnsupportedOperationException()
    }

    suspend fun saveArticle(article: ArticleDetail) {
        throw UnsupportedOperationException()
    }

    suspend fun saveArticles(articles: List<ArticleDetail>) {
        throw UnsupportedOperationException()
    }

    suspend fun deleteAllArticles() {
        throw UnsupportedOperationException()
    }
}
