package com.example.william.my.module.jetpack.paging.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.rxjava3.RxRemoteMediator
import com.example.william.my.basic.basic_repo.api.ArticleRxApi
import com.example.william.my.basic.basic_repo.bean.ArticleDetailData
import com.example.william.my.basic.basic_repo.database.ArticleDatabase
import com.example.william.my.basic.basic_shared.utils.Utils
import com.example.william.my.module.jetpack.paging.remotekey.RemoteKeyDatabase
import com.example.william.my.module.jetpack.paging.remotekey.data.RemoteKeyData
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * 文章分页数据远程协调器（RxJava3 响应式版本）。
 *
 * 核心机制（Paging 3 离线优先架构）：
 * 1. 单一真实数据源：UI 仅观察 Room 数据库返回的 PagingSource，不直接消费网络数据。
 * 2. 边界触发：当 UI 滑动到达已加载数据的边界（或初始加载）时，Paging 框架自动回调本协调器的 [loadSingle] 方法。
 * 3. 事务写入与自动失效：[loadSingle] 请求网络获取最新数据后，在数据库事务中写入 Room，Room 会自动触发 [androidx.paging.PagingSource.invalidate]，
 *    从而驱动 Paging 重新从数据库拉取最新数据并在 UI 上呈现。
 */
@OptIn(ExperimentalPagingApi::class)
class ArticleRxRemoteMediator(
    private val articleDatabase: ArticleDatabase,
    private val remoteKeyDatabase: RemoteKeyDatabase,
    private val networkApi: ArticleRxApi
) : RxRemoteMediator<Int, ArticleDetailData>() {

    private val articleDao = articleDatabase.articleDao()
    private val remoteKeyDao = remoteKeyDatabase.remoteKeyDao()

    /**
     * 初始化策略检查：在初次加载前执行，决定是否需要跳过初始网络刷新。
     */
    override fun initializeSingle(): Single<InitializeAction> {
        return Single.fromCallable {
            val cacheTimeout = TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS)
            val lastUpdated = remoteKeyDao.lastUpdatedSync() ?: 0L
            if (System.currentTimeMillis() - lastUpdated < cacheTimeout) {
                // 本地缓存仍在有效期内（1小时），直接展示缓存数据，跳过初始网络刷新
                InitializeAction.SKIP_INITIAL_REFRESH
            } else {
                // 本地缓存已过期或不存在，需要执行初始网络刷新；
                // 返回 LAUNCH_INITIAL_REFRESH 会在 REFRESH 成功前阻止并发的 APPEND 和 PREPEND
                InitializeAction.LAUNCH_INITIAL_REFRESH
            }
        }.subscribeOn(Schedulers.io())
    }

    /**
     * 分页加载核心逻辑：根据 [loadType] 判断加载方向并请求网络，随后写入 Room。
     */
    override fun loadSingle(
        loadType: LoadType,
        state: PagingState<Int, ArticleDetailData>
    ): Single<MediatorResult> {
        return when (loadType) {
            LoadType.REFRESH -> {
                Utils.logcat("RemoteMediator", "LoadType REFRESH")
                // REFRESH 对应首页加载，传递 0 加载第一页
                loadPage(0, loadType)
            }

            LoadType.PREPEND -> {
                Utils.logcat("RemoteMediator", "LoadType PREPEND")
                // 本示例为单向追加列表，首页即为起始位置，无需向前加载，直接返回分页结束
                Single.just(MediatorResult.Success(endOfPaginationReached = true))
            }

            LoadType.APPEND -> {
                Utils.logcat("RemoteMediator", "LoadType APPEND")

                // 查询 RemoteKey 表获取下一个分页页码（也可以使用 state.lastItemOrNull() 基于最后一条数据的 id 进行游标分页）
                Single.fromCallable {
                    val remoteKey = remoteKeyDao.remoteKeyByTagSync(tag)
                    val nextKey = remoteKey?.nextPageKey
                    remoteKey to nextKey
                }
                    .subscribeOn(Schedulers.io())
                    .flatMap { (remoteKey, nextKey) ->
                        // 追加时必须明确检查 nextKey 是否为 null：
                        // 1. 若 remoteKey != null 且 nextKey == null，表示上一页已是末尾，分页结束；
                        // 2. 若 remoteKey == null，表示初始 REFRESH 尚未写入有效 Key，此时不应加载更多，等待刷新完成。
                        if (nextKey == null) {
                            Single.just(MediatorResult.Success(endOfPaginationReached = remoteKey != null))
                        } else {
                            loadPage(nextKey, loadType)
                        }
                    }
            }
        }
    }

    /**
     * 发起网络请求加载文章列表并将数据写入 Room 数据库事务中
     */
    private fun loadPage(page: Int, loadType: LoadType): Single<MediatorResult> {
        return networkApi.getArticleSingle(page)
            .subscribeOn(Schedulers.io())
            .map { response ->
                val articles = response.data?.datas ?: emptyList()

                if (loadType == LoadType.REFRESH) {
                    // 下拉刷新时清空旧的 RemoteKey
                    remoteKeyDao.deleteByTagSync(tag)
                }

                val curPage = response.data?.curPage ?: 0
                val nextPage = if (articles.isEmpty()) null else curPage

                // 更新 RemoteKey 为下一页页码
                remoteKeyDao.insertKeySync(RemoteKeyData(tag, nextPage))

                // 将文章列表插入 Room 数据库事务中，Room 会自动使关联的 PagingSource 失效以刷新 UI
                articleDatabase.runInTransaction {
                    if (loadType == LoadType.REFRESH) {
                        articleDao.deleteAllArticlesSync()
                    }
                    articleDao.insertArticlesSync(articles.map { article ->
                        article.copy(page = curPage)
                    })
                }

                // 若本次网络请求返回的数据为空，说明已达到最后一页
                MediatorResult.Success(endOfPaginationReached = articles.isEmpty()) as MediatorResult
            }
            .onErrorResumeNext { e ->
                if (e is IOException || e is HttpException) {
                    Single.just(MediatorResult.Error(e))
                } else {
                    Single.error(e)
                }
            }
    }

    companion object {
        private const val tag = "article"
    }
}
