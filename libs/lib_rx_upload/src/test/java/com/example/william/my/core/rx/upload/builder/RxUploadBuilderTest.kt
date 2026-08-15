package com.example.william.my.core.rx.upload.builder

import com.example.william.my.core.rx.upload.RxUpload
import com.example.william.my.core.retrofit.rx.api.rxRetrofit
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.schedulers.TestScheduler
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class RxUploadBuilderTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun buildConfigCopiesMutableInputs() {
        val headers = mutableMapOf("Authorization" to "token")
        val fields = mutableMapOf("type" to "avatar")
        val config = RxUpload.builder()
            .api("https://example.com/upload")
            .addHeader(headers)
            .addParams(fields)
            .addFile("file", temporaryFolder.newFile())
            .buildConfig()

        headers["Authorization"] = "changed"
        fields["type"] = "document"

        assertEquals("token", config.headers["Authorization"])
        assertEquals("avatar", config.formFields.single().second)
    }

    @Test
    fun atLeastOneFileIsRequired() {
        assertThrows(IllegalArgumentException::class.java) {
            RxUpload.builder()
                .api("https://example.com/upload")
                .buildConfig()
        }
    }

    @Test
    fun addFilesAppendsFilesWithSharedFieldAndMediaType() {
        val files = listOf(
            temporaryFolder.newFile("first.txt"),
            temporaryFolder.newFile("second.txt"),
        )
        val mediaType = "text/plain".toMediaType()

        val config = RxUpload.builder()
            .api("https://example.com/upload")
            .addFiles("files", files, mediaType)
            .buildConfig()

        assertEquals(files, config.files.map { part -> part.file })
        assertEquals(listOf("files", "files"), config.files.map { part -> part.name })
        assertEquals(
            listOf(mediaType, mediaType),
            config.files.map { part -> part.mediaType },
        )
    }

    @Test
    fun defaultRetrofitDoesNotRetryPostOnConnectionFailure() {
        val config = RxUpload.builder()
            .api("https://example.com/upload")
            .addFile("file", temporaryFolder.newFile())
            .buildConfig()

        val client = config.retrofit.callFactory() as OkHttpClient
        assertFalse(client.retryOnConnectionFailure)
    }

    @Test
    fun injectedRetrofitCannotEnableConnectionFailureRetry() {
        val config = RxUpload.builder()
            .api("https://example.com/upload")
            .addFile("file", temporaryFolder.newFile())
            .retrofit(rxRetrofit {
                client(OkHttpClient.Builder().retryOnConnectionFailure(true).build())
            })
            .buildConfig()

        val client = config.retrofit.callFactory() as OkHttpClient
        assertFalse(client.retryOnConnectionFailure)
    }

    @Test
    fun uploadRejectsUnthrottledProgress() {
        assertThrows(IllegalArgumentException::class.java) {
            RxUpload.builder().progressIntervalMillis(0L)
        }
    }

    @Test
    fun disposeBeforeIoStartInvokesFinallyOnce() {
        val scheduler = TestScheduler()
        var finallyCount = 0
        val observer = RxUpload.builder()
            .api("https://example.com/upload")
            .addFile("file", temporaryFolder.newFile())
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
