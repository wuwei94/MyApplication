package com.example.william.my.core.rx.download.model

import java.io.File

/** 下载成功结果。 */
data class DownloadResult(
    val file: File,
    val totalBytes: Long,
    val resumed: Boolean,
)
