package com.example.william.my.module.jetpack.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.flow.Flow

@Dao
interface OAuthDao {

    /**
     * 插入单条用户信息
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOAuth(oAuth: OAuth): Long

    /**
     * 批量插入用户信息
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg users: OAuth)

    /**
     * 更新用户信息
     */
    @Update
    suspend fun updateOAuth(oAuth: OAuth)

    /**
     * 根据主键 ID 获取用户信息
     */
    @Query("SELECT * FROM oauth WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Long): OAuth?

    /**
     * 删除单条记录
     */
    @Delete
    suspend fun deleteOAuth(oAuth: OAuth)

    /**
     * 删除全部用户信息
     */
    @Query("DELETE FROM oauth")
    suspend fun deleteAllOAuth()

    /**
     * 获取全部用户列表（协程 Flow 响应式查询）
     */
    @Query("SELECT * FROM oauth ORDER BY id DESC")
    fun getAllOAuthFlow(): Flow<List<OAuth>>

    /**
     * 根据 ID 查询用户（RxJava Single 响应式查询）
     */
    @Query("SELECT * FROM oauth WHERE id = :userId")
    fun getUserSingle(userId: Long): Single<OAuth>

    /**
     * 获取全部用户（RxJava Flowable 响应式查询）
     */
    @Query("SELECT * FROM oauth ORDER BY id DESC")
    fun getAllOAuthFlowable(): Flowable<List<OAuth>>
}