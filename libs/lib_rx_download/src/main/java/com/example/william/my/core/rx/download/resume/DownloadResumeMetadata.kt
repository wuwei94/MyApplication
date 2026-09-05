package com.example.william.my.core.rx.download.resume

import okhttp3.Headers
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.Properties

/** 断点续传所需的资源身份信息。 */
internal data class DownloadResumeMetadata(
    val url: String,
    val etag: String?,
    val lastModified: String?,
) {

    val ifRange: String?
        get() = etag
            ?.takeUnless { value -> value.startsWith(WEAK_ETAG_PREFIX, ignoreCase = true) }
            ?: lastModified

    fun save(file: File) {
        val properties = Properties().apply {
            setProperty(KEY_URL, url)
            etag?.let { setProperty(KEY_ETAG, it) }
            lastModified?.let { setProperty(KEY_LAST_MODIFIED, it) }
        }
        val temporary = File(file.path + TEMPORARY_SUFFIX)
        FileOutputStream(temporary).use { output -> properties.store(output, null) }
        if (temporary.renameTo(file)) return
        if (file.exists() && !file.delete()) {
            temporary.delete()
            throw IOException("无法替换下载续传元数据：${file.path}")
        }
        if (!temporary.renameTo(file)) {
            temporary.delete()
            throw IOException("无法保存下载续传元数据：${file.path}")
        }
    }

    companion object {
        private const val KEY_URL = "url"
        private const val KEY_ETAG = "etag"
        private const val KEY_LAST_MODIFIED = "lastModified"
        private const val WEAK_ETAG_PREFIX = "W/"
        private const val TEMPORARY_SUFFIX = ".tmp"

        fun from(headers: Headers, url: String): DownloadResumeMetadata = DownloadResumeMetadata(
            url = url,
            etag = headers["ETag"],
            lastModified = headers["Last-Modified"],
        )

        fun load(file: File): DownloadResumeMetadata? {
            if (!file.isFile) return null
            return runCatching {
                val properties = Properties().apply {
                    FileInputStream(file).use { input -> load(input) }
                }
                val url = properties.getProperty(KEY_URL)?.takeIf(String::isNotBlank)
                    ?: return null
                DownloadResumeMetadata(
                    url = url,
                    etag = properties.getProperty(KEY_ETAG)?.takeIf(String::isNotBlank),
                    lastModified = properties.getProperty(KEY_LAST_MODIFIED)
                        ?.takeIf(String::isNotBlank),
                )
            }.getOrNull()
        }
    }
}
