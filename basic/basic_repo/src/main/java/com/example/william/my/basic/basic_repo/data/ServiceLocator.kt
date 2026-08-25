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
package com.example.william.my.basic.basic_repo.data

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.example.william.my.basic.basic_repo.api.ArticleApi
import com.example.william.my.basic.basic_repo.api.ArticleRxApi
import com.example.william.my.basic.basic_repo.data.repository.ArticleRepository
import com.example.william.my.basic.basic_repo.data.repository.DefaultArticleRepository
import com.example.william.my.basic.basic_repo.data.source.local.ArticleLocalDataSource
import com.example.william.my.basic.basic_repo.data.source.local.ArticleLocalDataSourceImpl
import com.example.william.my.basic.basic_repo.data.source.remote.ArticleRemoteDataSource
import com.example.william.my.basic.basic_repo.data.source.remote.ArticleRemoteDataSourceImpl
import com.example.william.my.basic.basic_repo.database.ArticleDatabase
import com.example.william.my.core.retrofit.createApi
import com.example.william.my.core.retrofit.rx.api.createRxApi

/**
 * 服务定位器（Service Locator），统一管理并对外提供数据层依赖实例。
 */
object ServiceLocator {

    private val lock = Any()

    @Volatile
    @VisibleForTesting
    var articleApi: ArticleApi? = null
        @VisibleForTesting set

    @Volatile
    @VisibleForTesting
    var articleRxApi: ArticleRxApi? = null
        @VisibleForTesting set

    @Volatile
    @VisibleForTesting
    var articleDatabase: ArticleDatabase? = null
        @VisibleForTesting set

    @Volatile
    @VisibleForTesting
    var articleRepository: ArticleRepository? = null
        @VisibleForTesting set

    // =========================================================================
    // 1. module_arch 模块专用 API
    // =========================================================================

    /**
     * 获取文章数据仓库实例（[DefaultArticleRepository]）。
     *
     * 专门提供给 module_arch 与 module_mavericks 中的各个架构模式使用：
     * - MVP：ArticlePresenter
     * - MVVM：ArticleLiveDataViewModel（伴生 Factory）
     * - MVI：ArticleStateFlowViewModel（伴生 Factory）
     * - Mavericks：ArticleMavericksViewModel、ArticleMavericksRepository
     */
    fun provideArticleRepository(context: Context): ArticleRepository {
        synchronized(lock) {
            return articleRepository ?: createArticleRepository(context)
        }
    }

    // =========================================================================
    // 2. module_jetpack 模块专用 API
    // =========================================================================

    /**
     * 获取标准文章网络请求 API 实例（[ArticleApi]，未装配 RxJava Adapter，专供协程挂起函数）。
     *
     * 供给协程相关组件调用：
     * - ArticleRemoteDataSourceImpl (挂起函数)
     * - module_jetpack (ArticlePagingSource, ArticleRemoteMediator)
     */
    fun provideArticleApi(): ArticleApi {
        synchronized(lock) {
            return articleApi ?: createApi()
        }
    }

    /**
     * 获取 RxJava 响应式文章网络请求 API 实例（[ArticleRxApi]，已装配 RxJava3CallAdapterFactory）。
     *
     * 供给 RxJava 相关组件调用：
     * - ArticleRemoteDataSourceImpl (Rx 响应式流)
     * - module_jetpack (ArticleRxPagingSource, ArticleRxRemoteMediator)
     */
    fun provideArticleRxApi(): ArticleRxApi {
        synchronized(lock) {
            return articleRxApi ?: createRxApi()
        }
    }

    /**
     * 获取 Room 文章数据库实例（[ArticleDatabase]）。
     *
     * 供给 module_jetpack 的 Paging 分页组件调用：
     * - PagingViewModel、ArticleRemoteMediator、ArticleRxRemoteMediator
     */
    fun provideArticleDatabase(context: Context): ArticleDatabase {
        synchronized(lock) {
            return articleDatabase ?: ArticleDatabase.getInstance(context)
        }
    }

    // =========================================================================
    // 3. 测试桩（Test Doubles）重置 API
    // =========================================================================

    /**
     * 重置所有单例实例并清空数据库，主要供单元测试使用，避免测试用例间相互污染。
     */
    @VisibleForTesting
    fun resetRepository() {
        synchronized(lock) {
            articleRepository = null
            articleApi = null
            articleRxApi = null
            articleDatabase = null
            // 清空并关闭真实单例数据库
            ArticleDatabase.resetDatabase()
        }
    }

    // =========================================================================
    // 4. 内部构建与装配方法
    // =========================================================================

    private fun createApi(): ArticleApi {
        return createApi(ArticleApi::class.java).also {
            articleApi = it
        }
    }

    private fun createRxApi(): ArticleRxApi {
        return createRxApi(ArticleRxApi::class.java).also {
            articleRxApi = it
        }
    }

    private fun createArticleRemoteDataSource(): ArticleRemoteDataSource {
        return ArticleRemoteDataSourceImpl(provideArticleApi(), provideArticleRxApi())
    }

    private fun createArticleLocalDataSource(context: Context): ArticleLocalDataSource {
        val database = provideArticleDatabase(context)
        return ArticleLocalDataSourceImpl(database.articleDao())
    }

    private fun createArticleRepository(context: Context): ArticleRepository {
        val newRepo =
            DefaultArticleRepository(createArticleRemoteDataSource(), createArticleLocalDataSource(context))
        articleRepository = newRepo
        return newRepo
    }
}
