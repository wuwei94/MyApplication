package com.example.william.my.basic.basic_shared.router.service.utils.impl

import android.content.Context
import android.graphics.Bitmap
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.ImageUtils
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.basic.basic_shared.router.service.utils.ImageUtilsService
import java.io.File

/**
 * 图片处理路由服务实现
 */
@Route(path = RouterPath.Service.ImageUtilsService)
class ImageUtilsServiceImpl : ImageUtilsService {

    override fun save(bitmap: Bitmap, filePath: String, format: Bitmap.CompressFormat): Boolean = ImageUtils.save(bitmap, filePath, format)

    override fun save(bitmap: Bitmap, file: File, format: Bitmap.CompressFormat): Boolean = ImageUtils.save(bitmap, file, format)

    override fun init(context: Context) {}
}
