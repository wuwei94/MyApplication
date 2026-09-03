package com.example.william.my.basic.basic_repo.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ArticleData(
    val curPage: Int = 0,
    val datas: List<ArticleDetailData> = emptyList()
) : Parcelable
