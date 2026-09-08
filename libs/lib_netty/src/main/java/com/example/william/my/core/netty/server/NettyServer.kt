package com.example.william.my.core.netty.server

import com.example.william.my.core.netty.NettyLogger
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import java.net.InetSocketAddress
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Netty TCP 服务端封装
 *
 * 服务端以进程内单例运行，事件（启动/停止/接入/收发/异常）会派发给所有已注册的
 * [NettyServerHandler.OnMessageListener]。
 *
 * 典型用法：
 * - 随 [start] 传入 listener（随服务停止自动注销，如 Service 内部使用）；
 * - 或通过 [addListener]/[removeListener] 随时订阅/退订（如页面 [onStart]/[onStop] 中注册，
 *   跨服务重启持续生效，需自行退订避免泄漏）。
 */
object NettyServer {

    private var channel: Channel? = null
    private var bossGroup: EventLoopGroup? = null
    private var workerGroup: EventLoopGroup? = null
    private var serverHandler: NettyServerHandler? = null

    /**
     * 进程内订阅者列表（线程安全，可来自多个模块/页面）。
     */
    private val listeners = CopyOnWriteArrayList<NettyServerHandler.OnMessageListener>()

    /**
     * 本次 [start] 传入的监听器，[stop] 时自动从订阅列表移除。
     */
    private var startListener: NettyServerHandler.OnMessageListener? = null

    /**
     * 事件派发器：把 NettyServerHandler 的连接级事件转发给所有订阅者。
     */
    private val dispatcher = object : NettyServerHandler.OnMessageListener {
        override fun onClientConnected(remoteAddress: String) {
            listeners.forEach { it.onClientConnected(remoteAddress) }
        }

        override fun onClientDisconnected(remoteAddress: String) {
            listeners.forEach { it.onClientDisconnected(remoteAddress) }
        }

        override fun onMessage(remoteAddress: String, message: String) {
            listeners.forEach { it.onMessage(remoteAddress, message) }
        }

        override fun onError(remoteAddress: String, throwable: Throwable) {
            listeners.forEach { it.onError(remoteAddress, throwable) }
        }
    }

    /**
     * 订阅服务端事件（可多次调用，与服务端是否已启动无关，重启后继续生效）。
     */
    fun addListener(listener: NettyServerHandler.OnMessageListener) {
        if (listener !in listeners) {
            listeners.add(listener)
        }
    }

    /**
     * 退订服务端事件。
     */
    fun removeListener(listener: NettyServerHandler.OnMessageListener) {
        listeners.remove(listener)
    }

    fun start(
        port: Int,
        listener: NettyServerHandler.OnMessageListener? = null,
    ) {
        if (isRunning()) {
            return
        }

        bossGroup = NioEventLoopGroup()
        workerGroup = NioEventLoopGroup()
        serverHandler = NettyServerHandler().apply {
            this.listener = dispatcher
        }

        if (listener != null) {
            addListener(listener)
            startListener = listener
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
            listeners.forEach { it.onStarted(port) }
        } catch (e: Exception) {
            NettyLogger.error("Server start failed", e)
            // 启动失败同样通知订阅者，便于界面展示真实结果（只报错，不再广播停止）
            listeners.forEach { it.onError("", e) }
            channel?.close()
            channel = null
            workerGroup?.shutdownGracefully()
            workerGroup = null
            bossGroup?.shutdownGracefully()
            bossGroup = null
            serverHandler = null
            startListener?.let { listeners.remove(it) }
            startListener = null
        }
    }

    fun stop() {
        val wasRunning = channel?.isActive == true
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

        // 仅在实际运行过的情况下广播停止事件，避免“从未启动也提示已停止”
        if (wasRunning) {
            listeners.forEach { it.onStopped() }
        }
        // 仅移除随 start 注册的监听器；addListener 注册的订阅者保留，重启后继续接收事件
        startListener?.let { listeners.remove(it) }
        startListener = null
    }

    fun broadcast(message: String) {
        serverHandler?.broadcast(message)
    }

    fun getConnectionCount(): Int = serverHandler?.getConnectionCount() ?: 0

    fun isRunning(): Boolean = channel?.isActive == true
}
