package com.example.william.my.core.netty.client

import com.example.william.my.core.netty.NettyClientInfo
import com.example.william.my.core.netty.NettyLogger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.netty.channel.Channel
import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap

/**
 * Netty 客户端 RxJava 封装
 *
 * 使用 RxJava Observable 方式处理 Netty 事件
 */
object NettyClientRx {

    private val channelMap = ConcurrentHashMap<String, Channel>()
    private val workerGroupMap = ConcurrentHashMap<String, EventLoopGroup>()
    private val disposableMap = ConcurrentHashMap<String, Disposable>()

    fun setChannel(host: String, port: Int, channel: Channel) {
        channelMap["$host:$port"] = channel
    }

    fun getChannel(host: String, port: Int): Channel? {
        return channelMap["$host:$port"]
    }

    /**
     * 创建连接 Observable
     *
     * @param host 服务器地址
     * @param port 服务器端口
     * @return Observable<NettyClientInfo>
     */
    fun createConnection(
        host: String,
        port: Int
    ): Observable<NettyClientInfo> {
        val key = "$host:$port"

        // 如果已有缓存的订阅，直接返回当前连接状态
        disposableMap[key]?.let { existing ->
            if (!existing.isDisposed) {
                val channel = channelMap[key]
                return if (channel != null && channel.isActive) {
                    Observable.just(NettyClientInfo.Open(host, port))
                } else {
                    Observable.empty()
                }
            }
        }

        return Observable.create(NettyClientRxOnSubscribe(host, port))
            .retry { throwable ->
                throwable is java.net.ConnectException || throwable is java.io.IOException
            }
            .doOnDispose {
                channelMap.remove(key)
                workerGroupMap.remove(key)
                disposableMap.remove(key)
            }
            .doOnNext { info ->
                when (info) {
                    is NettyClientInfo.Open -> {
                        // channel 已在 OnSubscribe 中设置
                    }
                    is NettyClientInfo.Closed -> {
                        channelMap.remove(key)
                        workerGroupMap.remove(key)
                        disposableMap.remove(key)
                    }
                    else -> { /* no-op */ }
                }
            }
            .share()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 发送文本消息
     */
    fun send(host: String, port: Int, message: String): Boolean {
        val channel = getChannel(host, port) ?: return false
        return try {
            if (channel.isActive && channel.isWritable) {
                channel.writeAndFlush("$message\n")
                true
            } else {
                NettyLogger.debug("Client channel not ready")
                false
            }
        } catch (e: Exception) {
            NettyLogger.error("send failed: $host:$port", e)
            false
        }
    }

    /**
     * 关闭连接
     */
    fun close(host: String, port: Int) {
        val key = "$host:$port"
        getChannel(host, port)?.let { channel ->
            try {
                channel.close().sync()
            } catch (e: Exception) {
                NettyLogger.error("close failed: $key", e)
            }
        }
        channelMap.remove(key)
        workerGroupMap[key]?.shutdownGracefully()
        workerGroupMap.remove(key)
        disposableMap[key]?.dispose()
        disposableMap.remove(key)
    }

    /**
     * 订阅连接
     */
    fun subscribe(host: String, port: Int): Disposable {
        val key = "$host:$port"
        disposableMap[key]?.dispose()
        val disposable = createConnection(host, port).subscribe()
        disposableMap[key] = disposable
        return disposable
    }
}
