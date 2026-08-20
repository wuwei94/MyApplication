package com.example.william.my.core.rx.download.queue.model

import com.example.william.my.core.retrofit.exception.ApiException
import com.example.william.my.core.rx.download.model.DownloadResult

/** 单个批量任务的成功结果。 */
data class DownloadTaskResult(
    val task: DownloadQueueTask,
    val result: DownloadResult,
)

/** 单个批量任务的失败结果。 */
data class DownloadTaskFailure(
    val task: DownloadQueueTask,
    val error: ApiException,
)

/** 批量下载全部进入终态后的结果。 */
data class DownloadQueueResult(
    val successes: List<DownloadTaskResult>,
    val failures: List<DownloadTaskFailure>,
) {
    val totalCount: Int
        get() = successes.size + failures.size
}
