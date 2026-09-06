package com.example.william.my.module.jetpack.paging.remotekey.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 远程键数据实体
 *
 * RemoteMediator 用于记录分页游标的 Room 实体。
 */
@Entity(tableName = "RemoteKey")
data class RemoteKeyData(
    @PrimaryKey
    @ColumnInfo(name = "tag", collate = ColumnInfo.NOCASE) val tag: String,
    @ColumnInfo(name = "nextPage") val nextPageKey: Int?,
    @ColumnInfo(name = "createdAt") val createdAt: Long? = System.currentTimeMillis(),
)
