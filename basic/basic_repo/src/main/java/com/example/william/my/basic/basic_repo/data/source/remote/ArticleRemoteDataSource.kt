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

import androidx.lifecycle.LiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import com.example.william.my.basic.basic_repo.api.ArticleApi
import com.example.william.my.basic.basic_repo.bean.ArticleData
import com.example.william.my.basic.basic_repo.bean.ArticleDetailData
import com.example.william.my.basic.basic_repo.data.NetworkResult
import com.example.william.my.basic.basic_repo.data.source.ArticleDataSource
import com.example.william.my.core.retrofit.rx.callback.ResponseCallback
import com.example.william.my.core.retrofit.exception.ApiException
import com.example.william.my.core.retrofit.rx.api.createRxApi
import com.example.william.my.core.retrofit.rx.api.toLiveData
import com.example.william.my.core.retrofit.rx.function.HttpResultFunction
import com.example.william.my.core.retrofit.rx.function.ServerResultFunction
import com.example.william.my.core.retrofit.response.RetrofitResponse
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.rx3.asFlow

object ArticleRemoteDataSourceImpl : ArticleDataSource<ArticleData, ArticleDetailData> {

    private val articleApi = createRxApi(ArticleApi::class.java)

    // =========================================================================
    // 1. 传统回调 API（教学对比演示）
    // =========================================================================

    override fun getArticleCallback(
        page: Int,
        callback: ArticleDataSource.LoadArticleCallback<ArticleDetailData>
    ) {
        articleApi.getArticleSingle(page)
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

    // =========================================================================
    // 2. 基础请求 API（Single 与 挂起函数）
    // =========================================================================

    /**
     * RxJava3 Single：执行网络请求并返回单次响应流。
     */
    override fun getArticleSingle(page: Int): Single<RetrofitResponse<ArticleData>> {
        return articleApi.getArticleSingle(page)
            .map(ServerResultFunction())
            .onErrorResumeNext(HttpResultFunction())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 协程挂起函数：执行网络请求并返回业务响应。
     */
    override suspend fun getArticleSuspend(page: Int): RetrofitResponse<ArticleData> {
        return articleApi.getArticleSuspend(page)
    }

    // =========================================================================
    // 3. Flow 响应式数据流及互转 API
    // =========================================================================

    /**
     * RxJava 转 Flow：将 Single 转换为 Kotlin 响应式 Flow (asFlow)。
     */
    override fun getArticleFlowByRx(page: Int): Flow<RetrofitResponse<ArticleData>> {
        return articleApi.getArticleSingle(page)
            .map(ServerResultFunction())
            .onErrorResumeNext(HttpResultFunction())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .toObservable()
            .asFlow()
    }

    /**
     * 纯协程 Flow 构建：通过 flow { ... } 发送 loading/error/success 状态。
     */
    override fun getArticleFlow(page: Int): Flow<RetrofitResponse<ArticleData>> {
        return flow {
            emit(RetrofitResponse.loading())
            val response = articleApi.getArticleSuspend(page)
            emit(response)
        }.catch { e ->
            emit(RetrofitResponse.error(e.message ?: "网络请求失败"))
        }.flowOn(Dispatchers.IO)
    }

    /**
     * LiveData 转 Flow：将 LiveData 转换为 Kotlin 响应式 Flow (asFlow)。
     */
    override fun getArticleFlowByLiveData(page: Int): Flow<RetrofitResponse<ArticleData>> {
        return liveData(Dispatchers.IO) {
            emit(RetrofitResponse.loading())
            try {
                val response = articleApi.getArticleSuspend(page)
                emit(response)
            } catch (e: Exception) {
                emit(RetrofitResponse.error(e.message ?: "网络请求失败"))
            }
        }.asFlow()
    }

    // =========================================================================
    // 4. LiveData 响应式数据流及互转 API
    // =========================================================================

    /**
     * RxJava 转 LiveData：遵循 ReactiveStreams 规范将 Single 桥接为 LiveData。
     */
    override fun getArticleLiveDataByRx(page: Int): LiveData<RetrofitResponse<ArticleData>> {
        return articleApi.getArticleSingle(page)
            .map(ServerResultFunction())
            .onErrorResumeNext(HttpResultFunction())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .toLiveData()
    }

    /**
     * 官方 liveData 协程构建器：通过 liveData(Dispatchers.IO) { ... } 构建生命周期感知的 LiveData。
     */
    override fun getArticleLiveData(page: Int): LiveData<RetrofitResponse<ArticleData>> {
        return liveData(Dispatchers.IO) {
            emit(RetrofitResponse.loading())
            try {
                val response = articleApi.getArticleSuspend(page)
                emit(response)
            } catch (e: Exception) {
                emit(RetrofitResponse.error(e.message ?: "网络请求失败"))
            }
        }
    }

    /**
     * Flow 转 LiveData：通过 asLiveData() 将 Kotlin Flow 桥接为 LiveData。
     */
    override fun getArticleLiveDataByFlow(page: Int): LiveData<RetrofitResponse<ArticleData>> {
        return flow {
            emit(RetrofitResponse.loading())
            val response = articleApi.getArticleSuspend(page)
            emit(response)
        }.catch { e ->
            emit(RetrofitResponse.error(e.message ?: "网络请求失败"))
        }.flowOn(Dispatchers.IO).asLiveData()
    }

    override suspend fun getArticleResult(page: Int): NetworkResult<List<ArticleDetailData>> {
        return try {
            val response = articleApi.getArticleSuspend(page)
            val data = response.data
                ?: return NetworkResult.Error(IllegalStateException("Response data is null"))
            NetworkResult.Success(data.datas)
        } catch (e: Exception) {
            NetworkResult.Error(e)
        }
    }

}
