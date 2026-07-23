package com.example.william.my.core.netty.client

import com.example.william.my.core.netty.NettyClientInfo
import com.example.william.my.core.netty.NettyLogger
import io.reactivex.rxjava3.core.ObservableEmitter
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

class NettyClientRxHandler(
    private val emitter: ObservableEmitter<NettyClientInfo>
) : SimpleChannelInboundHandler<String?>() {

    override fun channelActive(ctx: ChannelHandlerContext) {
        NettyLogger.debug("Client channelActive: ${ctx.channel().remoteAddress()}")
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        NettyLogger.debug("Client channelInactive")
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: String?) {
        NettyLogger.debug("Client channelRead0: $msg")
        if (!emitter.isDisposed) {
            emitter.onNext(NettyClientInfo.TextMessage(msg ?: ""))
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        NettyLogger.error("Client exceptionCaught", cause)
        if (!emitter.isDisposed) {
            emitter.onNext(NettyClientInfo.Error(cause))
        }
        ctx.close()
    }
}
