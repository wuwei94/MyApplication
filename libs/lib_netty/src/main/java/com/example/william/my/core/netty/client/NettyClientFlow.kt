package com.example.william.my.core.netty.client

import com.example.william.my.core.netty.NettyClientInfo
import com.example.william.my.core.netty.NettyLogger
import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.LineBasedFrameDecoder
import io.netty.handler.codec.string.StringDecoder
import io.netty.handler.codec.string.StringEncoder
import io.netty.util.CharsetUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap

/**
 * Netty TCP 客户端（Kotlin Coroutines Flow 封装）
 *
 * 使用 callbackFlow 将 Netty TCP Socket 事件转为响应式 Flow 数据流。
 * 协程取消或生命周期结束时自动安全关闭连接与释放 EventLoopGroup。
 */
object NettyClientFlow {

    private val channelMap = ConcurrentHashMap<String, Channel>()
    private val workerGroupMap = ConcurrentHashMap<String, EventLoopGroup>()

    fun setChannel(host: String, port: Int, channel: Channel) {
        channelMap["$host:$port"] = channel
    }

    fun getChannel(host: String, port: Int): Channel? = channelMap["$host:$port"]

    fun createConnection(host: String, port: Int): Flow<NettyClientInfo> = callbackFlow {
        val key = "$host:$port"
        val workerGroup: EventLoopGroup = NioEventLoopGroup()
        workerGroupMap[key] = workerGroup

        try {
            val handler = object : SimpleChannelInboundHandler<String?>() {
                override fun channelActive(ctx: ChannelHandlerContext) {
                    NettyLogger.debug("Flow channelActive: ${ctx.channel().remoteAddress()}")
                }

                override fun channelInactive(ctx: ChannelHandlerContext) {
                    NettyLogger.debug("Flow channelInactive: $key")
                    channelMap.remove(key)
                    trySend(NettyClientInfo.Closed("Connection closed"))
                    close()
                }

                override fun channelRead0(ctx: ChannelHandlerContext, msg: String?) {
                    NettyLogger.debug("Flow channelRead0: $msg")
                    trySend(NettyClientInfo.TextMessage(msg ?: ""))
                }

                override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
                    NettyLogger.error("Flow exceptionCaught: $key", cause)
                    trySend(NettyClientInfo.Error(cause))
                    ctx.close()
                }
            }

            val bootstrap = Bootstrap().apply {
                group(workerGroup)
                channel(NioSocketChannel::class.java)
                option(ChannelOption.SO_KEEPALIVE, true)
                handler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(ch: SocketChannel) {
                        val pipeline = ch.pipeline()
                        pipeline.addLast(LineBasedFrameDecoder(1024))
                        pipeline.addLast(StringDecoder(CharsetUtil.UTF_8))
                        pipeline.addLast(StringEncoder(CharsetUtil.UTF_8))
                        pipeline.addLast(handler)
                    }
                })
            }

            val future = bootstrap.connect(InetSocketAddress(host, port)).sync()
            val channel = future.channel()
            channelMap[key] = channel

            NettyLogger.debug("Flow Client connected to $host:$port")
            trySend(NettyClientInfo.Open(host, port))
        } catch (e: Exception) {
            NettyLogger.error("Flow Client connect failed: $host:$port", e)
            trySend(NettyClientInfo.Error(e))
            close(e)
        }

        awaitClose {
            NettyLogger.debug("Flow awaitClose: $key")
            channelMap.remove(key)?.let { channel ->
                try {
                    if (channel.isActive) {
                        channel.close().sync()
                    }
                } catch (e: Exception) {
                    NettyLogger.error("close failed in awaitClose: $key", e)
                }
            }
            workerGroupMap.remove(key)?.shutdownGracefully()
        }
    }.flowOn(Dispatchers.IO)

    fun send(host: String, port: Int, message: String): Boolean {
        val channel = getChannel(host, port) ?: return false
        return try {
            if (channel.isActive && channel.isWritable) {
                channel.writeAndFlush("$message\n")
                true
            } else {
                NettyLogger.debug("Flow Client channel not ready")
                false
            }
        } catch (e: Exception) {
            NettyLogger.error("send failed: $host:$port", e)
            false
        }
    }

    fun close(host: String, port: Int) {
        val key = "$host:$port"
        channelMap.remove(key)?.let { channel ->
            try {
                channel.close().sync()
            } catch (e: Exception) {
                NettyLogger.error("close failed: $key", e)
            }
        }
        workerGroupMap.remove(key)?.shutdownGracefully()
        workerGroupMap.remove(key)
    }

    fun isConnected(host: String, port: Int): Boolean {
        val channel = getChannel(host, port)
        return channel != null && channel.isActive
    }
}
