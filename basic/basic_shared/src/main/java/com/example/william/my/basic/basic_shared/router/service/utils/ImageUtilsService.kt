package com.example.william.my.basic.basic_shared.router.service.utils

import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import java.io.File

/**
 * 图片处理服务接口
 */
interface ImageUtilsService {
    fun save(bitmap: Bitmap, filePath: String, format: CompressFormat): Boolean
    fun save(bitmap: Bitmap, file: File, format: CompressFormat): Boolean
}
