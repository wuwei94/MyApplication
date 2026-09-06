package com.example.william.my.basic.basic_shared.router.service.utils

import com.alibaba.android.arouter.facade.template.IProvider
import java.io.File
import java.io.InputStream

/**
 * 文件读写路由服务接口
 */
interface FileIOUtilsService : IProvider {

    fun writeFileFromIS(file: File, inputStream: InputStream): Boolean

    fun writeFileFromString(file: File, str: String): Boolean
}
