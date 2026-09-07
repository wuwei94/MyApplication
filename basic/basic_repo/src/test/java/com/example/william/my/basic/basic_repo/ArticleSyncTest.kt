package com.example.william.my.basic.basic_repo

import com.example.william.my.basic.basic_database.ArticleLocalDataSource
import com.example.william.my.basic.basic_network.datasource.ArticleRemoteDataSource
import com.example.william.my.basic.basic_network.result.NetworkResult
import com.example.william.my.basic.basic_repo.bean.ArticleData
import com.example.william.my.basic.basic_repo.bean.ArticleDetailData
import com.example.william.my.basic.basic_repo.data.repository.DefaultArticleRepository
import com.example.william.my.basic.basic_repo.sync.TestSynchronizer
import com.example.william.my.basic.basic_repo.sync.model.NetworkChangeList
import com.example.william.my.core.retrofit.response.RetrofitResponse
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * 增量同步框架（Synchronizer + Syncable + ChangeList）单元测试
 *
 * 遵循 NiA 测试规范：采用手写测试替身（Fake），验证增量同步拉取、版本游标推进与幂等性保障。
 */
class ArticleSyncTest {

    private lateinit var fakeLocalDataSource: FakeArticleLocalDataSource
    private lateinit var fakeRemoteDataSource: FakeArticleRemoteDataSource
    private lateinit var repository: DefaultArticleRepository
    private lateinit var synchronizer: TestSynchronizer

    @Before
    fun setUp() {
        fakeLocalDataSource = FakeArticleLocalDataSource()
        fakeRemoteDataSource = FakeArticleRemoteDataSource()
        repository = DefaultArticleRepository(fakeRemoteDataSource, fakeLocalDataSource)
        synchronizer = TestSynchronizer()
    }

    @Test
    fun articleSync_initialSync_pullsDataAndAdvancesCursor() = runTest {
        // 初始状态：游标为 0
        assertEquals(0, synchronizer.getChangeListVersions().articleVersion)

        // 触发首次增量同步
        val success = repository.syncWith(synchronizer)

        assertTrue(success)
        // 游标推进至初始版本 1
        assertEquals(1, synchronizer.getChangeListVersions().articleVersion)
        // 本地数据包含初始拉取的文章
        assertEquals(2, fakeLocalDataSource.cachedArticles.size)
    }

    @Test
    fun articleSync_alreadyUpToDate_returnsTrueWithoutWriting() = runTest {
        // 执行首次同步
        repository.syncWith(synchronizer)
        val initialWriteCount = fakeLocalDataSource.writeCount

        // 再次同步（此时服务端无更高版本数据）
        val success = repository.syncWith(synchronizer)

        assertTrue(success)
        // 幂等性：写次数未发生增加
        assertEquals(initialWriteCount, fakeLocalDataSource.writeCount)
        assertEquals(1, synchronizer.getChangeListVersions().articleVersion)
    }

    @Test
    fun articleSync_remotePublishesNewVersion_pullsOnlyIncrementalDelta() = runTest {
        // 首次同步：建立 v1 游标
        repository.syncWith(synchronizer)
        assertEquals(1, synchronizer.getChangeListVersions().articleVersion)

        // 远端模拟发布新版本变更（v2）
        fakeRemoteDataSource.simulateRemoteNewVersion("新架构文章发布")

        // 触发增量同步
        val success = repository.syncWith(synchronizer)

        assertTrue(success)
        // 游标已增量推进至 v2
        assertEquals(2, synchronizer.getChangeListVersions().articleVersion)
        // 本地数据库现包含 3 篇文章（2 篇基础 + 1 篇增量）
        assertEquals(3, fakeLocalDataSource.cachedArticles.size)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 测试专用手写替身（Fake Test Doubles）
    // ─────────────────────────────────────────────────────────────────────────
    private class FakeArticleLocalDataSource : ArticleLocalDataSource {
        val cachedArticles = LinkedHashMap<String, ArticleDetailData>()
        private val articlesFlow = MutableStateFlow<List<ArticleDetailData>>(emptyList())
        var writeCount = 0

        override fun getArticlesStream(): Flow<List<ArticleDetailData>> = articlesFlow

        override fun getArticleCountStream(): Flow<Int> = articlesFlow.map { it.size }

        override suspend fun getArticles(): List<ArticleDetailData> = cachedArticles.values.toList()

        override suspend fun getArticlesByPage(page: Int): List<ArticleDetailData> = cachedArticles.values.filter { it.page == page }

        override suspend fun saveArticle(article: ArticleDetailData) {
            writeCount++
            cachedArticles[article.id] = article
            articlesFlow.value = cachedArticles.values.toList()
        }

        override suspend fun saveArticles(articles: List<ArticleDetailData>) {
            writeCount++
            articles.forEach { cachedArticles[it.id] = it }
            articlesFlow.value = cachedArticles.values.toList()
        }

        override suspend fun deleteAllArticles() {
            cachedArticles.clear()
            articlesFlow.value = emptyList()
        }
    }

    private class FakeArticleRemoteDataSource : ArticleRemoteDataSource {
        private val changes = mutableListOf(
            NetworkChangeList(
                id = "1",
                changeListVersion = 1,
                isDelete = false,
                article = ArticleDetailData(id = "1", title = "Android 架构第一篇", link = "", page = 0),
            ),
            NetworkChangeList(
                id = "2",
                changeListVersion = 1,
                isDelete = false,
                article = ArticleDetailData(id = "2", title = "Android 架构第二篇", link = "", page = 0),
            ),
        )
        private var currentVersion = 1

        override suspend fun getArticleChangeList(afterVersion: Int): List<NetworkChangeList> = changes.filter { it.changeListVersion > afterVersion }

        override fun simulateRemoteNewVersion(title: String): NetworkChangeList {
            currentVersion++
            val change = NetworkChangeList(
                id = "sim_$currentVersion",
                changeListVersion = currentVersion,
                isDelete = false,
                article = ArticleDetailData(id = "sim_$currentVersion", title = title, link = "", page = 0),
            )
            changes.add(change)
            return change
        }

        override fun getArticleCallback(page: Int, callback: ArticleRemoteDataSource.LoadArticleCallback) = Unit
        override fun getArticleSingle(page: Int): Single<RetrofitResponse<ArticleData>> = throw UnsupportedOperationException()
        override suspend fun getArticleSuspend(page: Int): RetrofitResponse<ArticleData> = throw UnsupportedOperationException()
        override suspend fun getArticleResult(page: Int): NetworkResult<List<ArticleDetailData>> = NetworkResult.Success(changes.mapNotNull { it.article })
    }
}
