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
package com.example.william.my.basic.basic_repo.data.source.local

import com.example.william.my.basic.basic_repo.bean.ArticleDetailData
import com.example.william.my.basic.basic_repo.data.result.NetworkResult
import com.example.william.my.basic.basic_repo.database.dao.ArticleDao

/**
 * 本地持久化数据源接口。
 */
interface ArticleLocalDataSource {

    /**
     * 从本地数据库按页查询文章数据。
     */
    suspend fun getArticleResult(page: Int): NetworkResult<List<ArticleDetailData>>

    /**
     * 保存单篇文章到本地数据库。
     */
    suspend fun saveArticle(article: ArticleDetailData)

    /**
     * 批量保存文章到本地数据库。
     */
    suspend fun saveArticles(articles: List<ArticleDetailData>)

    /**
     * 清空本地数据库中的所有文章。
     */
    suspend fun deleteAllArticles()
}

/**
 * 本地持久化数据源实现（Room DAO 挂起函数原生 main-safe，无需额外 withContext）。
 */
class ArticleLocalDataSourceImpl(
    private val articleDao: ArticleDao
) : ArticleLocalDataSource {

    override suspend fun getArticleResult(page: Int): NetworkResult<List<ArticleDetailData>> {
        return try {
            val articles = if (page >= 0) {
                articleDao.getArticlesByPage(page)
            } else {
                articleDao.getArticles()
            }
            NetworkResult.Success(articles)
        } catch (e: Exception) {
            NetworkResult.Error(e)
        }
    }

    override suspend fun saveArticle(article: ArticleDetailData) {
        articleDao.insertArticle(article)
    }

    override suspend fun saveArticles(articles: List<ArticleDetailData>) {
        articleDao.insertArticles(articles)
    }

    override suspend fun deleteAllArticles() {
        articleDao.deleteAllArticles()
    }
}
