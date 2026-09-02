package com.example.william.my.basic.basic_shared.router.service.utils.impl

import android.content.Context
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.FileIOUtils
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.basic.basic_shared.router.service.utils.FileIOUtilsService
import java.io.File
import java.io.InputStream

/**
 * 文件读写路由服务实现
 */
@Route(path = RouterPath.Service.FileIOUtilsService)
class FileIOUtilsServiceImpl : FileIOUtilsService {

    override fun writeFileFromIS(file: File, inputStream: InputStream): Boolean {
        return FileIOUtils.writeFileFromIS(file, inputStream)
    }

    override fun writeFileFromString(file: File, str: String): Boolean {
        return FileIOUtils.writeFileFromString(file, str)
    }

    override fun init(context: Context) {}
}