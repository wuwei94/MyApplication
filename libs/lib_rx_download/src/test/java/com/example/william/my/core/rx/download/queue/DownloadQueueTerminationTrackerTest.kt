package com.example.william.my.core.rx.download.queue

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadQueueTerminationTrackerTest {

    @Test
    fun closeWaitsForAllStartedOperations() {
        var finallyCount = 0
        val tracker = DownloadQueueTerminationTracker { finallyCount++ }

        assertTrue(tracker.start())
        assertTrue(tracker.start())
        tracker.close()
        assertEquals(0, finallyCount)

        tracker.finish()
        assertEquals(0, finallyCount)

        tracker.finish()
        assertEquals(1, finallyCount)
    }

    @Test
    fun closedTrackerRejectsNewOperationsAndNotifiesOnce() {
        var finallyCount = 0
        val tracker = DownloadQueueTerminationTracker { finallyCount++ }

        tracker.close()
        tracker.close()

        assertFalse(tracker.start())
        assertEquals(1, finallyCount)
    }
}
