package com.example.william.my.core.netty.client

import com.example.william.my.core.netty.NettyClientInfo
import com.example.william.my.core.netty.NettyLogger
import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import java.net.InetSocketAddress

/**
 * Netty 客户端 RxJava 连接订阅（建立连接并以 Observable 发射事件）
 */
class NettyClientRxOnSubscribe(
    private val host: String,
    private val port: Int,
) : ObservableOnSubscribe<NettyClientInfo> {

    override fun subscribe(emitter: ObservableEmitter<NettyClientInfo>) {
        val workerGroup: EventLoopGroup = NioEventLoopGroup()
        val handler = NettyClientRxHandler(emitter)

        try {
            val bootstrap = Bootstrap()
            bootstrap.group(workerGroup)
            bootstrap.channel(NioSocketChannel::class.java)
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true)
            bootstrap.handler(NettyClientRxInitializer(handler))

            val future = bootstrap.connect(InetSocketAddress(host, port)).sync()
            val channel = future.channel()

            // 保存 channel 引用
            NettyClientRx.setChannel(host, port, channel)

            if (!emitter.isDisposed) {
                emitter.onNext(NettyClientInfo.Open(host, port))
            }
            NettyLogger.debug("Client connected to $host:$port")

            // 等待连接关闭
            channel.closeFuture().sync()
            if (!emitter.isDisposed) {
                emitter.onNext(NettyClientInfo.Closed("Connection closed"))
            }
        } catch (e: Exception) {
            NettyLogger.error("Client connect failed: $host:$port", e)
            if (!emitter.isDisposed) {
                emitter.onError(e)
            }
        } finally {
            workerGroup.shutdownGracefully()
        }
    }
}
