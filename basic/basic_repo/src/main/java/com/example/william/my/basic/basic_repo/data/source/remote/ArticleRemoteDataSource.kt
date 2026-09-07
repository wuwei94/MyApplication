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
package com.example.william.my.basic.basic_repo.data.source.remote

import com.example.william.my.basic.basic_repo.api.ArticleApi
import com.example.william.my.basic.basic_repo.api.ArticleRxApi
import com.example.william.my.basic.basic_repo.bean.ArticleData
import com.example.william.my.basic.basic_repo.bean.ArticleDetailData
import com.example.william.my.basic.basic_repo.data.result.NetworkResult
import com.example.william.my.basic.basic_repo.sync.model.NetworkChangeList
import com.example.william.my.core.retrofit.exception.ApiException
import com.example.william.my.core.retrofit.response.RetrofitResponse
import com.example.william.my.core.retrofit.rx.callback.ResponseCallback
import com.example.william.my.core.retrofit.rx.function.HttpResultFunction
import com.example.william.my.core.retrofit.rx.function.ServerResultFunction
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

/**
 * 远程网络数据源接口。
 */
interface ArticleRemoteDataSource {

    interface LoadArticleCallback {
        fun onArticleLoaded(articles: List<ArticleDetailData>)
        fun onDataNotAvailable()
    }

    /**
     * 传统异步回调方式加载数据。
     */
    fun getArticleCallback(page: Int, callback: LoadArticleCallback)

    /**
     * RxJava3 Single：执行网络请求并返回单次响应流。
     */
    fun getArticleSingle(page: Int): Single<RetrofitResponse<ArticleData>>

    /**
     * 协程挂起函数：执行网络请求并返回业务响应。
     */
    suspend fun getArticleSuspend(page: Int): RetrofitResponse<ArticleData>

    /**
     * 业务数据挂起请求并包装为 [NetworkResult]。
     */
    suspend fun getArticleResult(page: Int): NetworkResult<List<ArticleDetailData>>

    /**
     * 按版本游标拉取增量数据变更列表（ChangeList）。
     *
     * @param afterVersion 客户端本地已持久化的版本游标。若远端无高于此版本的变更，则返回空列表。
     */
    suspend fun getArticleChangeList(afterVersion: Int): List<NetworkChangeList>

    /**
     * 模拟服务端发布更高版本的数据（教学演练专用，用于驱动增量拉取与游标递增）。
     */
    fun simulateRemoteNewVersion(title: String): NetworkChangeList
}

/**
 * 远程网络数据源默认实现。
 */
class ArticleRemoteDataSourceImpl(
    private val articleApi: ArticleApi,
    private val articleRxApi: ArticleRxApi,
) : ArticleRemoteDataSource {

    // ─────────────────────────────────────────────────────────────────────────
    // 1. 传统回调 API（教学对比演示）
    // ─────────────────────────────────────────────────────────────────────────
    override fun getArticleCallback(
        page: Int,
        callback: ArticleRemoteDataSource.LoadArticleCallback,
    ) {
        articleRxApi.getArticleSingle(page)
            .map(ServerResultFunction())
            .onErrorResumeNext(HttpResultFunction())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : ResponseCallback<ArticleData>() {
                override fun onResponse(response: ArticleData?) {
                    super.onResponse(response)
                    response?.run {
                        if (datas.isNotEmpty()) {
                            callback.onArticleLoaded(datas)
                        } else {
                            callback.onDataNotAvailable()
                        }
                    }
                }

                override fun onFailure(e: ApiException) {
                    super.onFailure(e)
                    callback.onDataNotAvailable()
                }
            })
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. 基础请求 API（Single 与 挂起函数）
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * RxJava3 Single：执行网络请求并返回单次响应流。
     */
    override fun getArticleSingle(page: Int): Single<RetrofitResponse<ArticleData>> = articleRxApi.getArticleSingle(page)
        .map(ServerResultFunction())
        .onErrorResumeNext(HttpResultFunction())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    /**
     * 协程挂起函数：执行网络请求并返回业务响应。
     */
    override suspend fun getArticleSuspend(page: Int): RetrofitResponse<ArticleData> = articleApi.getArticleSuspend(page)

    // ─────────────────────────────────────────────────────────────────────────
    // 3. 业务数据挂起请求
    // ─────────────────────────────────────────────────────────────────────────
    override suspend fun getArticleResult(page: Int): NetworkResult<List<ArticleDetailData>> {
        return try {
            val response = articleApi.getArticleSuspend(page)
            val data = response.data
                ?: return NetworkResult.Error(IllegalStateException("Response data is null"))
            val articles = data.datas.map { it.copy(page = page) }
            NetworkResult.Success(articles)
        } catch (e: Exception) {
            NetworkResult.Error(e)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. 增量同步变更列表（ChangeList）API（对齐 Now in Android）
    // ─────────────────────────────────────────────────────────────────────────
    private val remoteVersionChanges = mutableListOf<NetworkChangeList>()
    private var simulatedVersionCounter = 0

    override suspend fun getArticleChangeList(afterVersion: Int): List<NetworkChangeList> {
        // 如果本地版本游标为初始状态（afterVersion <= 0），先拉取首批基础数据作为初始版本（Version 1）
        if (afterVersion <= 0 && remoteVersionChanges.isEmpty()) {
            val networkResult = getArticleResult(0)
            if (networkResult is NetworkResult.Success) {
                val initialList = networkResult.data.map { item ->
                    NetworkChangeList(
                        id = item.id,
                        changeListVersion = 1,
                        isDelete = false,
                        article = item,
                    )
                }
                remoteVersionChanges.addAll(initialList)
                simulatedVersionCounter = 1
            }
        }
        // 增量同步核心契约：仅返回版本号严格大于客户端游标 afterVersion 的数据变更项
        return remoteVersionChanges.filter { it.changeListVersion > afterVersion }
    }

    override fun simulateRemoteNewVersion(title: String): NetworkChangeList {
        simulatedVersionCounter++
        val newChange = NetworkChangeList(
            id = "simulated_${System.currentTimeMillis()}",
            changeListVersion = simulatedVersionCounter,
            isDelete = false,
            article = ArticleDetailData(
                id = "simulated_${System.currentTimeMillis()}",
                title = "【增量变更 v$simulatedVersionCounter】$title",
                link = "https://www.wanandroid.com",
                page = 0,
            ),
        )
        remoteVersionChanges.add(newChange)
        return newChange
    }
}
