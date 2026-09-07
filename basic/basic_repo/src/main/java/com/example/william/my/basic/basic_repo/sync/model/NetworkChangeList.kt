package com.example.william.my.basic.basic_repo.sync.model

import com.example.william.my.basic.basic_repo.bean.ArticleDetailData

/**
 * 增量数据变更项（ChangeList）
 *
 * 遵循增量同步规范：
 * 远端仅返回高于客户端传入游标版本的变更记录集合，客户端据此按需执行插入、更新或物理删除。
 *
 * @param id 变更实体唯一标识
 * @param changeListVersion 当前变更发生时的全局版本游标（单调自增）
 * @param isDelete 是否为删除事件（true 表示远端已删除，本地需移除）
 * @param article 变更包含的最新实体数据（当 isDelete 为 true 时通常为空）
 */
data class NetworkChangeList(
    val id: String,
    val changeListVersion: Int,
    val isDelete: Boolean = false,
    val article: ArticleDetailData? = null,
)
