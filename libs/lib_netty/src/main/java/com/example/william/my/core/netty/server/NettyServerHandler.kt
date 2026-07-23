package com.example.william.my.core.netty.server

import com.example.william.my.core.netty.NettyLogger
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.group.ChannelGroup
import io.netty.channel.group.DefaultChannelGroup
import io.netty.util.concurrent.GlobalEventExecutor

class NettyServerHandler : SimpleChannelInboundHandler<String>() {

    private val channels: ChannelGroup = DefaultChannelGroup(GlobalEventExecutor.INSTANCE)
    var listener: OnMessageListener? = null

    override fun handlerAdded(ctx: ChannelHandlerContext) {
        val channel = ctx.channel()
        channels.add(channel)
        NettyLogger.debug("Server handlerAdded: ${channel.remoteAddress()}")
        listener?.onClientConnected(channel.remoteAddress().toString())
    }

    override fun handlerRemoved(ctx: ChannelHandlerContext) {
        val channel = ctx.channel()
        channels.remove(channel)
        NettyLogger.debug("Server handlerRemoved: ${channel.remoteAddress()}")
        listener?.onClientDisconnected(channel.remoteAddress().toString())
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: String) {
        val inComing = ctx.channel()
        NettyLogger.debug("Server channelRead0: ${inComing.remoteAddress()} - $msg")

        // 广播消息给其他客户端
        for (channel in channels) {
            if (channel !== inComing) {
                channel.writeAndFlush("[${inComing.remoteAddress()}]: $msg\n")
            } else {
                channel.writeAndFlush("[localhost]: $msg\n")
            }
        }

        listener?.onMessage(inComing.remoteAddress().toString(), msg)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        NettyLogger.error("Server exceptionCaught", cause)
        listener?.onError(ctx.channel().remoteAddress().toString(), cause)
        ctx.close()
    }

    fun broadcast(message: String) {
        channels.writeAndFlush("$message\n")
    }

    fun getConnectionCount(): Int {
        return channels.size
    }

    interface OnMessageListener {
        fun onClientConnected(remoteAddress: String) {}
        fun onClientDisconnected(remoteAddress: String) {}
        fun onMessage(remoteAddress: String, message: String) {}
        fun onError(remoteAddress: String, throwable: Throwable) {}
    }
}
