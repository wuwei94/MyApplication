package com.example.william.my.core.netty.client

import com.example.william.my.core.netty.NettyClientInfo
import com.example.william.my.core.netty.NettyLogger
import io.reactivex.rxjava3.observers.DisposableObserver

/**
 * Netty 客户端 RxJava Observer
 *
 * 提供事件回调的抽象类，简化 RxJava 使用
 */
abstract class NettyClientRxObserver : DisposableObserver<NettyClientInfo>() {

    override fun onNext(info: NettyClientInfo) {
        when (info) {
            is NettyClientInfo.Open -> onConnected(info.host, info.port)
            is NettyClientInfo.TextMessage -> onMessage(info.message)
            is NettyClientInfo.Closed -> onClosed(info.reason)
            is NettyClientInfo.Error -> onError(info.exception)
        }
    }

    override fun onError(e: Throwable) {
        NettyLogger.error("WebSocket error", e)
    }

    override fun onComplete() {
        // 空实现 — 不在此处自动 dispose
    }

    /**
     * 连接成功回调
     */
    protected open fun onConnected(host: String, port: Int) {}

    /**
     * 收到消息回调
     */
    protected open fun onMessage(message: String) {}

    /**
     * 连接关闭回调
     */
    protected open fun onClosed(reason: String) {}

    /**
     * 连接错误回调
     */
    protected open fun onError(exception: Exception) {}
}
