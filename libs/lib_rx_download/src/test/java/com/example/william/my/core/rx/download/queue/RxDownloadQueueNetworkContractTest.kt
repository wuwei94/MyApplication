package com.example.william.my.core.rx.download.queue

import com.example.william.my.core.retrofit.exception.ApiException
import com.example.william.my.core.retrofit.rx.api.rxRetrofit
import com.example.william.my.core.rx.download.RxDownloadManager
import com.example.william.my.core.rx.download.callback.RxDownloadCallback
import com.example.william.my.core.rx.download.queue.model.DownloadQueueEvent
import com.example.william.my.core.rx.download.queue.model.DownloadQueueProgress
import com.example.william.my.core.rx.download.queue.model.DownloadQueueResult
import com.example.william.my.core.rx.download.queue.model.DownloadQueueTask
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class RxDownloadQueueNetworkContractTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun queueHonorsConfiguredConcurrencyAndAggregatesBytes() {
        val active = AtomicInteger()
        val maximum = AtomicInteger()
        val server = MockWebServer().apply {
            dispatcher = object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse {
                    val current = active.incrementAndGet()
                    maximum.updateAndGet { previous -> maxOf(previous, current) }
                    Thread.sleep(RESPONSE_DELAY_MILLIS)
                    active.decrementAndGet()
                    return MockResponse().setBody(RESPONSE_BODY)
                }
            }
            start()
        }
        try {
            val tasks = (1..5).map { index ->
                DownloadQueueTask(
                    url = server.url("/file-$index").toString(),
                    destination = temporaryFolder.newFile("file-$index.bin"),
                    id = "file-$index",
                )
            }

            val observer = RxDownloadManager.builder()
                .retrofit(rxRetrofit { client(OkHttpClient()) })
                .maxConcurrency(2)
                .build()
                .download()
                .addTasks(tasks)
                .observeOn(Schedulers.trampoline())
                .progressIntervalMillis(50L)
                .buildFlowable()
                .test()

            observer.awaitDone(10L, TimeUnit.SECONDS).assertComplete().assertNoErrors()

            val events = observer.values()
            val result = events.filterIsInstance<DownloadQueueEvent.Completed>().single().result
            val finalProgress = events.filterIsInstance<DownloadQueueEvent.OverallProgress>()
                .last()
                .progress
            assertEquals(2, maximum.get())
            assertEquals(5, result.successes.size)
            assertTrue(result.failures.isEmpty())
            assertEquals(100, finalProgress.percent)
            assertEquals(RESPONSE_BODY.length.toLong() * 5L, finalProgress.currentBytes)
            assertEquals(finalProgress.totalBytes, finalProgress.currentBytes)
            val overallBytes = events.filterIsInstance<DownloadQueueEvent.OverallProgress>()
                .map { event -> event.progress.currentBytes }
            assertTrue(overallBytes.zipWithNext().all { (previous, next) -> previous <= next })
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun unifiedCallbackReceivesQueueProgressAndResult() {
        val server = MockWebServer().apply {
            enqueue(MockResponse().setBody(RESPONSE_BODY))
            start()
        }
        try {
            var recordedProgress: DownloadQueueProgress? = null
            var result: DownloadQueueResult? = null
            var failure: ApiException? = null
            val completed = CountDownLatch(1)

            RxDownloadManager.builder()
                .retrofit(rxRetrofit { client(OkHttpClient()) })
                .build()
                .download()
                .addTask(
                    api = server.url("/callback").toString(),
                    destination = temporaryFolder.newFile("callback.bin"),
                )
                .observeOn(Schedulers.trampoline())
                .build()
                .subscribeWith(
                    object : RxDownloadCallback<DownloadQueueProgress, DownloadQueueResult>() {
                        override fun onProgress(progress: DownloadQueueProgress) {
                            recordedProgress = progress
                        }

                        override fun onResponse(response: DownloadQueueResult) {
                            result = response
                            completed.countDown()
                        }

                        override fun onFailure(error: ApiException) {
                            failure = error
                            completed.countDown()
                        }
                    }
                )

            assertTrue(completed.await(10L, TimeUnit.SECONDS))
            assertNull(failure)
            assertEquals(1, requireNotNull(result).successes.size)
            assertEquals(1, requireNotNull(recordedProgress).completedCount)
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun queuesFromSameManagerShareConcurrencyLimit() {
        val active = AtomicInteger()
        val maximum = AtomicInteger()
        val server = MockWebServer().apply {
            dispatcher = object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse {
                    val current = active.incrementAndGet()
                    maximum.updateAndGet { previous -> maxOf(previous, current) }
                    Thread.sleep(RESPONSE_DELAY_MILLIS)
                    active.decrementAndGet()
                    return MockResponse().setBody(RESPONSE_BODY)
                }
            }
            start()
        }
        try {
            val manager = RxDownloadManager.builder()
                .retrofit(rxRetrofit { client(OkHttpClient()) })
                .maxConcurrency(2)
                .build()
            val queues = (1..2).map { queueIndex ->
                val tasks = (1..3).map { taskIndex ->
                    DownloadQueueTask(
                        url = server.url("/queue-$queueIndex-file-$taskIndex").toString(),
                        destination = temporaryFolder.newFile(
                            "queue-$queueIndex-file-$taskIndex.bin"
                        ),
                        id = "queue-$queueIndex-file-$taskIndex",
                    )
                }
                manager.download()
                    .addTasks(tasks)
                    .observeOn(Schedulers.trampoline())
                    .buildFlowable()
            }

            val observer = Flowable.merge(queues).test()

            observer.awaitDone(10L, TimeUnit.SECONDS).assertComplete().assertNoErrors()
            assertEquals(2, maximum.get())
            assertEquals(
                2,
                observer.values().filterIsInstance<DownloadQueueEvent.Completed>().size,
            )
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun taskFailureDoesNotCancelRemainingDownloads() {
        val server = MockWebServer().apply {
            dispatcher = object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse {
                    return if (request.path == "/failed") {
                        MockResponse().setResponseCode(500)
                    } else {
                        MockResponse().setBody(RESPONSE_BODY)
                    }
                }
            }
            start()
        }
        try {
            val paths = listOf("/first", "/failed", "/last")
            val tasks = paths.mapIndexed { index, path ->
                DownloadQueueTask(
                    url = server.url(path).toString(),
                    destination = temporaryFolder.newFile("result-$index.bin"),
                    id = "task-$index",
                )
            }

            val observer = RxDownloadManager.builder()
                .retrofit(rxRetrofit { client(OkHttpClient()) })
                .build()
                .download()
                .addTasks(tasks)
                .observeOn(Schedulers.trampoline())
                .buildFlowable()
                .test()

            observer.awaitDone(10L, TimeUnit.SECONDS).assertComplete().assertNoErrors()

            val result = observer.values()
                .filterIsInstance<DownloadQueueEvent.Completed>()
                .single()
                .result
            assertEquals(2, result.successes.size)
            assertEquals(1, result.failures.size)
            assertEquals("task-1", result.failures.single().task.id)
        } finally {
            server.shutdown()
        }
    }

    private companion object {
        const val RESPONSE_BODY = "payload"
        const val RESPONSE_DELAY_MILLIS = 150L
    }
}
