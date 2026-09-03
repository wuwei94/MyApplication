package com.example.william.my.core.server.netty

import com.example.william.my.core.netty.server.NettyServer
import com.example.william.my.core.netty.server.NettyServerHandler

class NettyWebSocketServer {

    private var listener: OnMessageListener? = null

    fun setOnMessageListener(listener: OnMessageListener) {
        this.listener = listener
    }

    fun start(port: Int = DEFAULT_PORT) {
        NettyServer.start(
            port = port,
            listener = object : NettyServerHandler.OnMessageListener {
                override fun onClientConnected(remoteAddress: String) {
                    listener?.onClientConnected(remoteAddress)
                }

                override fun onClientDisconnected(remoteAddress: String) {
                    listener?.onClientDisconnected(remoteAddress)
                }

                override fun onMessage(remoteAddress: String, message: String) {
                    listener?.onMessage(remoteAddress, message)
                }

                override fun onError(remoteAddress: String, throwable: Throwable) {
                    listener?.onError(remoteAddress, throwable)
                }
            },
        )
    }

    fun stop() {
        NettyServer.stop()
    }

    fun isRunning(): Boolean = NettyServer.isRunning()

    fun broadcast(message: String) {
        NettyServer.broadcast(message)
    }

    interface OnMessageListener {
        fun onClientConnected(remoteAddress: String)
        fun onClientDisconnected(remoteAddress: String)
        fun onMessage(remoteAddress: String, message: String)
        fun onError(remoteAddress: String, throwable: Throwable)
    }

    companion object {
        const val DEFAULT_PORT = 5567
    }
}
