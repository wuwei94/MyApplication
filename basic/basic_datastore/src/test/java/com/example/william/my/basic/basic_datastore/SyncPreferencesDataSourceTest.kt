package com.example.william.my.basic.basic_datastore

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * [SyncPreferencesDataSource] 游标持久化数据源单元测试
 *
 * 验证基于 MMKV / 内存 Fallback 的游标初始读取、原子推进与响应式 Flow 推流。
 */
class SyncPreferencesDataSourceTest {

    private lateinit var dataSource: SyncPreferencesDataSource

    @Before
    fun setUp() {
        // 传入 null 模拟纯 JVM 单元测试环境（触发内存 Fallback 容错逻辑）
        dataSource = SyncPreferencesDataSource(mmkv = null)
    }

    @Test
    fun syncPreferencesDataSource_initialVersion_defaultsToZero() = runTest {
        // 初始版本游标为 0
        assertEquals(0, dataSource.getChangeListVersions().articleVersion)
        assertEquals(0, dataSource.getChangeListVersionsSync().articleVersion)
        assertEquals(0, dataSource.changeListVersions.first().articleVersion)
    }

    @Test
    fun syncPreferencesDataSource_updateVersion_updatesFlowAndReturnsLatest() = runTest {
        // 触发游标版本原子推进
        dataSource.updateChangeListVersions {
            copy(articleVersion = 42)
        }

        // 验证读取结果已推进
        assertEquals(42, dataSource.getChangeListVersions().articleVersion)
        assertEquals(42, dataSource.getChangeListVersionsSync().articleVersion)
        assertEquals(42, dataSource.changeListVersions.first().articleVersion)
    }
}
