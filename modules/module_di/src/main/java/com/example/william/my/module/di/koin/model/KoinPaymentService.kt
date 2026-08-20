package com.example.william.my.module.di.koin.model

/**
 * 支付服务接口
 */
interface KoinPaymentService {
    fun pay(amount: Double): String
}

/**
 * 支付宝支付实现
 */
class KoinAliPayServiceImpl : KoinPaymentService {
    override fun pay(amount: Double): String = "AliPayService 支付宝支付: ￥$amount 成功"
}

/**
 * 微信支付实现
 */
class KoinWeChatPayServiceImpl : KoinPaymentService {
    override fun pay(amount: Double): String = "WeChatPayService 微信支付: ￥$amount 成功"
}
