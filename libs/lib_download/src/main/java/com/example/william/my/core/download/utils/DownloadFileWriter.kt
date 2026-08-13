package com.example.william.my.core.download.utils

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * 下载文件写入工具
 *
 * 将响应输入流写入指定文件，并在已知内容长度时提供写入进度。
 */
object DownloadFileWriter {

    private const val BUFFER_SIZE = 1024 * 512

    /**
     * 将输入流写入文件。
     *
     * @param contentLength 文件总大小（字节）。传 `-1` 表示大小未知，此时不回调进度。
     */
    fun writeFileFromIS(
        file: File,
        inputStream: InputStream?,
        append: Boolean,
        contentLength: Long,
        listener: OnProgressUpdateListener?
    ): Boolean {
        if (inputStream == null || !createOrExistsFile(file)) {
            return false
        }
        var outputStream: OutputStream? = null
        return try {
            outputStream = BufferedOutputStream(FileOutputStream(file, append), BUFFER_SIZE)
            if (listener == null || contentLength <= 0) {
                val data = ByteArray(BUFFER_SIZE)
                var length: Int
                while (inputStream.read(data).also { length = it } != -1) {
                    outputStream.write(data, 0, length)
                }
            } else {
                val totalSize = contentLength.toDouble()
                var currentSize = 0L
                listener.onProgressUpdate(0.0)
                val data = ByteArray(BUFFER_SIZE)
                var length: Int
                while (inputStream.read(data).also { length = it } != -1) {
                    outputStream.write(data, 0, length)
                    currentSize += length
                    listener.onProgressUpdate(currentSize / totalSize)
                }
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        } finally {
            try {
                inputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            try {
                outputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    @Deprecated(
        message = "使用 contentLength 参数的重载方法，available() 不返回总大小",
        replaceWith = ReplaceWith("writeFileFromIS(file, inputStream, append, contentLength, listener)")
    )
    fun writeFileFromIS(
        file: File,
        inputStream: InputStream?,
        append: Boolean,
        listener: OnProgressUpdateListener?
    ): Boolean {
        return writeFileFromIS(file, inputStream, append, -1L, listener)
    }

    /** 判断文件是否存在，不存在时尝试创建。 */
    private fun createOrExistsFile(file: File?): Boolean {
        if (file == null) return false
        if (file.exists()) return file.isFile
        return if (!createOrExistsDir(file.parentFile)) false else try {
            file.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    private fun createOrExistsDir(file: File?): Boolean {
        return file != null && if (file.exists()) file.isDirectory else file.mkdirs()
    }

    interface OnProgressUpdateListener {
        fun onProgressUpdate(progress: Double)
    }
}
