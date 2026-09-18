package com.example.william.my.basic.basic_datastore

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.example.william.my.basic.basic_datastore.proto.SyncPreferences
import com.example.william.my.basic.basic_model.ChangeListVersions
import com.google.protobuf.InvalidProtocolBufferException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.InputStream
import java.io.OutputStream

private const val SYNC_PREFERENCES_FILE = "sync_preferences.pb"

/**
 * Proto DataStore 序列化器：游标文件损坏时抛出 [CorruptionException]，由 DataStore 统一处理
 */
object SyncPreferencesSerializer : Serializer<SyncPreferences> {

    override val defaultValue: SyncPreferences = SyncPreferences.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): SyncPreferences {
        try {
            return SyncPreferences.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: SyncPreferences, output: OutputStream) {
        t.writeTo(output)
    }
}

private val Context.syncPreferencesDataStore: DataStore<SyncPreferences> by dataStore(
    fileName = SYNC_PREFERENCES_FILE,
    serializer = SyncPreferencesSerializer,
)

/**
 * 增量同步版本游标持久化数据源（Proto DataStore）
 *
 * 对齐 Google Now in Android 的 NiaPreferencesDataSource 架构契约：
 * 1. 【存储内核】：Proto DataStore + Protobuf schema，游标字段编译期类型安全；
 * 2. 【响应式抽象】：对外暴露 [Flow]，与上层 Flow / StateFlow 响应式架构无缝融合；
 * 3. 【读写分离】：读走 `data` 冷流，写走 `updateData` 原子变换，保证进程内游标一致；
 * 4. 【单进程约束】：DataStore 实例文件级独占，与 WorkManager 默认同进程调度模型一致。
 */
class SyncPreferencesDataSource internal constructor(
    private val dataStore: DataStore<SyncPreferences>,
) {

    constructor(context: Context) : this(context.syncPreferencesDataStore)

    /**
     * 响应式观察当前游标版本
     */
    val changeListVersions: Flow<ChangeListVersions> = dataStore.data
        .map { it.toChangeListVersions() }

    /**
     * 获取当前版本游标快照（支持协程挂起契约）
     */
    suspend fun getChangeListVersions(): ChangeListVersions = dataStore.data.first().toChangeListVersions()

    /**
     * 原子更新版本游标
     */
    suspend fun updateChangeListVersions(update: ChangeListVersions.() -> ChangeListVersions) {
        dataStore.updateData { preferences ->
            val updated = preferences.toChangeListVersions().update()
            preferences.toBuilder()
                .setArticleVersion(updated.articleVersion)
                .build()
        }
    }

    companion object {
        /**
         * 纯 JVM 单元测试用：内存 DataStore 替身，不落盘、不依赖 Android 文件路径
         */
        fun createInMemoryForTest(
            initial: ChangeListVersions = ChangeListVersions(),
        ): SyncPreferencesDataSource = SyncPreferencesDataSource(
            InMemorySyncPreferencesDataStore(
                SyncPreferences.newBuilder()
                    .setArticleVersion(initial.articleVersion)
                    .build(),
            ),
        )
    }
}

/**
 * 内存版 [DataStore]：满足同一读写契约，供单测注入
 */
private class InMemorySyncPreferencesDataStore(
    initial: SyncPreferences,
) : DataStore<SyncPreferences> {

    private val state = MutableStateFlow(initial)

    override val data: Flow<SyncPreferences> = state

    override suspend fun updateData(
        transform: suspend (t: SyncPreferences) -> SyncPreferences,
    ): SyncPreferences {
        val updated = transform(state.value)
        state.value = updated
        return updated
    }
}

private fun SyncPreferences.toChangeListVersions(): ChangeListVersions = ChangeListVersions(
    articleVersion = articleVersion,
)
