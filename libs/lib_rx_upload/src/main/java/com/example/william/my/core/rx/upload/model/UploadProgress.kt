package com.example.william.my.core.rx.upload.model

/** 单次上传的字节进度。总大小未知时 [totalBytes] 为 `-1`。 */
data class UploadProgress(
    val currentBytes: Long,
    val totalBytes: Long,
) {
    val percent: Int?
        get() = if (totalBytes > 0L) {
            ((currentBytes * 100L) / totalBytes).coerceIn(0L, 100L).toInt()
        } else {
            null
        }
}
