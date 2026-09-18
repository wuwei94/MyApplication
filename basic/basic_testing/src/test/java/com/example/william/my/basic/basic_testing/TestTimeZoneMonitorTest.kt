package com.example.william.my.basic.basic_testing

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.TimeZone

/**
 * [TestTimeZoneMonitor] 单元测试
 */
class TestTimeZoneMonitorTest {

    @Test
    fun timeZoneMonitor_defaultState_emitsUtc() = runTest {
        val monitor = TestTimeZoneMonitor()
        assertEquals(TimeZone.getTimeZone("UTC"), monitor.currentTimeZone.first())
    }

    @Test
    fun timeZoneMonitor_setTimeZone_emitsUpdatedZone() = runTest {
        val monitor = TestTimeZoneMonitor()
        val shanghai = TimeZone.getTimeZone("Asia/Shanghai")
        monitor.setTimeZone(shanghai)
        assertEquals(shanghai, monitor.currentTimeZone.first())
    }

    @Test
    fun timeZoneMonitor_setUtcAfterOtherZone_emitsUtc() = runTest {
        val monitor = TestTimeZoneMonitor(initialTimeZone = TimeZone.getTimeZone("Asia/Shanghai"))
        assertEquals(TimeZone.getTimeZone("Asia/Shanghai"), monitor.currentTimeZone.first())

        monitor.setTimeZone(TimeZone.getTimeZone("UTC"))
        assertEquals(TimeZone.getTimeZone("UTC"), monitor.currentTimeZone.first())
    }
}
