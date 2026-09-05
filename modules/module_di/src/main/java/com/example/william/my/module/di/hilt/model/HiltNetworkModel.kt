package com.example.william.my.module.di.hilt.model

import javax.inject.Inject

/**
 * 模拟第三方网络客户端（无法直接添加 @Inject constructor 注解的类）
 */
class HiltNetworkClient(val baseUrl: String, val timeoutSeconds: Long) {
    fun request(path: String): String = "NetworkClient: GET $baseUrl$path (timeout: ${timeoutSeconds}s)"
}

/**
 * API 服务接口
 */
interface HiltApiService {
    fun fetchData(): String
}

/**
 * 生产环境 API 服务实现
 */
class HiltProdApiServiceImpl @Inject constructor() : HiltApiService {
    override fun fetchData(): String = "ProdApiService: 连接生产集群 https://prod.example.com/api"
}

/**
 * 测试环境 API 服务实现
 */
class HiltDevApiServiceImpl @Inject constructor() : HiltApiService {
    override fun fetchData(): String = "DevApiService: 连接测试沙箱 https://dev.example.com/api"
}
