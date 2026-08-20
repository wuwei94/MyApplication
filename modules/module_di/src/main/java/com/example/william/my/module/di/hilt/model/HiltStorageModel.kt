package com.example.william.my.module.di.hilt.model

import javax.inject.Inject

/**
 * 存储服务抽象接口
 */
interface HiltStorageService {
    fun saveData(key: String, value: String): String
}

/**
 * 磁盘存储具体实现
 */
class HiltDiskStorageServiceImpl @Inject constructor() : HiltStorageService {
    override fun saveData(key: String, value: String): String {
        return "DiskStorageService: 成功写入本地磁盘 [$key -> $value]"
    }
}
