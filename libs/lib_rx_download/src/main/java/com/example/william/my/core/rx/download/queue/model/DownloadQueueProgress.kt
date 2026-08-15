package com.example.william.my.core.rx.download.queue.model

/** 批量下载的字节与任务数进度。存在未知文件大小时 [totalBytes] 为 `-1`。 */
data class DownloadQueueProgress(
    val currentBytes: Long,
    val totalBytes: Long,
    val completedCount: Int,
    val successCount: Int,
    val failedCount: Int,
    val totalCount: Int,
    val activeTaskIds: List<String>,
) {
    val percent: Int?
        get() = when {
            totalBytes > 0L -> {
                ((currentBytes * 100L) / totalBytes).coerceIn(0L, 100L).toInt()
            }

            totalCount > 0 && completedCount == totalCount && failedCount == 0 -> 100
            else -> null
        }
}
