package com.example.william.my.basic.basic_network.model

import com.example.william.my.basic.basic_model.ArticleDetailData

/**
 * 文章列表网络数据响应（当前页码 + 文章详情列表）
 */
data class ArticleData(
    val curPage: Int = 0,
    val datas: List<ArticleDetailData> = emptyList(),
)
