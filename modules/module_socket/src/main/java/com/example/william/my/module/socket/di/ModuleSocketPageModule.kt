package com.example.william.my.module.socket.di

import android.app.Activity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.socket.SocketMainActivity
import com.example.william.my.module.socket.tcpsocket.netty.activity.NettyTcpSocketClientActivity
import com.example.william.my.module.socket.tcpsocket.netty.activity.NettyTcpSocketClientFlowActivity
import com.example.william.my.module.socket.tcpsocket.netty.activity.NettyTcpSocketClientRxActivity
import com.example.william.my.module.socket.websocket.java.activity.JavaWebSocketClientActivity
import com.example.william.my.module.socket.websocket.java.activity.JavaWebSocketClientFlowActivity
import com.example.william.my.module.socket.websocket.java.activity.JavaWebSocketClientRxActivity
import com.example.william.my.module.socket.websocket.okhttp.activity.OkHttpWebSocketClientActivity
import com.example.william.my.module.socket.websocket.okhttp.activity.OkHttpWebSocketClientFlowActivity
import com.example.william.my.module.socket.websocket.okhttp.activity.OkHttpWebSocketClientRxActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * module_socket 页面路由 Hilt 多重绑定模块
 */
@Module
@InstallIn(SingletonComponent::class)
object ModuleSocketPageModule {

    @Provides
    @IntoMap
    @StringKey(RouterPath.Socket.Main)
    fun provideSocketMainActivity(): Class<out Activity> = SocketMainActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Socket.NettyTcpSocketClient)
    fun provideNettyTcpSocketClientActivity(): Class<out Activity> = NettyTcpSocketClientActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Socket.NettyTcpSocketClientFlow)
    fun provideNettyTcpSocketClientFlowActivity(): Class<out Activity> = NettyTcpSocketClientFlowActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Socket.NettyTcpSocketClientRx)
    fun provideNettyTcpSocketClientRxActivity(): Class<out Activity> = NettyTcpSocketClientRxActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Socket.JavaWebSocketClient)
    fun provideJavaWebSocketClientActivity(): Class<out Activity> = JavaWebSocketClientActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Socket.JavaWebSocketClientFlow)
    fun provideJavaWebSocketClientFlowActivity(): Class<out Activity> = JavaWebSocketClientFlowActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Socket.JavaWebSocketClientRx)
    fun provideJavaWebSocketClientRxActivity(): Class<out Activity> = JavaWebSocketClientRxActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Socket.OkHttpWebSocketClient)
    fun provideOkHttpWebSocketClientActivity(): Class<out Activity> = OkHttpWebSocketClientActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Socket.OkHttpWebSocketClientFlow)
    fun provideOkHttpWebSocketClientFlowActivity(): Class<out Activity> = OkHttpWebSocketClientFlowActivity::class.java

    @Provides
    @IntoMap
    @StringKey(RouterPath.Socket.OkHttpWebSocketClientRx)
    fun provideOkHttpWebSocketClientRxActivity(): Class<out Activity> = OkHttpWebSocketClientRxActivity::class.java
}
