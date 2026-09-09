package com.example.william.my.module.gpuimage.helper

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * GPUImage 示例图片加载与结果保存工具
 *
 * - 图片源：内置 assets 示例图（[decodeAsset]）与系统相册图片（[decodeUri]）。
 *   解码时按 [MAX_EDGE] 上限等比降采样，避免超大图上传 OpenGL 纹理爆显存/超
 *   GL_MAX_TEXTURE_SIZE。
 * - 结果保存：[saveToGallery] 通过 MediaStore 写入系统相册（API 29+ 使用
 *   RELATIVE_PATH 到 Pictures/GPUImage，无需存储权限；旧版本写入外部共享相册）。
 */
object GpuImageHelper {

    /** 解码图片最长边上限（px） */
    private const val MAX_EDGE = 2048

    const val SAMPLE_DOG = "sample_dog.jpg"
    const val SAMPLE_CAR = "sample_car.jpg"

    /** 解码 assets 中的内置示例图 */
    fun decodeAsset(context: Context, fileName: String): Bitmap? = runCatching {
        context.assets.open(fileName).use { stream -> decodeSampled(stream.readBytes()) }
    }.getOrNull()

    /** 解码相册选图返回的 content Uri */
    fun decodeUri(context: Context, uri: Uri): Bitmap? = runCatching {
        context.contentResolver.openInputStream(uri)?.use { stream -> decodeSampled(stream.readBytes()) }
    }.getOrNull()

    /**
     * 两步解码：先读边界计算采样率，再按 [MAX_EDGE] 等比降采样。
     * 一次性读入字节数组，避免依赖输入流的 mark/reset（content Uri 流不保证支持）。
     */
    private fun decodeSampled(bytes: ByteArray): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)

        var sampleSize = 1
        while (bounds.outWidth / sampleSize > MAX_EDGE || bounds.outHeight / sampleSize > MAX_EDGE) {
            sampleSize *= 2
        }
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
    }

    /** 将处理结果 Bitmap 保存到系统相册，成功返回其 content Uri（需在 IO 线程调用） */
    fun saveToGallery(context: Context, bitmap: Bitmap): Uri? {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "GPUImage_$timestamp.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/GPUImage")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        val uri = context.contentResolver.insert(collection, values) ?: return null

        val written = runCatching {
            context.contentResolver.openOutputStream(uri)?.use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, output)
            } ?: false
        }.getOrDefault(false)

        if (!written) {
            context.contentResolver.delete(uri, null, null)
            return null
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            context.contentResolver.update(uri, values, null, null)
        }
        return uri
    }
}
