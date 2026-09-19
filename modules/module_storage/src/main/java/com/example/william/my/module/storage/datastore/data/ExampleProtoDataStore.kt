@file:Suppress("BlockingMethodInNonBlockingContext")

package com.example.william.my.module.storage.datastore.data

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.example.william.my.module.storage.proto.Settings
import com.example.william.my.module.storage.proto.ThemeMode
import com.google.protobuf.InvalidProtocolBufferException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * Protocol Buffers 序列化器
 */
object SettingsSerializer : Serializer<Settings> {

    override val defaultValue: Settings = Settings.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): Settings {
        try {
            return Settings.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: Settings, output: OutputStream) {
        t.writeTo(output)
    }
}

// 顶层单例属性委托（官方推荐最佳实践，避免同文件多实例竞争）
private val Context.protoDataStore: DataStore<Settings> by dataStore(
    fileName = "jetpack_settings.pb",
    serializer = SettingsSerializer,
)

/**
 * Proto DataStore 示例：基于 Protobuf schema 的类型安全偏好对象存储。
 *
 * 面向单文件应用偏好（主题、语言、开关、功能旗标等）；多实体、检索与关系查询归 Room。
 */
class ExampleProtoDataStore(private val context: Context) {

    private val dataStore: DataStore<Settings> = context.protoDataStore

    /**
     * 观察整份偏好对象 Flow（带异常捕获与默认值处理）
     */
    fun getSettings(): Flow<Settings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(Settings.getDefaultInstance())
            } else {
                throw exception
            }
        }

    /**
     * 多字段原子写入：主题 / 语言 / 通知开关在同一次 updateData 事务内落盘
     */
    suspend fun updateUserPreferences(
        themeMode: ThemeMode,
        preferredLanguage: String,
        notificationEnabled: Boolean,
    ) {
        dataStore.updateData { current ->
            current.toBuilder()
                .setThemeMode(themeMode)
                .setPreferredLanguage(preferredLanguage)
                .setNotificationEnabled(notificationEnabled)
                .build()
        }
    }

    /**
     * 切换主题模式（枚举字段，编译期类型安全）
     */
    suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.updateData { current ->
            current.toBuilder()
                .setThemeMode(themeMode)
                .build()
        }
    }

    /**
     * 功能开关增删：repeated 字段已存在则移除，否则追加
     */
    suspend fun toggleFeatureFlag(flag: String) {
        dataStore.updateData { current ->
            val flags = current.enabledFeatureFlagsList.toMutableList()
            if (!flags.remove(flag)) {
                flags.add(flag)
            }
            current.toBuilder()
                .clearEnabledFeatureFlags()
                .addAllEnabledFeatureFlags(flags)
                .build()
        }
    }

    /**
     * 自增示例计数器
     */
    suspend fun incrementCounter() {
        dataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setExampleCounter(currentSettings.exampleCounter + 1)
                .build()
        }
    }

    /**
     * 清空 Proto 存储数据（重置为 schema 默认值）
     */
    suspend fun clear() {
        dataStore.updateData {
            it.toBuilder()
                .clear()
                .build()
        }
    }
}
