package com.example.william.my.core.base.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

/**
 * 网络状态感知与监听帮助类（基于现代 ConnectivityManager.NetworkCallback）
 */
@SuppressLint("MissingPermission")
object NetworkChangeHelper {

    private val TAG = this.javaClass.simpleName

    private var mNetworkChangeListener: NetworkChangeListener? = null
    private var mNetworkCallback: NetworkCallback? = null

    /**
     * 注册网络变化监听（传统监听器模式）
     */
    fun register(context: Context, networkChangeListener: NetworkChangeListener?) {
        mNetworkChangeListener = networkChangeListener
        val connectivityManager =
            context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return

        val callback = object : NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                mNetworkChangeListener?.onNetworkStatusChange(true)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                mNetworkChangeListener?.onNetworkStatusChange(false)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities,
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                    val isWifi = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    val isCellular = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    Log.i(TAG, "Network validated: wifi=$isWifi, cellular=$isCellular")
                }
            }
        }
        mNetworkCallback = callback
        connectivityManager.registerDefaultNetworkCallback(callback)
    }

    /**
     * 注销网络监听
     */
    fun unregister(context: Context) {
        val connectivityManager =
            context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        mNetworkCallback?.let {
            try {
                connectivityManager?.unregisterNetworkCallback(it)
            } catch (e: Exception) {
                Log.e(TAG, "unregisterNetworkCallback error", e)
            }
            mNetworkCallback = null
        }
        mNetworkChangeListener = null
    }

    /**
     * 以响应式 Flow 形式观察网络是否连通
     * 自动随收集生命周期注册与反注册，天然杜绝内存泄漏与多观察者互相覆盖问题。
     */
    fun observeNetwork(context: Context): Flow<Boolean> = callbackFlow {
        val connectivityManager =
            context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (connectivityManager == null) {
            channel.trySend(false)
            channel.close()
            return@callbackFlow
        }

        val callback = object : NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                trySend(false)
            }
        }

        connectivityManager.registerDefaultNetworkCallback(callback)

        awaitClose {
            try {
                connectivityManager.unregisterNetworkCallback(callback)
            } catch (e: Exception) {
                Log.e(TAG, "unregister callbackFlow error", e)
            }
        }
    }.conflate()

    interface NetworkChangeListener {
        /**
         * 网络状态改变
         *
         * @param isAvailable 是否可用
         */
        fun onNetworkStatusChange(isAvailable: Boolean)
    }
}
