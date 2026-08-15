package com.example.william.my.core.rx.download.queue.model

import java.io.File

/** 批量下载中的单个不可变任务。 */
data class DownloadQueueTask(
    val url: String,
    val destination: File,
    val id: String = destination.absolutePath,
    val headers: Map<String, String> = emptyMap(),
    val resume: Boolean = true,
)
