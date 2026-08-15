package com.example.william.my.core.rx.download.builder

import com.example.william.my.core.rx.download.RxDownload
import com.example.william.my.core.rx.download.RxDownloadManager
import com.example.william.my.core.retrofit.rx.api.rxRetrofit
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.schedulers.TestScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class RxDownloadBuilderTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun buildConfigCopiesHeaders() {
        val headers = mutableMapOf("Authorization" to "token")
        val config = RxDownload.builder()
            .api("https://example.com/file")
            .destination(temporaryFolder.newFile())
            .addHeader(headers)
            .subscribeOn(Schedulers.trampoline())
            .observeOn(Schedulers.trampoline())
            .progressOn(Schedulers.trampoline())
            .buildConfig()

        headers["Authorization"] = "changed"

        assertEquals("token", config.headers["Authorization"])
    }

    @Test
    fun destinationIsRequired() {
        assertThrows(IllegalArgumentException::class.java) {
            RxDownload.builder()
                .api("https://example.com/file")
                .buildConfig()
        }
    }

    @Test
    fun requestUsesInjectedRetrofit() {
        val retrofit = rxRetrofit()
        val config = RxDownload.builder()
            .api("https://example.com/file")
            .destination(temporaryFolder.newFile())
            .retrofit(retrofit)
            .buildConfig()

        assertSame(retrofit, config.retrofit)
    }

    @Test
    fun managerUsesThreeConcurrentDownloadsByDefault() {
        val manager = RxDownloadManager.builder().build()

        assertEquals(RxDownloadManager.DEFAULT_MAX_CONCURRENCY, manager.maxConcurrency)
        assertEquals(3, manager.maxConcurrency)
    }

    @Test
    fun managerUsesInjectedRetrofit() {
        val retrofit = rxRetrofit()
        val manager = RxDownloadManager.builder()
            .retrofit(retrofit)
            .build()

        assertSame(retrofit, manager.retrofit)
    }

    @Test
    fun managersKeepIndependentConcurrencyConfiguration() {
        val businessA = RxDownloadManager.builder().maxConcurrency(1).build()
        val businessB = RxDownloadManager.builder().maxConcurrency(5).build()

        assertEquals(1, businessA.maxConcurrency)
        assertEquals(5, businessB.maxConcurrency)
    }

    @Test
    fun managerRejectsUnboundedConcurrency() {
        assertThrows(IllegalArgumentException::class.java) {
            RxDownloadManager.builder()
                .maxConcurrency(RxDownloadManager.MAX_CONCURRENCY + 1)
        }
    }

    @Test
    fun queueRejectsUnthrottledProgress() {
        assertThrows(IllegalArgumentException::class.java) {
            RxDownloadManager.builder()
                .build()
                .download()
                .progressIntervalMillis(0L)
        }
    }

    @Test
    fun singleDownloadRejectsUnthrottledProgress() {
        assertThrows(IllegalArgumentException::class.java) {
            RxDownload.builder().progressIntervalMillis(0L)
        }
    }

    @Test
    fun queueCannotExceedManagerConcurrency() {
        assertThrows(IllegalArgumentException::class.java) {
            RxDownloadManager.builder()
                .maxConcurrency(2)
                .build()
                .download()
                .maxConcurrency(3)
        }
    }

    @Test
    fun disposeBeforeIoStartInvokesFinallyOnce() {
        val scheduler = TestScheduler()
        var finallyCount = 0
        val observer = RxDownload.builder()
            .api("https://example.com/file")
            .destination(temporaryFolder.newFile())
            .subscribeOn(scheduler)
            .observeOn(Schedulers.trampoline())
            .onFinally { finallyCount++ }
            .buildSingle()
            .test()

        observer.dispose()
        scheduler.triggerActions()

        assertEquals(1, finallyCount)
    }
}
