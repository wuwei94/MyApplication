package com.example.william.my.basic.basic_sync

import android.content.Context
import com.example.william.my.basic.basic_datastore.SyncPreferencesDataSource
import com.example.william.my.basic.basic_repo.sync.SyncManager
import com.example.william.my.basic.basic_sync.work.SyncWorker

/**
 * 增量同步框架门面（对齐 Now in Android 的 Sync 入口）
 */
object Sync {

    @Volatile
    private var syncManager: SyncManager? = null

    @Volatile
    private var syncPreferencesDataSource: SyncPreferencesDataSource? = null

    /**
     * 初始化后台增量同步（如在 Application 启动时调用）
     */
    fun initialize(context: Context) {
        SyncWorker.startUpSyncWork(context)
    }

    /**
     * 获取 [SyncManager] 单例
     */
    fun provideSyncManager(context: Context): SyncManager = syncManager ?: synchronized(this) {
        syncManager ?: WorkManagerSyncManager(context.applicationContext).also {
            syncManager = it
        }
    }

    /**
     * 获取 [SyncPreferencesDataSource] 单例
     */
    fun provideSyncPreferencesDataSource(context: Context): SyncPreferencesDataSource = syncPreferencesDataSource ?: synchronized(this) {
        syncPreferencesDataSource ?: SyncPreferencesDataSource(context.applicationContext).also {
            syncPreferencesDataSource = it
        }
    }
}
