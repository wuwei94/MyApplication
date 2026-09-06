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
package com.example.william.my.basic.basic_repo.database.dao

import androidx.annotation.WorkerThread
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.example.william.my.basic.basic_repo.bean.ArticleDetailData
import kotlinx.coroutines.flow.Flow

/**
 * 文章表的数据访问对象（DAO）
 */
@Dao
interface ArticleDao {

    /**
     * 以响应式 Flow 流方式观察所有文章（SSOT 唯一数据源）。
     *
     * 依赖 Room 原生 InvalidationTracker 机制：
     * 当 Articles 表发生任何写入、更新或删除变动时，Room 自动在后台线程重新执行查询并向下游推流。
     */
    @Query("SELECT * FROM Articles")
    fun getArticlesStream(): Flow<List<ArticleDetailData>>

    /**
     * 观察本地缓存文章总条数流。
     */
    @Query("SELECT COUNT(*) FROM Articles")
    fun getArticleCountStream(): Flow<Int>

    /**
     * 查询文章表中的全部文章。
     *
     * @return 全部文章。
     */
    @Query("SELECT * FROM Articles")
    suspend fun getArticles(): List<ArticleDetailData>

    /**
     * 查询文章表中指定页码的文章。
     *
     * @param page 页码。
     * @return 指定页码的文章。
     */
    @Query("SELECT * FROM Articles WHERE page = :page")
    suspend fun getArticlesByPage(page: Int): List<ArticleDetailData>

    /**
     * 批量插入或更新文章（Room 2.5+ 原生 @Upsert，优先于传统 REPLACE 策略）。
     *
     * 相比于 OnConflictStrategy.REPLACE（底层先 DELETE 再 INSERT，易破坏主键且产生双重触发器开销），
     * @Upsert 采用真正的 INSERT ... ON CONFLICT DO UPDATE 原生逻辑，性能更高且更安全。
     */
    @Upsert
    suspend fun upsertArticles(articles: List<ArticleDetailData>)

    /**
     * 插入或更新单篇文章。
     */
    @Upsert
    suspend fun upsertArticle(article: ArticleDetailData)

    /**
     * 批量插入文章，若已存在则替换。
     *
     * @param articles 待插入的文章。
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<ArticleDetailData>)

    /**
     * 同步阻塞插入文章数据。
     *
     * 注意：属于同步 I/O 操作，禁止在主线程直接调用。
     */
    @WorkerThread
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertArticlesSync(articles: List<ArticleDetailData>)

    /**
     * 插入单篇文章，若已存在则替换。
     *
     * @param article 待插入的文章。
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: ArticleDetailData)

    /**
     * 删除全部文章。
     */
    @Query("DELETE FROM Articles")
    suspend fun deleteAllArticles()

    /**
     * 同步阻塞清空文章数据。
     *
     * 注意：属于同步 I/O 操作，禁止在主线程直接调用。
     */
    @WorkerThread
    @Query("DELETE FROM Articles")
    fun deleteAllArticlesSync()

    /**
     * 删除指定页码的文章
     *
     * @param page 页码
     */
    @Query("DELETE FROM Articles WHERE page = :page")
    suspend fun deleteArticlesByPage(page: Int)

    /**
     * 返回按页码排序的文章分页数据源（供 Paging 3 使用）
     */
    @Query("Select * From Articles Order By page")
    fun getArticlesPagingSource(): PagingSource<Int, ArticleDetailData>
}
