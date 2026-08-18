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

import com.example.william.my.basic.basic_repo.bean.ArticleData
import com.example.william.my.basic.basic_repo.bean.ArticleDetailData
import com.example.william.my.basic.basic_repo.data.result.NetworkResult
import com.example.william.my.basic.basic_repo.data.source.ArticleDataSource
import com.example.william.my.basic.basic_repo.database.dao.ArticleDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ArticleLocalDataSourceImpl(
    private val articleDao: ArticleDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ArticleDataSource<ArticleData, ArticleDetailData> {

    override suspend fun getArticleResult(page: Int): NetworkResult<List<ArticleDetailData>> {
        return withContext(ioDispatcher) {
            try {
                val articles = articleDao.getArticles()
                NetworkResult.Success(articles)
            } catch (e: Exception) {
                NetworkResult.Error(e)
            }
        }
    }

    override suspend fun saveArticle(article: ArticleDetailData) {
        withContext(ioDispatcher) {
            articleDao.insertArticle(article)
        }
    }

    override suspend fun saveArticles(articles: List<ArticleDetailData>) {
        withContext(ioDispatcher) {
            articleDao.insertArticles(articles)
        }
    }

    override suspend fun deleteAllArticles() {
        withContext(ioDispatcher) {
            articleDao.deleteAllArticles()
        }
    }
}
