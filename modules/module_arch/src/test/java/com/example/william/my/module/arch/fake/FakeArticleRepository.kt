package com.example.william.my.module.arch.fake

import androidx.lifecycle.LiveData
import com.example.william.my.basic.basic_repo.bean.ArticleData
import com.example.william.my.basic.basic_repo.bean.ArticleDetailData
import com.example.william.my.basic.basic_repo.data.repository.ArticleRepository
import com.example.william.my.basic.basic_repo.data.result.NetworkResult
import com.example.william.my.basic.basic_repo.sync.Synchronizer
import com.example.william.my.core.retrofit.response.RetrofitResponse
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * 内存级文章数据仓库测试替身（Fake）
 *
 * 遵循测试原则：不使用 Mockito 插桩，基于内存 StateFlow 提供真实的响应式流。
 */
class FakeArticleRepository : ArticleRepository {

    private val articlesFlow = MutableStateFlow<List<ArticleDetailData>>(emptyList())
    var shouldSyncSucceed: Boolean = true

    fun emitArticles(articles: List<ArticleDetailData>) {
        articlesFlow.value = articles
    }

    override fun getArticlesStream(): Flow<List<ArticleDetailData>> = articlesFlow.asStateFlow()

    override fun getArticleCountStream(): Flow<Int> = articlesFlow.map { it.size }

    override suspend fun syncArticles(page: Int): Result<Unit> = if (shouldSyncSucceed) {
        val current = articlesFlow.value.toMutableList()
        current.add(
            ArticleDetailData(
                id = "remote_${current.size + 1}",
                title = "同步文章_${current.size + 1}",
                link = "https://example.com/sync",
                page = page,
            ),
        )
        articlesFlow.value = current
        Result.success(Unit)
    } else {
        Result.failure(RuntimeException("模拟网络同步失败"))
    }

    override suspend fun insertLocalArticle(title: String) {
        val current = articlesFlow.value.toMutableList()
        current.add(
            ArticleDetailData(
                id = "local_${current.size + 1}",
                title = title,
                link = "https://example.com/local",
                page = 0,
            ),
        )
        articlesFlow.value = current
    }

    override suspend fun clearLocalArticles() {
        articlesFlow.value = emptyList()
    }

    override fun simulateRemoteNewVersion(title: String) {
        // 模拟远端产生新版本
    }

    override suspend fun syncWith(synchronizer: Synchronizer): Boolean = true

    override fun getArticleCallback(page: Int, callback: ArticleRepository.LoadArticleCallback) {}
    override fun getArticleSingle(page: Int): Single<RetrofitResponse<ArticleData>> = throw NotImplementedError()
    override suspend fun getArticleSuspend(page: Int): RetrofitResponse<ArticleData> = throw NotImplementedError()
    override fun getArticleLiveData(page: Int): LiveData<RetrofitResponse<ArticleData>> = throw NotImplementedError()
    override fun getArticleLiveDataByRx(page: Int): LiveData<RetrofitResponse<ArticleData>> = throw NotImplementedError()
    override fun getArticleLiveDataByFlow(page: Int): LiveData<RetrofitResponse<ArticleData>> = throw NotImplementedError()
    override fun getArticleFlow(page: Int): Flow<RetrofitResponse<ArticleData>> = throw NotImplementedError()
    override fun getArticleFlowByRx(page: Int): Flow<RetrofitResponse<ArticleData>> = throw NotImplementedError()
    override fun getArticleFlowByLiveData(page: Int): Flow<RetrofitResponse<ArticleData>> = throw NotImplementedError()
    override suspend fun getArticleResult(page: Int, forceUpdate: Boolean): NetworkResult<List<ArticleDetailData>> = throw NotImplementedError()
}
