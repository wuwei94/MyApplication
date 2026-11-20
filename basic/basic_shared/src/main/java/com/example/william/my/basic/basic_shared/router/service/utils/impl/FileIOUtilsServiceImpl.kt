package com.example.william.my.basic.basic_shared.router.service.utils.impl

import com.blankj.utilcode.util.FileIOUtils
import com.example.william.my.basic.basic_shared.router.service.utils.FileIOUtilsService
import java.io.File
import java.io.InputStream
import javax.inject.Inject

/**
 * 文件读写服务实现
 */
class FileIOUtilsServiceImpl @Inject constructor() : FileIOUtilsService {

    override fun writeFileFromIS(file: File, inputStream: InputStream): Boolean = FileIOUtils.writeFileFromIS(file, inputStream)

    override fun writeFileFromString(file: File, str: String): Boolean = FileIOUtils.writeFileFromString(file, str)
}
