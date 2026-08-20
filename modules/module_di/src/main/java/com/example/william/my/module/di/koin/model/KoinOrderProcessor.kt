package com.example.william.my.module.di.koin.model

/**
 * 模拟订单处理器（工厂模式，每次新建）
 */
class KoinOrderProcessor {
    fun processOrder(orderId: Long): String = "OrderProcessor 处理订单 [#$orderId]"
}
