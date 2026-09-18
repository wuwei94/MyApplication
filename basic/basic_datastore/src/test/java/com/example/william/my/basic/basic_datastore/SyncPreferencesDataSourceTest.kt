package com.example.william.my.basic.basic_datastore

import com.example.william.my.basic.basic_model.ChangeListVersions
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * [SyncPreferencesDataSource] 游标持久化数据源单元测试
 *
 * 验证基于内存 DataStore 替身的游标初始读取、原子推进与响应式 Flow 推流。
 */
class SyncPreferencesDataSourceTest {

    private lateinit var dataSource: SyncPreferencesDataSource

    @Before
    fun setUp() {
        dataSource = SyncPreferencesDataSource.createInMemoryForTest()
    }

    @Test
    fun syncPreferencesDataSource_initialVersion_defaultsToZero() = runTest {
        assertEquals(0, dataSource.getChangeListVersions().articleVersion)
        assertEquals(0, dataSource.changeListVersions.first().articleVersion)
    }

    @Test
    fun syncPreferencesDataSource_updateVersion_updatesFlowAndReturnsLatest() = runTest {
        dataSource.updateChangeListVersions {
            copy(articleVersion = 42)
        }

        assertEquals(42, dataSource.getChangeListVersions().articleVersion)
        assertEquals(42, dataSource.changeListVersions.first().articleVersion)
    }

    @Test
    fun syncPreferencesDataSource_createInMemoryForTest_seedsInitialVersion() = runTest {
        val seeded = SyncPreferencesDataSource.createInMemoryForTest(
            initial = ChangeListVersions(articleVersion = 7),
        )

        assertEquals(7, seeded.getChangeListVersions().articleVersion)
        assertEquals(7, seeded.changeListVersions.first().articleVersion)
    }
}
