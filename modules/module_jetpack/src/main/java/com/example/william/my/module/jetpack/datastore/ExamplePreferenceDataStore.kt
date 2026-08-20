package com.example.william.my.module.jetpack.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// 顶层单例属性委托（官方推荐最佳实践，避免多实例竞争导致异常）
private val Context.preferenceDataStore: DataStore<Preferences> by preferencesDataStore(name = "jetpack_preferences_settings")

/**
 * Preferences DataStore 示例：基于键值对存储，提供类型安全的 Flow 查询与协程事务写入。
 */
class ExamplePreferenceDataStore(private val context: Context) {

    private val dataStore: DataStore<Preferences> = context.preferenceDataStore

    /**
     * 读取计数器 Flow（带 IOException 捕获与默认值降级）
     */
    fun getCounter(): Flow<Int> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[KEY_COUNTER] ?: 0
            }
    }

    /**
     * 读取用户名 Flow
     */
    fun getUserName(): Flow<String> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[KEY_USER_NAME] ?: "未设置用户名"
            }
    }

    /**
     * 自增计数器
     */
    suspend fun incrementCounter() {
        dataStore.edit { preferences ->
            val currentCounter = preferences[KEY_COUNTER] ?: 0
            preferences[KEY_COUNTER] = currentCounter + 1
        }
    }

    /**
     * 设置用户名
     */
    suspend fun setUserName(name: String) {
        dataStore.edit { preferences ->
            preferences[KEY_USER_NAME] = name
        }
    }

    /**
     * 清空所有 Preferences 键值
     */
    suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    companion object {
        private val KEY_COUNTER = intPreferencesKey("example_counter")
        private val KEY_USER_NAME = stringPreferencesKey("example_user_name")
    }
}