package com.example.william.my.core.netty.client

import com.example.william.my.core.netty.NettyLogger
import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import java.net.InetSocketAddress

object NettyClient {

    private var channel: Channel? = null
    private var workerGroup: EventLoopGroup? = null

    fun connect(
        host: String,
        port: Int,
        listener: NettyClientHandler.OnMessageListener? = null
    ) {
        workerGroup = NioEventLoopGroup()
        val handler = NettyClientHandler().apply {
            this.listener = listener
        }

        try {
            val bootstrap = Bootstrap()
            bootstrap.group(workerGroup)
            bootstrap.channel(NioSocketChannel::class.java)
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true)
            bootstrap.handler(NettyClientInitializer(handler))

            val future = bootstrap.connect(InetSocketAddress(host, port)).sync()
            channel = future.channel()
            NettyLogger.debug("Client connected to $host:$port")
        } catch (e: Exception) {
            NettyLogger.error("Client connect failed", e)
            listener?.onError(e)
        }
    }

    fun disconnect() {
        try {
            channel?.close()?.sync()
            channel = null
            workerGroup?.shutdownGracefully()
            workerGroup = null
            NettyLogger.debug("Client disconnected")
        } catch (e: Exception) {
            NettyLogger.error("Client disconnect failed", e)
        }
    }

    fun sendMessage(message: String): Boolean {
        val ch = channel ?: return false
        return try {
            if (ch.isActive && ch.isWritable) {
                ch.writeAndFlush("$message\n")
                true
            } else {
                NettyLogger.debug("Client channel not ready")
                false
            }
        } catch (e: Exception) {
            NettyLogger.error("Client sendMessage failed", e)
            false
        }
    }

    fun getAddress(): String {
        return channel?.remoteAddress()?.toString() ?: ""
    }

    fun isConnected(): Boolean {
        return channel?.isActive == true
    }

    /**
     * 获取当前连接的 Channel
     *
     * @return Channel 实例，未连接时返回 null
     */
    fun getChannel(): Channel? {
        return channel
    }

    /**
     * 获取指定地址的 Channel
     *
     * 由于 NettyClient 只维护单个连接，此方法直接返回当前连接
     *
     * @param host 服务器地址
     * @param port 服务器端口
     * @return Channel 实例，未连接时返回 null
     */
    fun getChannel(host: String, port: Int): Channel? {
        return channel
    }
}
