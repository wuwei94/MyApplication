package com.example.william.my.basic.basic_repo.sync.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.william.my.basic.basic_repo.sync.model.ChangeListVersions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.syncDataStore: DataStore<Preferences> by preferencesDataStore(name = "sync_preferences")

/**
 * 增量同步版本游标持久化数据源
 *
 * 基于 Jetpack DataStore Preferences 存储当前已同步的各领域版本游标，
 * 确保进程重启或应用被杀后游标不丢失。
 */
class SyncPreferencesDataSource(
    private val context: Context,
) {

    private val articleVersionKey = intPreferencesKey("article_version")

    /**
     * 响应式观察当前游标版本
     */
    val changeListVersions: Flow<ChangeListVersions> = context.syncDataStore.data.map { preferences ->
        ChangeListVersions(
            articleVersion = preferences[articleVersionKey] ?: 0,
        )
    }

    /**
     * 获取当前版本游标快照
     */
    suspend fun getChangeListVersions(): ChangeListVersions = changeListVersions.first()

    /**
     * 原子更新版本游标
     */
    suspend fun updateChangeListVersions(update: ChangeListVersions.() -> ChangeListVersions) {
        context.syncDataStore.edit { preferences ->
            val current = ChangeListVersions(
                articleVersion = preferences[articleVersionKey] ?: 0,
            )
            val updated = current.update()
            preferences[articleVersionKey] = updated.articleVersion
        }
    }
}
