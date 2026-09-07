package com.example.william.my.basic.basic_datastore

import android.content.Context
import com.example.william.my.basic.basic_model.ChangeListVersions
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val MMAP_ID = "sync_preferences"
private const val KEY_ARTICLE_VERSION = "article_version"

/**
 * 增量同步版本游标持久化数据源（基于 Tencent MMKV 键值存储实现）
 *
 * 对齐 Google Now in Android 的 NiaPreferencesDataSource 架构契约：
 * 1. 【存储内核】：采用 Tencent MMKV（基于 mmap 内存映射），读写无延迟且进程异常退出数据不丢失；
 * 2. 【响应式抽象】：通过 [MutableStateFlow] 在内存持有最新快照并向外暴露 [Flow]，保持与上层响应式架构（Flow / StateFlow）无缝融合；
 * 3. 【多进程安全】：使用 [MMKV.MULTI_PROCESS_MODE] 确保后台 WorkManager 独立进程同步时数据版本一致；
 * 4. 【环境降级兼容】：在无 Native so 环境（如本地纯 JVM 单元测试）下提供内存 Fallback 容错，避免 UnsatisfiedLinkError 导致崩溃。
 */
private fun createMmkvInstance(context: Context): MMKV? = try {
    MMKV.initialize(context)
    MMKV.mmkvWithID(MMAP_ID, MMKV.MULTI_PROCESS_MODE)
} catch (_: Throwable) {
    null
}

class SyncPreferencesDataSource internal constructor(
    private val mmkv: MMKV?,
) {

    constructor(context: Context) : this(createMmkvInstance(context))

    @Volatile
    private var fallbackArticleVersion: Int = 0

    private val _changeListVersions: MutableStateFlow<ChangeListVersions> =
        MutableStateFlow(readVersions())

    /**
     * 响应式观察当前游标版本
     */
    val changeListVersions: Flow<ChangeListVersions> = _changeListVersions.asStateFlow()

    /**
     * 获取当前版本游标快照（支持协程挂起契约）
     */
    suspend fun getChangeListVersions(): ChangeListVersions = readVersions()

    /**
     * 同步获取当前版本游标快照（MMKV mmap 同步读取零耗时优势）
     */
    fun getChangeListVersionsSync(): ChangeListVersions = readVersions()

    /**
     * 原子更新版本游标
     */
    suspend fun updateChangeListVersions(update: ChangeListVersions.() -> ChangeListVersions) {
        synchronized(this) {
            val current = readVersions()
            val updated = current.update()
            writeVersions(updated)
            _changeListVersions.value = updated
        }
    }

    private fun readVersions(): ChangeListVersions {
        val articleVersion = mmkv?.decodeInt(KEY_ARTICLE_VERSION, 0) ?: fallbackArticleVersion
        return ChangeListVersions(
            articleVersion = articleVersion,
        )
    }

    private fun writeVersions(versions: ChangeListVersions) {
        fallbackArticleVersion = versions.articleVersion
        mmkv?.encode(KEY_ARTICLE_VERSION, versions.articleVersion)
    }
}

/**
 * MMKV 数据源别名，保持语义清晰度
 */
typealias SyncMmkvDataSource = SyncPreferencesDataSource
