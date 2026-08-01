package com.example.william.my.core.retrofit.utils

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

object FileIOUtils {

    private const val sBufferSize = 1024 * 512

    /**
     * 将输入流写入文件（带进度回调，需要已知总大小）。
     *
     * @param contentLength 文件总大小（字节），可从 ResponseBody.contentLength() 获取。
     *        传 -1 表示未知大小，进度回调将不会被调用。
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
        var os: OutputStream? = null
        return try {
            os = BufferedOutputStream(FileOutputStream(file, append), sBufferSize)
            if (listener == null || contentLength <= 0) {
                val data = ByteArray(sBufferSize)
                var len: Int
                while (inputStream.read(data).also { len = it } != -1) {
                    os.write(data, 0, len)
                }
            } else {
                val totalSize = contentLength.toDouble()
                var curSize = 0L
                listener.onProgressUpdate(0.0)
                val data = ByteArray(sBufferSize)
                var len: Int
                while (inputStream.read(data).also { len = it } != -1) {
                    os.write(data, 0, len)
                    curSize += len
                    listener.onProgressUpdate(curSize / totalSize)
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
                os?.close()
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

    /**
     * 判断文件是否存在，不存在则判断是否创建成功
     */
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