package com.example.william.my.module.jetpack.paging.remotekey.dao

import androidx.annotation.WorkerThread
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.william.my.module.jetpack.paging.remotekey.data.RemoteKeyData

/**
 * 远程分页键（RemoteKey）数据访问接口。
 *
 * 配合 [androidx.paging.RemoteMediator] 使用，持久化保存每个分页业务（以 tag 为主键）的下一页页码与最后更新时间戳。
 */
@Dao
interface RemoteKeyDao {

    /**
     * 插入或更新远程分页 Key 记录（挂起函数）。
     *
     * @param key 远程分页 Key 数据实体
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKey(key: RemoteKeyData)

    /**
     * 同步阻塞插入或更新远程分页 Key 记录。
     *
     * 注意：属于同步 I/O 操作，禁止在主线程直接调用。
     *
     * @param key 远程分页 Key 数据实体
     */
    @WorkerThread
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertKeySync(key: RemoteKeyData)

    /**
     * 根据业务标识 tag 查询对应的 RemoteKey 记录（挂起函数）。
     *
     * @param tag 业务标识（如 "article"）
     * @return 对应的 [RemoteKeyData] 实体，若不存在则返回 null
     */
    @Query("SELECT * FROM RemoteKey WHERE tag = :tag")
    suspend fun remoteKeyByTag(tag: String): RemoteKeyData?

    /**
     * 同步阻塞根据业务标识 tag 查询对应的 RemoteKey 记录。
     *
     * 注意：属于同步 I/O 操作，禁止在主线程直接调用。
     *
     * @param tag 业务标识（如 "article"）
     * @return 对应的 [RemoteKeyData] 实体，若不存在则返回 null
     */
    @WorkerThread
    @Query("SELECT * FROM RemoteKey WHERE tag = :tag")
    fun remoteKeyByTagSync(tag: String): RemoteKeyData?

    /**
     * 根据业务标识 tag 删除对应的 RemoteKey 记录（挂起函数）。
     *
     * @param tag 业务标识（如 "article"）
     */
    @Query("DELETE FROM RemoteKey WHERE tag = :tag")
    suspend fun deleteByTag(tag: String)

    /**
     * 同步阻塞根据业务标识 tag 删除对应的 RemoteKey 记录。
     *
     * 注意：属于同步 I/O 操作，禁止在主线程直接调用。
     *
     * @param tag 业务标识（如 "article"）
     */
    @WorkerThread
    @Query("DELETE FROM RemoteKey WHERE tag = :tag")
    fun deleteByTagSync(tag: String)

    /**
     * 查询 RemoteKey 表中最新的创建/更新时间戳（挂起函数）。
     *
     * 用于在 RemoteMediator 初始化时判断本地缓存是否过期（如超过 1 小时）。
     *
     * @return 最新的时间戳毫秒数，若表为空则返回 null
     */
    @Query("Select createdAt From RemoteKey Order By createdAt DESC LIMIT 1")
    suspend fun lastUpdated(): Long?

    /**
     * 同步阻塞查询 RemoteKey 表中最新的创建/更新时间戳。
     *
     * 用于在 [com.example.william.my.module.jetpack.paging.mediator.ArticleRxRemoteMediator.initializeSingle] 中判断本地缓存是否过期。
     *
     * 注意：属于同步 I/O 操作，禁止在主线程直接调用。
     *
     * @return 最新的时间戳毫秒数，若表为空则返回 null
     */
    @WorkerThread
    @Query("Select createdAt From RemoteKey Order By createdAt DESC LIMIT 1")
    fun lastUpdatedSync(): Long?
}
