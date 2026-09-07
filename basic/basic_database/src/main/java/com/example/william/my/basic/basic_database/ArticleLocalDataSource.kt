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
package com.example.william.my.basic.basic_database

import com.example.william.my.basic.basic_database.dao.ArticleDao
import com.example.william.my.basic.basic_database.model.asEntity
import com.example.william.my.basic.basic_database.model.asExternalModel
import com.example.william.my.basic.basic_model.ArticleDetailData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 本地持久化数据源接口。
 */
interface ArticleLocalDataSource {

    /**
     * 响应式观察本地所有文章流（SSOT 唯一数据源）。
     */
    fun getArticlesStream(): Flow<List<ArticleDetailData>>

    /**
     * 响应式观察本地文章总数流。
     */
    fun getArticleCountStream(): Flow<Int>

    /**
     * 从本地数据库按页查询文章数据。
     */
    suspend fun getArticlesByPage(page: Int): List<ArticleDetailData>

    /**
     * 查询本地所有文章数据。
     */
    suspend fun getArticles(): List<ArticleDetailData>

    /**
     * 保存或更新单篇文章到本地数据库。
     */
    suspend fun saveArticle(article: ArticleDetailData)

    /**
     * 批量保存或更新文章到本地数据库。
     */
    suspend fun saveArticles(articles: List<ArticleDetailData>)

    /**
     * 清空本地数据库中的所有文章。
     */
    suspend fun deleteAllArticles()
}

/**
 * 本地持久化数据源默认实现。
 */
class ArticleLocalDataSourceImpl(
    private val articleDao: ArticleDao,
) : ArticleLocalDataSource {

    override fun getArticlesStream(): Flow<List<ArticleDetailData>> = articleDao.getArticlesStream().map { entities ->
        entities.map { it.asExternalModel() }
    }

    override fun getArticleCountStream(): Flow<Int> = articleDao.getArticleCountStream()

    override suspend fun getArticlesByPage(page: Int): List<ArticleDetailData> = articleDao.getArticlesByPage(page).map { it.asExternalModel() }

    override suspend fun getArticles(): List<ArticleDetailData> = articleDao.getArticles().map { it.asExternalModel() }

    override suspend fun saveArticle(article: ArticleDetailData) {
        articleDao.upsertArticle(article.asEntity())
    }

    override suspend fun saveArticles(articles: List<ArticleDetailData>) {
        articleDao.upsertArticles(articles.map { it.asEntity() })
    }

    override suspend fun deleteAllArticles() {
        articleDao.deleteAllArticles()
    }
}
