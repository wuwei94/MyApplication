package com.example.william.my.core.base.utils

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

object DownloadUtils {
    fun saveFileToDownloads(context: Context, filePath: String?, fileName: String?): Boolean {
        if (filePath.isNullOrBlank() || fileName.isNullOrBlank()) return false
        val sourceFile = File(filePath)
        if (!sourceFile.exists() || !sourceFile.isFile) return false

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                try {
                    val inputStream: InputStream = FileInputStream(sourceFile)
                    FileIOUtils.writeFileFromIS(context, uri, inputStream)
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
            val targetFile = File(targetDir, fileName)
            try {
                val inputStream: InputStream = FileInputStream(sourceFile)
                FileIOUtils.writeFileFromIS(targetFile, inputStream, false)
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}
