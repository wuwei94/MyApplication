package com.example.william.my.core.rx.download.config

import androidx.lifecycle.Lifecycle
import com.example.william.my.core.rx.download.model.DownloadProgress
import com.trello.rxlifecycle4.LifecycleProvider
import io.reactivex.rxjava3.core.Scheduler
import retrofit2.Retrofit
import java.io.File

/** 单次下载的只读配置快照。 */
internal data class DownloadConfig(
    val url: String,
    val destination: File,
    val headers: Map<String, String>,
    val resume: Boolean,
    val retrofit: Retrofit,
    val lifecycle: LifecycleProvider<Lifecycle.Event>?,
    val subscribeScheduler: Scheduler,
    val observeScheduler: Scheduler?,
    val progressScheduler: Scheduler?,
    val progressIntervalMillis: Long,
    val onProgress: ((DownloadProgress) -> Unit)?,
    val onOperationStart: (() -> Boolean)?,
    val onFinally: (() -> Unit)?,
)
