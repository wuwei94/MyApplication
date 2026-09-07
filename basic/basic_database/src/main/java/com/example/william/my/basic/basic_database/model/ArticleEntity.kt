package com.example.william.my.basic.basic_database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.william.my.basic.basic_model.ArticleDetailData

/**
 * 文章数据库表实体（对齐 Now in Android 的 Database Entity）
 *
 * 专属由 Room 映射管理，通过扩展函数与领域模型 [ArticleDetailData] 进行双向转换。
 */
@Entity(tableName = "Articles")
data class ArticleEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String = "",
    @ColumnInfo(name = "title") val title: String = "",
    @ColumnInfo(name = "link") val link: String = "",
    @ColumnInfo(name = "page") val page: Int = -1,
)

/**
 * 将数据库实体转换为领域模型
 */
fun ArticleEntity.asExternalModel(): ArticleDetailData = ArticleDetailData(
    id = id,
    title = title,
    link = link,
    page = page,
)

/**
 * 将领域模型转换为数据库实体
 */
fun ArticleDetailData.asEntity(): ArticleEntity = ArticleEntity(
    id = id,
    title = title,
    link = link,
    page = page,
)
