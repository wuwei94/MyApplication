package com.example.william.my.basic.basic_repo.bean

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * 文章详情数据（对应 Room 的 Articles 表实体）
 */
@Parcelize
@Entity(tableName = "Articles")
data class ArticleDetailData(
    @PrimaryKey @ColumnInfo(name = "id") val id: String = "",
    @ColumnInfo(name = "title") val title: String = "",
    @ColumnInfo(name = "link") val link: String = "",
    @ColumnInfo(name = "page") val page: Int = -1,
) : Parcelable
