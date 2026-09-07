package com.example.william.my.basic.basic_model

/**
 * 文章业务领域实体模型（对齐 Now in Android 的 Domain Model）
 *
 * 位于纯 JVM 领域模型层，不绑定具体持久化框架（如 Room）与网络序列化细节。
 *
 * @param id 文章唯一标识
 * @param title 文章标题
 * @param link 文章链接
 * @param page 所属分页编号
 */
data class ArticleDetailData(
    val id: String = "",
    val title: String = "",
    val link: String = "",
    val page: Int = -1,
)
