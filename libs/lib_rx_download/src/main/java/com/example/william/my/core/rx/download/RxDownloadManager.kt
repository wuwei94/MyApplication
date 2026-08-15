package com.example.william.my.core.rx.download

import com.example.william.my.core.rx.download.queue.DownloadConcurrencyLimiter
import com.example.william.my.core.rx.download.queue.DownloadDestinationRegistry
import com.example.william.my.core.rx.download.queue.RxDownloadQueueBuilder
import com.example.william.my.core.rx.download.request.RxDownloadRequest
import retrofit2.Retrofit

/** 按业务隔离共享 Retrofit 与下载并发队列。 */
class RxDownloadManager private constructor(
    internal val retrofit: Retrofit,
    internal val maxConcurrency: Int,
    private val concurrencyLimiter: DownloadConcurrencyLimiter,
    private val destinationRegistry: DownloadDestinationRegistry,
) {

    fun download(): RxDownloadQueueBuilder {
        return RxDownloadQueueBuilder(
            retrofit,
            maxConcurrency,
            concurrencyLimiter,
            destinationRegistry,
        )
    }

    fun queue(): RxDownloadQueueBuilder = download()

    class Builder internal constructor() {
        private var retrofit: Retrofit = RxDownloadRequest.defaultRetrofit
        private var maxConcurrency: Int = DEFAULT_MAX_CONCURRENCY

        fun retrofit(retrofit: Retrofit) = apply { this.retrofit = retrofit }

        fun maxConcurrency(maxConcurrency: Int) = apply {
            require(maxConcurrency in 1..MAX_CONCURRENCY) {
                "下载并发数必须在 1 到 $MAX_CONCURRENCY 之间"
            }
            this.maxConcurrency = maxConcurrency
        }

        fun build(): RxDownloadManager {
            return RxDownloadManager(
                retrofit = retrofit,
                maxConcurrency = maxConcurrency,
                concurrencyLimiter = DownloadConcurrencyLimiter(maxConcurrency),
                destinationRegistry = DownloadDestinationRegistry(),
            )
        }
    }

    companion object {
        const val DEFAULT_MAX_CONCURRENCY = 3
        const val MAX_CONCURRENCY = 32

        @JvmStatic
        fun builder(): Builder = Builder()
    }
}
