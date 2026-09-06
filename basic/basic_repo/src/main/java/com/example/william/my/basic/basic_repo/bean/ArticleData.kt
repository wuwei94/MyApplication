package com.example.william.my.basic.basic_repo.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 文章列表数据（当前页码 + 文章详情列表）
 */
@Parcelize
data class ArticleData(
    val curPage: Int = 0,
    val datas: List<ArticleDetailData> = emptyList(),
) : Parcelable
