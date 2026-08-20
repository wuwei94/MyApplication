package com.example.william.my.core.rx.download.queue.model

import com.example.william.my.core.retrofit.exception.ApiException
import com.example.william.my.core.rx.download.model.DownloadProgress
import com.example.william.my.core.rx.download.model.DownloadResult

/** 批量下载对外发送的任务和整体事件。 */
sealed interface DownloadQueueEvent {

    data class TaskStarted(
        val task: DownloadQueueTask,
    ) : DownloadQueueEvent

    data class TaskProgress(
        val task: DownloadQueueTask,
        val progress: DownloadProgress,
    ) : DownloadQueueEvent

    data class OverallProgress(
        val progress: DownloadQueueProgress,
    ) : DownloadQueueEvent

    data class TaskSucceeded(
        val task: DownloadQueueTask,
        val result: DownloadResult,
    ) : DownloadQueueEvent

    data class TaskFailed(
        val task: DownloadQueueTask,
        val error: ApiException,
    ) : DownloadQueueEvent

    data class Completed(
        val result: DownloadQueueResult,
    ) : DownloadQueueEvent
}
