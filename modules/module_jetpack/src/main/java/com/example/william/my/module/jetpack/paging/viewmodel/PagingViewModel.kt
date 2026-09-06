package com.example.william.my.module.jetpack.paging.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.liveData
import androidx.paging.rxjava3.cachedIn
import androidx.paging.rxjava3.flowable
import com.example.william.my.basic.basic_repo.api.ArticleApi
import com.example.william.my.basic.basic_repo.api.ArticleRxApi
import com.example.william.my.basic.basic_repo.bean.ArticleDetailData
import com.example.william.my.basic.basic_repo.data.ServiceLocator
import com.example.william.my.basic.basic_repo.database.ArticleDatabase
import com.example.william.my.module.jetpack.paging.mediator.ArticleRemoteMediator
import com.example.william.my.module.jetpack.paging.mediator.ArticleRxRemoteMediator
import com.example.william.my.module.jetpack.paging.remotekey.RemoteKeyDatabase
import com.example.william.my.module.jetpack.paging.source.ArticlePagingSource
import com.example.william.my.module.jetpack.paging.source.ArticleRxPagingSource
import io.reactivex.rxjava3.core.Flowable
import kotlinx.coroutines.flow.Flow

/**
 * 分页 ViewModel
 *
 * 提供网络、数据库缓存等多种分页数据流。
 */
class PagingViewModel(
    private val articleDatabase: ArticleDatabase,
    private val remoteKeyDatabase: RemoteKeyDatabase,
    private val articleApi: ArticleApi,
    private val articleRxApi: ArticleRxApi,
) : ViewModel() {

    // ─────────────────────────────────────────────────────────────────────────
    // 1. 网络 + 数据库缓存（RemoteMediator 模式，离线优先）
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * LiveData 流：通过 ArticleRemoteMediator 请求网络写入 Room，并监听 Room PagingSource
     */
    @OptIn(ExperimentalPagingApi::class)
    val articlesByMediatorLiveData: LiveData<PagingData<ArticleDetailData>> =
        Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = {
                articleDatabase.articleDao().getArticlesPagingSource()
            },
            remoteMediator = ArticleRemoteMediator(
                articleDatabase,
                remoteKeyDatabase,
                articleApi,
            ),
        ).liveData.cachedIn(viewModelScope)

    /**
     * 协程 Flow 流：通过 ArticleRemoteMediator 请求网络写入 Room，并监听 Room PagingSource
     */
    @OptIn(ExperimentalPagingApi::class)
    val articlesByMediatorFlow: Flow<PagingData<ArticleDetailData>> =
        Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = {
                // pagingSourceFactory 每次调用都应返回全新的 PagingSource，因为 PagingSource 实例不可复用。
                articleDatabase.articleDao().getArticlesPagingSource()
            },
            remoteMediator = ArticleRemoteMediator(
                articleDatabase,
                remoteKeyDatabase,
                articleApi,
            ),
        ).flow.cachedIn(viewModelScope)

    /**
     * RxJava Flowable 流：通过 ArticleRxRemoteMediator 请求网络写入 Room，并监听 Room PagingSource
     */
    @OptIn(ExperimentalPagingApi::class)
    val articlesByMediatorFlowable: Flowable<PagingData<ArticleDetailData>> =
        Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = {
                articleDatabase.articleDao().getArticlesPagingSource()
            },
            remoteMediator = ArticleRxRemoteMediator(
                articleDatabase,
                remoteKeyDatabase,
                articleRxApi,
            ),
        ).flowable.cachedIn(viewModelScope)

    // ─────────────────────────────────────────────────────────────────────────
    // 2. 纯网络直接加载（PagingSource 模式，无本地缓存）
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * LiveData 流：直接通过 ArticlePagingSource 加载网络数据
     */
    val articlesByNetworkLiveData: LiveData<PagingData<ArticleDetailData>> = Pager(
        config = PagingConfig(pageSize = 20),
    ) {
        ArticlePagingSource(articleApi)
    }.liveData.cachedIn(viewModelScope)

    /**
     * 协程 Flow 流：直接通过 ArticlePagingSource 加载网络数据
     */
    val articlesByNetworkFlow: Flow<PagingData<ArticleDetailData>> = Pager(
        config = PagingConfig(pageSize = 20),
    ) {
        ArticlePagingSource(articleApi)
    }.flow.cachedIn(viewModelScope)

    /**
     * RxJava Flowable 流：直接通过 ArticleRxPagingSource 加载网络数据
     */
    val articlesByNetworkFlowable: Flowable<PagingData<ArticleDetailData>> = Pager(
        config = PagingConfig(pageSize = 20),
    ) {
        ArticleRxPagingSource(articleRxApi)
    }.flowable.cachedIn(viewModelScope)

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                PagingViewModel(
                    articleDatabase = ServiceLocator.provideArticleDatabase(application),
                    remoteKeyDatabase = RemoteKeyDatabase.getInstance(application),
                    articleApi = ServiceLocator.provideArticleApi(),
                    articleRxApi = ServiceLocator.provideArticleRxApi(),
                )
            }
        }
    }
}
