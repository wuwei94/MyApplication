package com.example.william.my.core.rx.upload.request

import com.example.william.my.core.retrofit.exception.ApiException
import com.example.william.my.core.retrofit.rx.api.rxRetrofit
import com.example.william.my.core.rx.upload.RxUpload
import com.example.william.my.core.rx.upload.builder.RxUploadBuilder
import com.example.william.my.core.rx.upload.callback.RxUploadCallback
import com.example.william.my.core.rx.upload.exception.UploadHttpException
import com.example.william.my.core.rx.upload.model.UploadProgress
import com.example.william.my.core.rx.upload.model.UploadResult
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.schedulers.TestScheduler
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class RxUploadNetworkContractTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun multipartUploadContainsFieldsFileAndFinalProgress() {
        val server = MockWebServer().apply {
            enqueue(MockResponse().setBody("{\"ok\":true}"))
            start()
        }
        try {
            val file = temporaryFolder.newFile("avatar.txt").apply { writeText("payload") }
            val progressUpdates = mutableListOf<Pair<Long, Long>>()
            var uploadResult: UploadResult? = null
            var failure: ApiException? = null

            request(server)
                .addHeader("X-Request-Id", "request-1")
                .addFormField("type", "avatar")
                .addFile("file", file)
                .build()
                .subscribeWith(object : RxUploadCallback() {
                    override fun onProgress(progress: UploadProgress) {
                        progressUpdates += progress.currentBytes to progress.totalBytes
                    }

                    override fun onResponse(response: UploadResult) {
                        uploadResult = response
                    }

                    override fun onFailure(error: ApiException) {
                        failure = error
                    }
                })

            val recorded = server.takeRequest()
            val body = recorded.body.readUtf8()
            assertEquals("POST", recorded.method)
            assertEquals("request-1", recorded.getHeader("X-Request-Id"))
            assertTrue(recorded.getHeader("Content-Type").orEmpty().startsWith("multipart/form-data"))
            assertTrue(body.contains("name=\"type\""))
            assertTrue(body.contains("avatar"))
            assertTrue(body.contains("filename=\"avatar.txt\""))
            assertTrue(body.contains("payload"))
            assertNull(failure)
            assertEquals("{\"ok\":true}", uploadResult?.body)
            assertEquals(progressUpdates.last().second, progressUpdates.last().first)
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun nonSuccessfulResponseReturnsTypedErrorWithBody() {
        val server = MockWebServer().apply {
            enqueue(MockResponse().setResponseCode(413).setBody("too large"))
            start()
        }
        try {
            val file = temporaryFolder.newFile().apply { writeText("payload") }

            request(server)
                .addFile("file", file)
                .buildSingle()
                .test()
                .assertError { error ->
                    error is UploadHttpException &&
                        error.statusCode == 413 &&
                        error.responseBody == "too large"
                }
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun callbackExtractsJsonErrorMessageFromHttpError() {
        val server = MockWebServer().apply {
            enqueue(MockResponse().setResponseCode(400).setBody("""{"message":"文件大小超限"}"""))
            start()
        }
        try {
            val file = temporaryFolder.newFile("large.txt").apply { writeText("payload") }
            var failure: ApiException? = null

            request(server)
                .addFile("file", file)
                .build()
                .subscribeWith(object : RxUploadCallback() {
                    override fun onFailure(error: ApiException) {
                        failure = error
                    }
                })

            assertEquals(400, failure?.code)
            assertEquals("文件大小超限", failure?.message)
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun multipartUploadSupportsMultipleFilesWithSharedField() {
        val server = MockWebServer().apply {
            enqueue(MockResponse().setBody("ok"))
            start()
        }
        try {
            val files = listOf(
                temporaryFolder.newFile("first.txt").apply { writeText("first") },
                temporaryFolder.newFile("second.txt").apply { writeText("second") },
            )

            request(server)
                .addFiles("files", files, "text/plain".toMediaType())
                .buildSingle()
                .blockingGet()

            val body = server.takeRequest().body.readUtf8()
            assertEquals(2, body.split("filename=\"").size - 1)
            assertTrue(body.contains("filename=\"first.txt\""))
            assertTrue(body.contains("filename=\"second.txt\""))
            assertTrue(body.contains("Content-Type: text/plain"))
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun asynchronousFinalProgressArrivesBeforeSuccess() {
        val server = MockWebServer().apply {
            enqueue(MockResponse().setBody("ok"))
            start()
        }
        try {
            val scheduler = TestScheduler()
            val events = mutableListOf<String>()
            val file = temporaryFolder.newFile().apply { writeText("payload") }
            request(server)
                .addFile("file", file)
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

    private fun request(server: MockWebServer): RxUploadBuilder {
        return RxUpload.builder()
            .url(server.url("/upload").toString())
            .retrofit(rxRetrofit { client(OkHttpClient()) })
            .subscribeOn(Schedulers.trampoline())
            .observeOn(Schedulers.trampoline())
            .progressOn(Schedulers.trampoline())
            .progressIntervalMillis(50L)
    }
}
