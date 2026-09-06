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
import com.example.william.my.basic.basic_repo.bean.ArticleData
import com.example.william.my.basic.basic_repo.bean.ArticleDetailData
import com.example.william.my.basic.basic_repo.data.result.NetworkResult
import com.example.william.my.core.retrofit.response.RetrofitResponse
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.flow.Flow

/**
 * 文章数据仓库接口（服务于 module_arch 的 MVP / MVVM / MVI 与 Mavericks 架构模式教学对比演示）。
 *
 * 架构对应关系：
 * 1. MVP           -> 传统回调 API ([getArticleCallback])
 * 2. MVVM          -> 基础请求与响应式流 API ([getArticleSingle], [getArticleSuspend], [getArticleLiveData] 等)
 * 3. MVI           -> Flow 响应式数据流 API ([getArticleFlow] 等)
 * 4. Mavericks     -> 挂起函数 API ([getArticleSuspend])，由专用的 [ArticleMavericksRepository] 包装驱动状态
 * 5. Offline-First -> Room 响应式流 ([getArticlesStream]) + 网络写同步 ([syncArticles])，实现单一真实数据源（SSOT）
 */
interface ArticleRepository {

    interface LoadArticleCallback {
        fun onArticleLoaded(articles: List<ArticleDetailData>)
        fun onDataNotAvailable()
    }

    // =========================================================================
    // 1. 传统回调 API（教学对比演示）
    // =========================================================================

    /**
     * 传统异步回调方式加载数据。
     */
    fun getArticleCallback(page: Int, callback: LoadArticleCallback)

    // =========================================================================
    // 2. 基础请求 API（Single 与 挂起函数）
    // =========================================================================

    /**
     * RxJava3 Single：执行网络请求并返回单次响应流。
     */
    fun getArticleSingle(page: Int): Single<RetrofitResponse<ArticleData>>

    /**
     * 协程挂起函数：执行网络请求并返回业务响应。
     */
    suspend fun getArticleSuspend(page: Int): RetrofitResponse<ArticleData>

    // =========================================================================
    // 3. LiveData 响应式数据流及互转 API
    // =========================================================================

    /**
     * 官方 liveData 协程构建器：通过 liveData(Dispatchers.IO) { ... } 构建生命周期感知的 LiveData。
     */
    fun getArticleLiveData(page: Int): LiveData<RetrofitResponse<ArticleData>>

    /**
     * RxJava 转 LiveData：遵循 ReactiveStreams 规范将 Single 桥接为 LiveData。
     */
    fun getArticleLiveDataByRx(page: Int): LiveData<RetrofitResponse<ArticleData>>

    /**
     * Flow 转 LiveData：通过 asLiveData() 将 Kotlin Flow 桥接为 LiveData。
     */
    fun getArticleLiveDataByFlow(page: Int): LiveData<RetrofitResponse<ArticleData>>

    // =========================================================================
    // 4. Flow 响应式数据流及互转 API
    // =========================================================================

    /**
     * 纯协程 Flow 构建：通过 flow { ... } 发送 loading/error/success 状态。
     */
    fun getArticleFlow(page: Int): Flow<RetrofitResponse<ArticleData>>

    /**
     * RxJava 转 Flow：将 Single 转换为 Kotlin 响应式 Flow (asFlow)。
     */
    fun getArticleFlowByRx(page: Int): Flow<RetrofitResponse<ArticleData>>

    /**
     * LiveData 转 Flow：将 LiveData 转换为 Kotlin 响应式 Flow (asFlow)。
     */
    fun getArticleFlowByLiveData(page: Int): Flow<RetrofitResponse<ArticleData>>

    // =========================================================================
    // 5. 本地持久化与数据仓库业务 API
    // =========================================================================

    /**
     * 获取文章列表业务结果（支持本地持久化与强制刷新）。
     *
     * @param page 页码（首页通常为 0）。
     * @param forceUpdate 是否强制从远端拉取最新数据并更新本地缓存。
     * @return 包含文章列表的数据结果封装 [NetworkResult]。
     */
    suspend fun getArticleResult(
        page: Int,
        forceUpdate: Boolean = false,
    ): NetworkResult<List<ArticleDetailData>>

    // =========================================================================
    // 6. 现代离线优先与 SSOT 单一真实数据源 API（对齐 Now in Android）
    // =========================================================================

    /**
     * SSOT 唯一数据源读流：观察 Room 本地数据库中的全部文章列表。
     *
     * 遵循 Google 离线优先架构（Offline-first architecture）规范：
     * UI 层永远且仅观察本地数据库的数据流，不直接消费网络请求结果。
     */
    fun getArticlesStream(): Flow<List<ArticleDetailData>>

    /**
     * 观察本地文章总数流。
     */
    fun getArticleCountStream(): Flow<Int>

    /**
     * 网络写同步（Write-Only Sync）：从远端 API 拉取最新文章并写入本地 Room 数据库。
     *
     * 注意：本方法执行完毕后不向 UI 直接返回列表数据，数据更新由 Room 数据库的 Flow 响应式驱动。
     *
     * @param page 页码（首页通常为 0）。
     * @return 同步执行结果（成功或异常）。
     */
    suspend fun syncArticles(page: Int = 0): Result<Unit>

    /**
     * 本地模拟写入文章（互动教学验证专用）。
     *
     * 绕过网络直接向 Room 写入一条本地文章，用于验证 UI 是否能自动感知 Room 变动。
     */
    suspend fun insertLocalArticle(title: String)

    /**
     * 本地清空文章缓存（互动教学验证专用）。
     *
     * 清空 Room 数据库，用于验证 UI 是否即时随底层数据表清空而变为空视图。
     */
    suspend fun clearLocalArticles()
}
