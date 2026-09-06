package com.example.william.my.module.jetpack.paging.source

import androidx.paging.PagingState
import androidx.paging.rxjava3.RxPagingSource
import com.example.william.my.basic.basic_repo.api.ArticleRxApi
import com.example.william.my.basic.basic_repo.bean.ArticleData
import com.example.william.my.basic.basic_repo.bean.ArticleDetailData
import com.example.william.my.core.retrofit.response.RetrofitResponse
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

/**
 * 文章 Rx 分页数据源
 *
 * 基于 RxJava 的分页数据源，实现 RxPagingSource 的加载逻辑。
 */
class ArticleRxPagingSource(
    private val networkApi: ArticleRxApi,
) : RxPagingSource<Int, ArticleDetailData>() {

    /**
     * 加载指定页码的文章数据
     */
    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, ArticleDetailData>> {
        // 如果未定义，从 0 开始刷新。
        val nextPageNumber = params.key ?: 0

        return networkApi
            .getArticleSingle(nextPageNumber)
            .subscribeOn(Schedulers.io())
            .map { response ->
                toLoadResult(response)
            }
            .onErrorReturn { throwable ->
                LoadResult.Error(throwable)
            }
    }

    private fun toLoadResult(response: RetrofitResponse<ArticleData>): LoadResult<Int, ArticleDetailData> {
        val data = response.data
        return if (data != null) {
            LoadResult.Page(
                data = data.datas,
                prevKey = null, // 仅向前分页。
                nextKey = data.curPage,
            )
        } else {
            LoadResult.Error(NullPointerException("Response data is null"))
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ArticleDetailData>): Int? {
        // 尝试找到距离 anchorPosition 最近的页的 key，从 prevKey 或 nextKey 中选取，
        // 但需要在此处处理可空性：
        //  * prevKey == null -> anchorPage 是第一页。
        //  * nextKey == null -> anchorPage 是最后一页。
        //  * prevKey 和 nextKey 都为 null -> anchorPage 是初始页，直接返回 null。
        val anchorPosition = state.anchorPosition
            ?: return null

        val (_, prevKey, nextKey) = state.closestPageToPosition(anchorPosition)
            ?: return null

        if (prevKey != null) {
            return prevKey + 1
        }

        if (nextKey != null) {
            return nextKey - 1
        }

        return null
    }
}
