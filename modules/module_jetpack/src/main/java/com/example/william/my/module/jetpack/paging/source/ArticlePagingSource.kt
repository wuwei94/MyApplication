package com.example.william.my.module.jetpack.paging.source

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.william.my.basic.basic_repo.api.ArticleApi
import com.example.william.my.basic.basic_repo.bean.ArticleDetailData

/**
 * 文章分页数据源
 *
 * 基于网络的分页数据源，实现 PagingSource 的加载与刷新逻辑。
 */
class ArticlePagingSource(
    private val networkApi: ArticleApi,
) : PagingSource<Int, ArticleDetailData>() {

    /**
     * 加载指定页码的文章数据
     */
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ArticleDetailData> = try {
        // 如果未定义，从0开始刷新
        val nextPageNumber = params.key ?: 0

        val response = networkApi.getArticleSuspend(nextPageNumber)

        LoadResult.Page(
            data = response.data!!.datas,
            prevKey = null, // 仅向前分页。
            nextKey = response.data!!.curPage,
        )
    } catch (e: Exception) {
        // 处理错误，返回 LoadResult.Error()
        LoadResult.Error(e)
    }

    override fun getRefreshKey(state: PagingState<Int, ArticleDetailData>): Int? {
        // 尝试找到距离 anchorPosition 最近的页的 key，从 prevKey 或 nextKey 中选取，
        // 但需要在此处处理可空性：
        //  * prevKey == null -> anchorPage 是第一页。
        //  * nextKey == null -> anchorPage 是最后一页。
        //  * prevKey 和 nextKey 都为 null -> anchorPage 是初始页，直接返回 null。
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}
