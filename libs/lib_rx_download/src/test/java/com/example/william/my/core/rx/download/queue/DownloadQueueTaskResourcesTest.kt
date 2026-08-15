package com.example.william.my.core.rx.download.queue

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class DownloadQueueTaskResourcesTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun queueFinallyWaitsForCancelledPreflightToExit() {
        val limiter = DownloadConcurrencyLimiter(1)
        limiter.acquire()
        val registry = DownloadDestinationRegistry()
        val destination = temporaryFolder.newFile("preflight.bin")
        var finallyCount = 0
        val tracker = DownloadQueueTerminationTracker { finallyCount++ }
        val resources = DownloadQueueTaskResources(
            terminationTracker = tracker,
            concurrencyLimiter = limiter,
            destinationRegistry = registry,
            destination = destination,
        )
        val workerFinished = CountDownLatch(1)
        val worker = Thread {
            runCatching(resources::acquire)
            workerFinished.countDown()
        }.apply { isDaemon = true }

        worker.start()
        awaitThreadWaiting(worker)
        assertDestinationInUse(registry, destination)
        tracker.close()
        resources.cancel()

        assertEquals(0, finallyCount)
        limiter.release()
        assertTrue(workerFinished.await(2L, TimeUnit.SECONDS))
        assertEquals(1, finallyCount)
        registry.acquire(destination).release()
    }

    @Test
    fun resourcesRemainHeldUntilPhysicalOperationFinishes() {
        val limiter = DownloadConcurrencyLimiter(1)
        val registry = DownloadDestinationRegistry()
        val destination = temporaryFolder.newFile("physical.bin")
        var finallyCount = 0
        val tracker = DownloadQueueTerminationTracker { finallyCount++ }
        val resources = DownloadQueueTaskResources(
            terminationTracker = tracker,
            concurrencyLimiter = limiter,
            destinationRegistry = registry,
            destination = destination,
        )
        resources.acquire()
        assertTrue(resources.startOperation())

        tracker.close()
        resources.cancel()
        resources.finishIfReady()

        assertEquals(0, finallyCount)
        assertDestinationInUse(registry, destination)
        val secondPermitAcquired = AtomicBoolean(false)
        val secondPermitFinished = CountDownLatch(1)
        val permitWorker = Thread {
            limiter.acquire()
            secondPermitAcquired.set(true)
            limiter.release()
            secondPermitFinished.countDown()
        }.apply { isDaemon = true }
        permitWorker.start()
        assertFalse(secondPermitFinished.await(100L, TimeUnit.MILLISECONDS))

        resources.finishOperation()

        assertTrue(secondPermitFinished.await(2L, TimeUnit.SECONDS))
        assertTrue(secondPermitAcquired.get())
        assertEquals(1, finallyCount)
        registry.acquire(destination).release()
    }

    private fun awaitThreadWaiting(worker: Thread) {
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2L)
        while (System.nanoTime() < deadline) {
            if (worker.state == Thread.State.WAITING) return
            if (!worker.isAlive) break
            Thread.yield()
        }
        throw AssertionError("任务未在限定时间内进入并发许可等待状态")
    }

    private fun assertDestinationInUse(
        registry: DownloadDestinationRegistry,
        destination: java.io.File,
    ) {
        assertTrue(runCatching { registry.acquire(destination) }.isFailure)
    }
}
