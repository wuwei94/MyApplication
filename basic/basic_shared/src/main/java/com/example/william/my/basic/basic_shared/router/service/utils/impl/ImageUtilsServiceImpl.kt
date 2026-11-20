package com.example.william.my.basic.basic_shared.router.service.utils.impl

import android.graphics.Bitmap
import com.blankj.utilcode.util.ImageUtils
import com.example.william.my.basic.basic_shared.router.service.utils.ImageUtilsService
import java.io.File
import javax.inject.Inject

/**
 * 图片处理服务实现
 */
class ImageUtilsServiceImpl @Inject constructor() : ImageUtilsService {

    override fun save(bitmap: Bitmap, filePath: String, format: Bitmap.CompressFormat): Boolean = ImageUtils.save(bitmap, filePath, format)

    override fun save(bitmap: Bitmap, file: File, format: Bitmap.CompressFormat): Boolean = ImageUtils.save(bitmap, file, format)
}
