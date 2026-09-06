package com.example.william.my.core.netty.client

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.DelimiterBasedFrameDecoder
import io.netty.handler.codec.Delimiters
import io.netty.handler.codec.string.StringDecoder
import io.netty.handler.codec.string.StringEncoder

/**
 * Netty 客户端 RxJava 管道初始化器
 */
class NettyClientRxInitializer(
    private val handler: NettyClientRxHandler,
) : ChannelInitializer<SocketChannel>() {

    override fun initChannel(ch: SocketChannel) {
        val pipeline = ch.pipeline()

        // 以("\n")为结尾分割的解码器
        val delimiter = Delimiters.lineDelimiter()
        pipeline.addLast("framer", DelimiterBasedFrameDecoder(2048, *delimiter))

        // 字符串解码 和 编码
        pipeline.addLast("decoder", StringDecoder())
        pipeline.addLast("encoder", StringEncoder())

        // 自定义处理器
        pipeline.addLast("handler", handler)
    }
}
