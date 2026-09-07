package com.example.william.my.core.base.network

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * [TestNetworkMonitor] 单元测试
 */
class TestNetworkMonitorTest {

    @Test
    fun networkMonitor_defaultState_emitsTrue() = runTest {
        val networkMonitor = TestNetworkMonitor()
        assertTrue(networkMonitor.isOnline.first())
    }

    @Test
    fun networkMonitor_setOffline_emitsFalse() = runTest {
        val networkMonitor = TestNetworkMonitor()
        networkMonitor.setConnected(false)
        assertFalse(networkMonitor.isOnline.first())
    }

    @Test
    fun networkMonitor_reconnect_emitsTrue() = runTest {
        val networkMonitor = TestNetworkMonitor()
        networkMonitor.setConnected(false)
        assertFalse(networkMonitor.isOnline.first())

        networkMonitor.setConnected(true)
        assertTrue(networkMonitor.isOnline.first())
    }
}
