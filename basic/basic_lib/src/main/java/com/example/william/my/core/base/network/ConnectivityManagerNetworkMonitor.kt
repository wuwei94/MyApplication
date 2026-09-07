package com.example.william.my.core.base.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.core.content.getSystemService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn

/**
 * 基于系统 [ConnectivityManager] 的响应式网络监听实现
 *
 * 核心设计：
 * 1. 【生命周期与泄漏安全】：基于 [callbackFlow] 构建，在 [awaitClose] 中主动反注册 [NetworkCallback]，确保订阅取消时无资源泄漏；
 * 2. 【双重能力校验】：通过 [NetworkCapabilities.NET_CAPABILITY_INTERNET] 校验设备具备互联网通路，并过滤无网本地连接；
 * 3. 【零延迟初始探测】：流启动瞬间主动探测当前活跃网络（[ConnectivityManager.getActiveNetwork]），避免回调到达前的空白期；
 * 4. 【背压与防抖】：结合 [conflate] 与 [distinctUntilChanged]，消除高频弱网切换造成的 UI 抖动。
 */
class ConnectivityManagerNetworkMonitor(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : NetworkMonitor {

    override val isOnline: Flow<Boolean> = callbackFlow {
        val connectivityManager = context.getSystemService<ConnectivityManager>()
        if (connectivityManager == null) {
            channel.trySend(false)
            channel.close()
            return@callbackFlow
        }

        val callback = object : NetworkCallback() {
            private val networks = mutableSetOf<Network>()

            override fun onAvailable(network: Network) {
                networks += network
                channel.trySend(true)
            }

            override fun onLost(network: Network) {
                networks -= network
                channel.trySend(networks.isNotEmpty())
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities,
            ) {
                val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                if (hasInternet) {
                    networks += network
                } else {
                    networks -= network
                }
                channel.trySend(networks.isNotEmpty())
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)

        // 立即下发当前活跃网络的初始连接状态
        channel.trySend(connectivityManager.isCurrentlyConnected())

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
        .flowOn(ioDispatcher)
        .conflate()
        .distinctUntilChanged()

    private fun ConnectivityManager.isCurrentlyConnected(): Boolean {
        val activeNet = activeNetwork ?: return false
        val capabilities = getNetworkCapabilities(activeNet) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
