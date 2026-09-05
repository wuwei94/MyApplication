package com.example.william.my.core.netty.server

import com.example.william.my.core.netty.NettyLogger
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import java.net.InetSocketAddress

object NettyServer {

    private var channel: Channel? = null
    private var bossGroup: EventLoopGroup? = null
    private var workerGroup: EventLoopGroup? = null
    private var serverHandler: NettyServerHandler? = null

    fun start(
        port: Int,
        listener: NettyServerHandler.OnMessageListener? = null,
    ) {
        bossGroup = NioEventLoopGroup()
        workerGroup = NioEventLoopGroup()
        serverHandler = NettyServerHandler().apply {
            this.listener = listener
        }

        try {
            val serverBootstrap = ServerBootstrap()
            serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel::class.java)
                .childHandler(NettyServerInitializer(serverHandler!!))
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)

            val future = serverBootstrap.bind(InetSocketAddress(port)).sync()
            channel = future.channel()
            NettyLogger.debug("Server started on port $port")
        } catch (e: Exception) {
            NettyLogger.error("Server start failed", e)
        }
    }

    fun stop() {
        try {
            channel?.close()?.sync()
            channel = null
            workerGroup?.shutdownGracefully()
            workerGroup = null
            bossGroup?.shutdownGracefully()
            bossGroup = null
            serverHandler = null
            NettyLogger.debug("Server stopped")
        } catch (e: Exception) {
            NettyLogger.error("Server stop failed", e)
        }
    }

    fun broadcast(message: String) {
        serverHandler?.broadcast(message)
    }

    fun getConnectionCount(): Int = serverHandler?.getConnectionCount() ?: 0

    fun isRunning(): Boolean = channel?.isActive == true
}
