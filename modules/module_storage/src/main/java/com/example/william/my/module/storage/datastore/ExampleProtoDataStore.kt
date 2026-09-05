@file:Suppress("BlockingMethodInNonBlockingContext")

package com.example.william.my.module.storage.datastore

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.example.william.my.module.storage.proto.Settings
import com.google.protobuf.InvalidProtocolBufferException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
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

// 顶层单例属性委托（官方推荐最佳实践）
private val Context.protoDataStore: DataStore<Settings> by dataStore(
    fileName = "jetpack_settings.pb",
    serializer = SettingsSerializer,
)

/**
 * Proto DataStore 示例：基于 Protocol Buffers 的类型安全结构化存储。
 */
class ExampleProtoDataStore(private val context: Context) {

    private val dataStore: DataStore<Settings> = context.protoDataStore

    /**
     * 读取 Proto 中的计数器 Flow（带异常捕获与默认值处理）
     */
    fun getCounter(): Flow<Int> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(Settings.getDefaultInstance())
            } else {
                throw exception
            }
        }
        .map { settings ->
            settings.exampleCounter
        }

    /**
     * 自增 Proto 计数器
     */
    suspend fun incrementCounter() {
        dataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setExampleCounter(currentSettings.exampleCounter + 1)
                .build()
        }
    }

    /**
     * 清空 Proto 存储数据
     */
    suspend fun clear() {
        dataStore.updateData {
            it.toBuilder()
                .clear()
                .build()
        }
    }
}
