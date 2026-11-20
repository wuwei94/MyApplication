package com.example.william.my.basic.basic_shared.router.service.utils

import java.io.File
import java.io.InputStream

/**
 * 文件读写服务接口
 */
interface FileIOUtilsService {

    fun writeFileFromIS(file: File, inputStream: InputStream): Boolean

    fun writeFileFromString(file: File, str: String): Boolean
}
