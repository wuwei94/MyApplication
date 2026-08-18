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
    fun injectedRetrofitWithCustomCallFactoryPreservesFactory() {
        val customFactory = okhttp3.Call.Factory { request ->
            OkHttpClient().newCall(request)
        }
        val customRetrofit = retrofit2.Retrofit.Builder()
            .baseUrl("https://example.com/")
            .callFactory(customFactory)
            .build()

        val config = RxUpload.builder()
            .api("https://example.com/upload")
            .addFile("file", temporaryFolder.newFile())
            .retrofit(customRetrofit)
            .buildConfig()

        assertEquals(customFactory, config.retrofit.callFactory())
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

    @Test
    fun addHeadersAndFieldsMergeIncrementallyAndSetReplaces() {
        val config = RxUpload.builder()
            .api("https://example.com/upload")
            .addFile("file", temporaryFolder.newFile())
            .addHeader("A", "1")
            .addHeaders(mapOf("B" to "2"))
            .addFormField("f1", "v1")
            .addFormFields(mapOf("f2" to "v2"))
            .buildConfig()

        assertEquals(mapOf("A" to "1", "B" to "2"), config.headers)
        assertEquals(listOf("f1" to "v1", "f2" to "v2"), config.formFields)

        val resetConfig = RxUpload.builder()
            .api("https://example.com/upload")
            .addFile("file", temporaryFolder.newFile())
            .addHeader("A", "1")
            .setHeaders(mapOf("C" to "3"))
            .addFormField("f1", "v1")
            .setFormFields(mapOf("f3" to "v3"))
            .buildConfig()

        assertEquals(mapOf("C" to "3"), resetConfig.headers)
        assertEquals(listOf("f3" to "v3"), resetConfig.formFields)
    }
}
