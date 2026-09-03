@file:JvmName("MediaStoreUtils")

package com.example.william.my.core.base.ext

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileInputStream

/**
 * 将本地已有文件安全保存/导出到系统的公共 Downloads 目录
 *
 * 适配 Android 10+ (API 29) 分区存储（Scoped Storage）规范：
 * - Android 10 及以上：通过 [MediaStore.Downloads] 内容提供者以流形式写入公共存储区；
 * - Android 9 及以下：直接复制到系统的 [Environment.DIRECTORY_DOWNLOADS] 目录。
 *
 * @param context Android 上下文
 * @param targetFileName 目标文件名（如 "export_data.xlsx"、"report.pdf"）
 * @return 导出是否成功
 */
fun File.saveToPublicDownloads(context: Context, targetFileName: String): Boolean {
    if (!this.exists() || !this.isFile || targetFileName.isBlank()) return false

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, targetFileName)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    FileInputStream(this).use { `is` ->
                        `is`.copyTo(os)
                    }
                } != null
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        } else {
            false
        }
    } else {
        @Suppress("DEPRECATION")
        val targetDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val targetFile = File(targetDir, targetFileName)
        try {
            this.copyTo(targetFile, overwrite = true)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
