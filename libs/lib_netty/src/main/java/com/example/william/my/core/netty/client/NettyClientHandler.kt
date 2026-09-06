package com.example.william.my.core.netty.client

import com.example.william.my.core.netty.NettyLogger
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

/**
 * Netty 客户端消息处理器
 */
class NettyClientHandler : SimpleChannelInboundHandler<String?>() {

    var listener: OnMessageListener? = null

    override fun channelActive(ctx: ChannelHandlerContext) {
        NettyLogger.debug("Client channelActive: ${ctx.channel().remoteAddress()}")
        listener?.onConnected(ctx.channel().remoteAddress().toString())
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        NettyLogger.debug("Client channelInactive")
        listener?.onDisconnected()
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: String?) {
        NettyLogger.debug("Client channelRead0: $msg")
        listener?.onMessage(msg ?: "")
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        NettyLogger.error("Client exceptionCaught", cause)
        listener?.onError(cause)
        ctx.close()
    }

    /**
     * 客户端消息监听器
     */
    interface OnMessageListener {
        fun onConnected(remoteAddress: String) {}
        fun onMessage(message: String) {}
        fun onDisconnected() {}
        fun onError(throwable: Throwable) {}
    }
}
