package com.example.william.my.core.rx.download.queue

import com.example.william.my.core.rx.download.RxDownloadManager
import com.example.william.my.core.rx.download.queue.model.DownloadQueueTask
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DownloadDestinationRegistryTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun activeDestinationCannotBeAcquiredTwice() {
        val registry = DownloadDestinationRegistry()
        val destination = temporaryFolder.newFile("shared.bin")
        val lease = registry.acquire(destination)

        assertThrows(IllegalStateException::class.java) {
            registry.acquire(destination)
        }

        lease.release()
        val nextLease = registry.acquire(destination)
        assertNotNull(nextLease)
        nextLease.release()
    }

    @Test
    fun queuesFromSameManagerShareDestinationRegistry() {
        val manager = RxDownloadManager.builder().build()
        val first = manager.download()
            .addTask(task("first.bin"))
            .buildConfig()
        val second = manager.download()
            .addTask(task("second.bin"))
            .buildConfig()

        assertSame(first.destinationRegistry, second.destinationRegistry)
    }

    private fun task(fileName: String): DownloadQueueTask {
        return DownloadQueueTask(
            url = "https://example.com/$fileName",
            destination = temporaryFolder.newFile(fileName),
        )
    }
}
