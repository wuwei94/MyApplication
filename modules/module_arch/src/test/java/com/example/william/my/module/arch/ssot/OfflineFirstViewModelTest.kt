package com.example.william.my.module.arch.ssot

import app.cash.turbine.test
import com.example.william.my.basic.basic_datastore.SyncPreferencesDataSource
import com.example.william.my.basic.basic_model.ArticleDetailData
import com.example.william.my.module.arch.fake.FakeArticleRepository
import com.example.william.my.module.arch.fake.TestNetworkMonitor
import com.example.william.my.module.arch.fake.TestSyncManager
import com.example.william.my.module.arch.ssot.data.OfflineFirstIntent
import com.example.william.my.module.arch.ssot.viewmodel.OfflineFirstViewModel
import com.example.william.my.module.arch.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * 离线优先（SSOT）架构 ViewModel 单元测试
 *
 * 规范约定：
 * 1. 采用手写内存 Fake 数据源，不引入 Mockito 模拟框架；
 * 2. 采用 [MainDispatcherRule] 重定向主调度器；
 * 3. 采用 Turbine 断言响应式单一不可变状态 [OfflineFirstViewModel.uiState] 的演进；
 * 4. 测试方法遵循 `被测对象_场景_预期结果` 规范。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OfflineFirstViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: FakeArticleRepository
    private lateinit var networkMonitor: TestNetworkMonitor
    private lateinit var syncManager: TestSyncManager
    private lateinit var syncPreferences: SyncPreferencesDataSource
    private lateinit var viewModel: OfflineFirstViewModel

    @Before
    fun setUp() {
        repository = FakeArticleRepository()
        networkMonitor = TestNetworkMonitor()
        syncManager = TestSyncManager()
        syncPreferences = createTestSyncPreferences()

        viewModel = OfflineFirstViewModel(
            repository = repository,
            networkMonitor = networkMonitor,
            syncManager = syncManager,
            syncPreferences = syncPreferences,
        )
    }

    private fun createTestSyncPreferences(): SyncPreferencesDataSource {
        val constructor = SyncPreferencesDataSource::class.java.getDeclaredConstructor(
            Class.forName("com.tencent.mmkv.MMKV"),
        )
        constructor.isAccessible = true
        return constructor.newInstance(null) as SyncPreferencesDataSource
    }

    @Test
    fun uiState_initialCache_emitsPopulatedArticles() = runTest {
        val initialData = listOf(
            ArticleDetailData(id = "1", title = "Room 历史缓存文章", link = "https://example.com/1", page = 0),
        )
        repository.emitArticles(initialData)

        viewModel.uiState.test {
            val item = awaitItem()
            // 验证 SSOT：UI 状态中的文章与数量完全同步于 Repository 本地数据流
            assertEquals(1, item.articles.size)
            assertEquals("Room 历史缓存文章", item.articles.first().title)
            assertEquals(1, item.cacheCount)
            assertTrue(item.isOnline)
            assertFalse(item.isSyncing)
        }
    }

    @Test
    fun uiState_insertLocalArticle_automaticallyUpdatesStream() = runTest {
        viewModel.uiState.test {
            // 初始为空状态
            assertEquals(0, awaitItem().articles.size)

            // 发送本地插入意图
            viewModel.sendIntent(OfflineFirstIntent.AddLocalArticle("本地即时新增文章"))

            // 验证响应式数据流自动推流更新
            val updated = awaitItem()
            assertEquals(1, updated.articles.size)
            assertEquals("本地即时新增文章", updated.articles.first().title)
            assertEquals(1, updated.cacheCount)
        }
    }

    @Test
    fun uiState_clearLocalCache_emptiesArticlesList() = runTest {
        repository.emitArticles(
            listOf(ArticleDetailData(id = "1", title = "待清理文章", link = "", page = 0)),
        )

        viewModel.uiState.test {
            assertEquals(1, awaitItem().articles.size)

            // 发送清空本地缓存意图
            viewModel.sendIntent(OfflineFirstIntent.ClearLocalCache)

            // 验证数据表清空后流式反应
            val cleared = awaitItem()
            assertTrue(cleared.articles.isEmpty())
            assertEquals(0, cleared.cacheCount)
        }
    }

    @Test
    fun networkMonitor_networkReconnects_triggersAutoHealingSync() = runTest {
        // 先模拟断网
        networkMonitor.setConnected(false)

        // 重新构建 ViewModel 捕捉断网转连网边缘
        val healingViewModel = OfflineFirstViewModel(
            repository = repository,
            networkMonitor = networkMonitor,
            syncManager = syncManager,
            syncPreferences = syncPreferences,
        )

        val beforeCount = syncManager.syncRequestedCount

        // 模拟网络恢复
        networkMonitor.setConnected(true)

        // 验证在线自愈机制触发了一次增量同步
        assertTrue(syncManager.syncRequestedCount > beforeCount)
    }
}
