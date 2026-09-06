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
package com.example.william.my.basic.basic_repo.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import com.example.william.my.basic.basic_repo.bean.ArticleData
import com.example.william.my.basic.basic_repo.bean.ArticleDetailData
import com.example.william.my.basic.basic_repo.data.result.NetworkResult
import com.example.william.my.basic.basic_repo.data.source.local.ArticleLocalDataSource
import com.example.william.my.basic.basic_repo.data.source.remote.ArticleRemoteDataSource
import com.example.william.my.core.retrofit.response.RetrofitResponse
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.rx3.asFlow

/**
 * 文章数据仓库默认实现（[ArticleRepository]），服务于 module_arch 架构模式。
 *
 * 遵循现代 Android 协程架构设计原则：挂起函数应保证主线程安全（Suspend functions should be main-safe）。
 * 底层数据源（Room DAO、Retrofit）已自行调度并在后台线程执行耗时与 I/O 操作，因此 Repository 层无需重复注入 CoroutineDispatcher 进行多余的上下文切换。
 */
class DefaultArticleRepository(
    private val articlesRemoteDataSource: ArticleRemoteDataSource,
    private val articlesLocalDataSource: ArticleLocalDataSource,
) : ArticleRepository {

    // ─────────────────────────────────────────────────────────────────────────
    // 1. 传统回调 API（教学对比演示）
    // ─────────────────────────────────────────────────────────────────────────
    override fun getArticleCallback(
        page: Int,
        callback: ArticleRepository.LoadArticleCallback,
    ) {
        articlesRemoteDataSource.getArticleCallback(
            page,
            object : ArticleRemoteDataSource.LoadArticleCallback {
                override fun onArticleLoaded(articles: List<ArticleDetailData>) {
                    callback.onArticleLoaded(articles)
                }

                override fun onDataNotAvailable() {
                    callback.onDataNotAvailable()
                }
            },
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. 基础请求 API（Single 与 挂起函数）
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * RxJava3 Single：执行网络请求并返回单次响应流。
     */
    override fun getArticleSingle(page: Int): Single<RetrofitResponse<ArticleData>> = articlesRemoteDataSource.getArticleSingle(page)

    /**
     * 协程挂起函数：执行网络请求并返回业务响应。
     */
    override suspend fun getArticleSuspend(page: Int): RetrofitResponse<ArticleData> = articlesRemoteDataSource.getArticleSuspend(page)

    // ─────────────────────────────────────────────────────────────────────────
    // 3. LiveData 响应式数据流及互转 API
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * 官方 liveData 协程构建器：通过 liveData(Dispatchers.IO) { ... } 构建生命周期感知的 LiveData。
     */
    override fun getArticleLiveData(page: Int): LiveData<RetrofitResponse<ArticleData>> = liveData(Dispatchers.IO) {
        emit(RetrofitResponse.loading())
        try {
            val response = articlesRemoteDataSource.getArticleSuspend(page)
            emit(response)
        } catch (e: Exception) {
            emit(RetrofitResponse.error(e.message ?: "网络请求失败"))
        }
    }

    /**
     * RxJava 转 LiveData：遵循 ReactiveStreams 规范将 Single 桥接为 LiveData。
     */
    override fun getArticleLiveDataByRx(page: Int): LiveData<RetrofitResponse<ArticleData>> = articlesRemoteDataSource.getArticleSingle(page)
        .toObservable()
        .asFlow()
        .asLiveData()

    /**
     * Flow 转 LiveData：通过 asLiveData() 将 Kotlin Flow 桥接为 LiveData。
     */
    override fun getArticleLiveDataByFlow(page: Int): LiveData<RetrofitResponse<ArticleData>> = getArticleFlow(page)
        .asLiveData()

    // ─────────────────────────────────────────────────────────────────────────
    // 4. Flow 响应式数据流及互转 API
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * 纯协程 Flow 构建：通过 flow { ... } 发送 loading/error/success 状态。
     */
    override fun getArticleFlow(page: Int): Flow<RetrofitResponse<ArticleData>> = flow {
        emit(RetrofitResponse.loading())
        val response = articlesRemoteDataSource.getArticleSuspend(page)
        emit(response)
    }.catch { e ->
        emit(RetrofitResponse.error(e.message ?: "网络请求失败"))
    }.flowOn(Dispatchers.IO)

    /**
     * RxJava 转 Flow：将 Single 转换为 Kotlin 响应式 Flow (asFlow)。
     */
    override fun getArticleFlowByRx(page: Int): Flow<RetrofitResponse<ArticleData>> = articlesRemoteDataSource.getArticleSingle(page)
        .toObservable()
        .asFlow()

    /**
     * LiveData 转 Flow：将 LiveData 转换为 Kotlin 响应式 Flow (asFlow)。
     */
    override fun getArticleFlowByLiveData(page: Int): Flow<RetrofitResponse<ArticleData>> = getArticleLiveData(page)
        .asFlow()

    // ─────────────────────────────────────────────────────────────────────────
    // 5. 本地持久化与数据仓库业务 API
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * 获取文章列表业务结果（支持离线优先本地缓存与自动回退）。
     *
     * 策略说明：
     * 1. [forceUpdate] 为 true：强制从远端拉取最新数据存入本地，再返回本地数据；
     * 2. [forceUpdate] 为 false：优先读取本地缓存；若本地有数据则直接返回（秒开）；若本地无缓存或查询失败，则自动回退至远端拉取、写入缓存并返回。
     *
     * @param page 页码（首页通常为 0）。
     * @param forceUpdate 是否强制从远端拉取最新数据并更新本地缓存。
     * @return 包含文章列表的数据结果封装 [NetworkResult]。
     */
    override suspend fun getArticleResult(
        page: Int,
        forceUpdate: Boolean,
    ): NetworkResult<List<ArticleDetailData>> {
        if (forceUpdate) {
            try {
                updateArticlesFromRemoteDataSource(page)
            } catch (ex: Exception) {
                return NetworkResult.Error(ex)
            }
        } else {
            val localResult = articlesLocalDataSource.getArticleResult(page)
            if (localResult is NetworkResult.Success && localResult.data.isNotEmpty()) {
                return localResult
            }
            // 本地无缓存或为空，自动回退拉取远端数据并缓存
            try {
                updateArticlesFromRemoteDataSource(page)
            } catch (ex: Exception) {
                return NetworkResult.Error(ex)
            }
        }
        return articlesLocalDataSource.getArticleResult(page)
    }

    /**
     * 从远端数据源拉取最新文章并同步至本地缓存。
     *
     * 同步策略：
     * - 下拉刷新首页（page <= 0）：清空历史全量缓存，存入首页数据；
     * - 加载更多页（page > 0）：利用 REPLACE 策略增量写入当前页数据，保留前序页历史缓存。
     */
    private suspend fun updateArticlesFromRemoteDataSource(page: Int) {
        val remoteArticles = articlesRemoteDataSource.getArticleResult(page)
        if (remoteArticles is NetworkResult.Success) {
            // 下拉刷新首页（page <= 0）时清空历史全量缓存；加载更多页时使用 REPLACE 策略增量保存当前页数据
            if (page <= 0) {
                articlesLocalDataSource.deleteAllArticles()
            }
            articlesLocalDataSource.saveArticles(remoteArticles.data)
        } else if (remoteArticles is NetworkResult.Error) {
            throw remoteArticles.exception
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. 现代离线优先与 SSOT 单一真实数据源 API（对齐 Now in Android）
    // ─────────────────────────────────────────────────────────────────────────
    override fun getArticlesStream(): Flow<List<ArticleDetailData>> = articlesLocalDataSource.getArticlesStream()

    override fun getArticleCountStream(): Flow<Int> = articlesLocalDataSource.getArticleCountStream()

    override suspend fun syncArticles(page: Int): Result<Unit> = runCatching {
        val remoteResult = articlesRemoteDataSource.getArticleResult(page)
        when (remoteResult) {
            is NetworkResult.Success -> {
                if (page <= 0) {
                    articlesLocalDataSource.deleteAllArticles()
                }
                articlesLocalDataSource.saveArticles(remoteResult.data)
            }
            is NetworkResult.Error -> {
                throw remoteResult.exception
            }
            is NetworkResult.Loading -> Unit
        }
    }

    override suspend fun insertLocalArticle(title: String) {
        val localArticle = ArticleDetailData(
            id = System.currentTimeMillis().toString(),
            title = title,
            link = "https://www.wanandroid.com",
            page = 0,
        )
        articlesLocalDataSource.saveArticle(localArticle)
    }

    override suspend fun clearLocalArticles() {
        articlesLocalDataSource.deleteAllArticles()
    }
}
