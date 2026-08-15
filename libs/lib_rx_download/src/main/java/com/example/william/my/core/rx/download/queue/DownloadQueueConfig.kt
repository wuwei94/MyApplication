package com.example.william.my.core.rx.download.queue

import androidx.lifecycle.Lifecycle
import com.example.william.my.core.rx.download.queue.model.DownloadQueueTask
import com.trello.rxlifecycle4.LifecycleProvider
import io.reactivex.rxjava3.core.Scheduler
import retrofit2.Retrofit

/** 单次批量下载的只读配置快照。 */
internal data class DownloadQueueConfig(
    val tasks: List<DownloadQueueTask>,
    val retrofit: Retrofit,
    val concurrencyLimiter: DownloadConcurrencyLimiter,
    val destinationRegistry: DownloadDestinationRegistry,
    val maxConcurrency: Int,
    val lifecycle: LifecycleProvider<Lifecycle.Event>?,
    val subscribeScheduler: Scheduler,
    val observeScheduler: Scheduler?,
    val progressIntervalMillis: Long,
    val onFinally: (() -> Unit)?,
)
