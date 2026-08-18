package com.example.william.my.core.rx.download.request

import com.example.william.my.core.rx.download.RxDownload
import com.example.william.my.core.rx.download.builder.RxDownloadBuilder
import com.example.william.my.core.rx.download.callback.RxDownloadCallback
import com.example.william.my.core.rx.download.exception.DownloadHttpException
import com.example.william.my.core.rx.download.model.DownloadProgress
import com.example.william.my.core.rx.download.model.DownloadResult
import com.example.william.my.core.rx.download.resume.DownloadResumeMetadata
import com.example.william.my.core.retrofit.rx.api.rxRetrofit
import com.example.william.my.core.retrofit.exception.ApiException
import io.reactivex.rxjava3.schedulers.TestScheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class RxDownloadNetworkContractTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun resumeUsesValidRangeAndAppendsPartialResponse() {
        val server = MockWebServer().apply {
            enqueue(
                MockResponse()
                    .setResponseCode(206)
                    .addHeader("Content-Range", "bytes 6-10/11")
                    .setBody("world")
            )
            start()
        }
        try {
            val target = File(temporaryFolder.root, "result.txt")
            seedPartial(server, target, "hello ")
            val progressUpdates = mutableListOf<Pair<Long, Long>>()
            var result: DownloadResult? = null
            var failure: ApiException? = null

            request(server, target)
                .build()
                .subscribeWith(object : RxDownloadCallback<DownloadProgress, DownloadResult>() {
                    override fun onProgress(progress: DownloadProgress) {
                        progressUpdates += progress.currentBytes to progress.totalBytes
                    }

                    override fun onResponse(response: DownloadResult) {
                        result = response
                    }

                    override fun onFailure(error: ApiException) {
                        failure = error
                    }
                })

            val recorded = server.takeRequest()
            assertEquals("bytes=6-", recorded.getHeader("Range"))
            assertEquals(ETAG, recorded.getHeader("If-Range"))
            assertEquals("hello world", target.readText())
            assertFalse(partialFile(target).exists())
            assertNull(failure)
            assertTrue(requireNotNull(result).resumed)
            assertEquals(11L to 11L, progressUpdates.last())
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun fullResponseOverwritesPartialFileWhenServerIgnoresRange() {
        val server = MockWebServer().apply {
            enqueue(MockResponse().setBody("fresh"))
            start()
        }
        try {
            val target = temporaryFolder.newFile().apply { writeText("previous") }
            seedPartial(server, target, "partial")

            val result = request(server, target).buildSingle().blockingGet()

            val recorded = server.takeRequest()
            assertEquals("bytes=7-", recorded.getHeader("Range"))
            assertEquals(ETAG, recorded.getHeader("If-Range"))
            assertEquals("fresh", target.readText())
            assertFalse(result.resumed)
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun rangeNotSatisfiableIsSuccessWhenLocalFileIsComplete() {
        val server = MockWebServer().apply {
            enqueue(
                MockResponse()
                    .setResponseCode(416)
                    .addHeader("Content-Range", "bytes */5")
            )
            start()
        }
        try {
            val target = File(temporaryFolder.root, "ready.txt")
            seedPartial(server, target, "ready")

            val result = request(server, target).buildSingle().blockingGet()

            assertEquals(5L, result.totalBytes)
            assertEquals("ready", target.readText())
            assertTrue(result.resumed)
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun httpErrorDoesNotModifyExistingPartialFile() {
        val server = MockWebServer().apply {
            enqueue(MockResponse().setResponseCode(500).setBody("error"))
            start()
        }
        try {
            val target = temporaryFolder.newFile().apply { writeText("complete") }
            seedPartial(server, target, "partial")

            request(server, target)
                .buildSingle()
                .test()
                .assertError { error ->
                    error is DownloadHttpException &&
                        error.statusCode == 500 &&
                        error.responseBody == "error"
                }

            assertEquals("complete", target.readText())
            assertEquals("partial", partialFile(target).readText())
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun callbackUsesHttpErrorResponseBodyAsMessage() {
        val server = MockWebServer().apply {
            enqueue(MockResponse().setResponseCode(404).setBody("file not found"))
            start()
        }
        try {
            val target = File(temporaryFolder.root, "missing.txt")
            var failure: ApiException? = null

            request(server, target)
                .build()
                .subscribeWith(object : RxDownloadCallback<DownloadProgress, DownloadResult>() {
                    override fun onFailure(error: ApiException) {
                        failure = error
                    }
                })

            assertEquals(404, failure?.code)
            assertEquals("file not found", failure?.message)
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun callbackExtractsJsonErrorMessageFromHttpError() {
        val server = MockWebServer().apply {
            enqueue(MockResponse().setResponseCode(403).setBody("""{"message":"文件已过期"}"""))
            start()
        }
        try {
            val target = File(temporaryFolder.root, "expired.txt")
            var failure: ApiException? = null

            request(server, target)
                .build()
                .subscribeWith(object : RxDownloadCallback<DownloadProgress, DownloadResult>() {
                    override fun onFailure(error: ApiException) {
                        failure = error
                    }
                })

            assertEquals(403, failure?.code)
            assertEquals("文件已过期", failure?.message)
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun mismatchedContentRangeDoesNotAppendResponse() {
        val server = MockWebServer().apply {
            enqueue(
                MockResponse()
                    .setResponseCode(206)
                    .addHeader("Content-Range", "bytes 2-4/5")
                    .setBody("bad")
            )
            start()
        }
        try {
            val target = temporaryFolder.newFile().apply { writeText("complete") }
            seedPartial(server, target, "partial")

            request(server, target)
                .buildSingle()
                .test()
                .assertError { error ->
                    error is java.io.IOException &&
                        error.message.orEmpty().contains("起点不匹配")
                }

            assertEquals("complete", target.readText())
            assertEquals("partial", partialFile(target).readText())
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun partialFileFromDifferentUrlIsNotResumed() {
        val server = MockWebServer().apply {
            enqueue(MockResponse().setBody("fresh"))
            start()
        }
        try {
            val target = File(temporaryFolder.root, "identity.txt")
            partialFile(target).writeText("stale")
            DownloadResumeMetadata(
                url = server.url("/old-file").toString(),
                etag = ETAG,
                lastModified = null,
            ).save(metadataFile(target))

            request(server, target).buildSingle().blockingGet()

            assertNull(server.takeRequest().getHeader("Range"))
            assertEquals("fresh", target.readText())
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun asynchronousFinalProgressArrivesBeforeSuccess() {
        val server = MockWebServer().apply {
            enqueue(MockResponse().setBody("payload"))
            start()
        }
        try {
            val scheduler = TestScheduler()
            val events = mutableListOf<String>()
            val target = File(temporaryFolder.root, "async.txt")
            request(server, target)
                .progressOn(scheduler)
                .onProgress { progress -> events += "progress:${progress.percent}" }
                .buildSingle()
                .subscribe(
                    { events += "success" },
                    { error -> events += "failure:${error.message}" },
                )

            assertTrue(events.isEmpty())
            scheduler.triggerActions()

            assertEquals("success", events.last())
            assertTrue(events.dropLast(1).contains("progress:100"))
        } finally {
            server.shutdown()
        }
    }

    private fun request(server: MockWebServer, target: java.io.File): RxDownloadBuilder {
        return RxDownload.builder()
            .url(server.url("/file").toString())
            .destination(target)
            .retrofit(rxRetrofit { client(OkHttpClient()) })
            .subscribeOn(Schedulers.trampoline())
            .observeOn(Schedulers.trampoline())
            .progressOn(Schedulers.trampoline())
            .progressIntervalMillis(50L)
    }

    private fun seedPartial(server: MockWebServer, target: File, content: String) {
        partialFile(target).writeText(content)
        DownloadResumeMetadata(
            url = server.url("/file").toString(),
            etag = ETAG,
            lastModified = null,
        ).save(metadataFile(target))
    }

    private fun partialFile(target: File): File {
        return File(target.path + RxDownloadRequest.PARTIAL_SUFFIX)
    }

    private fun metadataFile(target: File): File {
        return File(target.path + RxDownloadRequest.METADATA_SUFFIX)
    }

    private companion object {
        const val ETAG = "\"file-v1\""
    }
}
