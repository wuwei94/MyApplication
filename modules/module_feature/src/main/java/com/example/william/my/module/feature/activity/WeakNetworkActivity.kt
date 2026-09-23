package com.example.william.my.module.feature.activity

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.coroutine.collectWithLifecycle
import com.example.william.my.core.base.network.ConnectivityManagerNetworkMonitor
import com.example.william.my.core.base.network.NetworkMonitor
import com.example.william.my.core.okhttp.header.ControlHeaders
import com.example.william.my.core.okhttp.okHttpClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume

/**
 * 弱网处理 — 业务侧网络感知与降级策略实战
 *
 * 核心机制与避坑点：
 * 1. 网络感知：[ConnectivityManagerNetworkMonitor] 以 [NetworkMonitor.isOnline] 推送连通性，
 *    UI 用 [collectWithLifecycle] 订阅，生命周期低于 STARTED 时自动停止，避免回调泄漏；
 * 2. 超时降级：弱网下缩短 `callTimeout` 快速失败，减少用户等待与请求堆积；
 * 3. 退避重试：应用层指数退避有次数与间隔上限；网络恢复时由 `isOnline` 边沿触发自愈，避免重试风暴；
 * 4. 离线兜底：最近成功响应驻留业务内存，磁盘缓存走 OkHttp Cache + `CACHE_ALIVE_SECONDS` 控制头。
 *
 * 官方参考：
 * https://developer.android.com/training/monitoring-device-state/connectivity-status
 * https://square.github.io/okhttp/features/timeouts
 */
@Route(path = RouterPath.Feature.WeakNetwork)
class WeakNetworkActivity : BasicResponseActivity() {

    private lateinit var networkMonitor: NetworkMonitor

    /**
     * 正常链路 Client：15s 统一超时 + 连接失败重试，请求头可指定缓存存活秒数。
     */
    private val cacheClient: OkHttpClient by lazy {
        okHttpClient {
            timeout(NORMAL_TIMEOUT_SECONDS)
            retryOnConnectionFailure(true)
            cache(application as Application, "weak_network")
        }
    }

    /**
     * 短超时 Client：2s callTimeout，关闭连接失败重试，用于演示弱网快速失败。
     */
    private val shortTimeoutClient: OkHttpClient by lazy {
        okHttpClient {
            timeout(SHORT_TIMEOUT_SECONDS)
            retryOnConnectionFailure(false)
        }
    }

    /** 最近一次成功响应体，断网时作业务侧内存兜底 */
    private var lastSuccessBody: String? = null

    private var lastOnline: Boolean? = null
    private val pendingRecoveryRetry = AtomicBoolean(false)
    private var backoffJobRunning = false

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("弱网处理：网络感知 / 短超时快速失败 / 退避重试 / 离线缓存兜底 / 网络恢复自愈")

        networkMonitor = ConnectivityManagerNetworkMonitor(this)
        networkMonitor.isOnline.collectWithLifecycle(this) { online ->
            onNetworkStatusChanged(online)
        }
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 观察当前网络状态（NetworkMonitor）",
        "2. 正常请求（15s 超时 + 写入磁盘/内存缓存）",
        "3. 短超时快速失败（2s callTimeout）",
        "4. 失败自动退避重试（最多 3 次）",
        "5. 离线缓存兜底读取（内存 + 磁盘）",
        "6. 挂起网络恢复自动重试",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> observeNetworkStatus()
            1 -> requestNormal()
            2 -> requestShortTimeout()
            3 -> requestWithBackoffRetry()
            4 -> readOfflineFallback()
            5 -> armRecoveryRetry()
        }
    }

    private fun onNetworkStatusChanged(online: Boolean) {
        val previous = lastOnline
        lastOnline = online
        updateLog(NETWORK_LOG_KEY, "network=${if (online) "online" else "offline"}")

        when {
            previous == null -> {
                appendLog("→ [NetworkMonitor] 初始状态：${if (online) "online" else "offline"}")
            }

            previous == online -> Unit

            online -> {
                appendLog("✓ [NetworkMonitor] 网络已恢复")
                if (pendingRecoveryRetry.getAndSet(false)) {
                    appendLog("→ [Recovery] 检测到挂起请求，自动重试...")
                    requestNormal()
                }
            }

            else -> {
                appendLog("✗ [NetworkMonitor] 网络已断开")
                if (pendingRecoveryRetry.get()) {
                    updateLog(NETWORK_LOG_KEY, "network=offline | recovery=armed")
                }
            }
        }
    }

    private fun observeNetworkStatus() {
        appendLog("→ [NetworkMonitor] 当前 isOnline 订阅始终随生命周期生效")
        val online = lastOnline
        if (online == null) {
            appendLog("→ [NetworkMonitor] 等待首次状态推送...")
        } else {
            appendLog("✓ [NetworkMonitor] 当前状态：${if (online) "online" else "offline"}")
        }
    }

    private fun requestNormal() {
        appendLog("→ [Normal] 发起请求（timeout=${NORMAL_TIMEOUT_SECONDS}s + cache alive=${CACHE_ALIVE_SECONDS}s）...")
        enqueue(cacheClient, buildArticleRequest(cacheAliveSeconds = CACHE_ALIVE_SECONDS), TAG_NORMAL) { body ->
            lastSuccessBody = body
        }
    }

    private fun requestShortTimeout() {
        appendLog("→ [ShortTimeout] 发起请求（callTimeout=${SHORT_TIMEOUT_SECONDS}s，关闭连接失败重试）...")
        val request = Request.Builder()
            .url(articleListUrl())
            .get()
            .build()
        enqueue(shortTimeoutClient, request, TAG_SHORT_TIMEOUT)
    }

    private fun requestWithBackoffRetry() {
        if (backoffJobRunning) {
            appendLog("→ [Retry] 退避重试进行中...")
            return
        }
        backoffJobRunning = true
        appendLog("→ [Retry] 发起请求，失败后指数退避（最多 $MAX_RETRY 次）...")
        lifecycleScope.launch {
            var delayMs = INITIAL_BACKOFF_MS
            for (attempt in 1..MAX_RETRY) {
                if (attempt > 1) {
                    appendLog("→ [Retry] 退避 ${delayMs}ms 后进行第 $attempt 次尝试...")
                    delay(delayMs)
                    delayMs *= BACKOFF_MULTIPLIER
                } else {
                    appendLog("→ [Retry] 第 $attempt 次尝试...")
                }

                val result = awaitRequest(cacheClient, buildArticleRequest(CACHE_ALIVE_SECONDS))
                result.onSuccess { body ->
                    lastSuccessBody = body
                    appendLog("✓ [Retry] 第 $attempt 次请求成功")
                    appendFormatLog("✓ [Retry] ", preview(body))
                    backoffJobRunning = false
                    return@launch
                }.onFailure { e ->
                    appendLog("✗ [Retry] 第 $attempt 次失败：${e.message}")
                }
            }
            appendLog("✗ [Retry] 已达最大重试次数，可点击「6. 挂起网络恢复自动重试」")
            backoffJobRunning = false
        }
    }

    private fun readOfflineFallback() {
        val cached = lastSuccessBody
        if (cached == null) {
            appendLog("✗ [Offline:memory] 暂无内存兜底，请先成功请求一次")
        } else {
            appendLog("✓ [Offline:memory] 返回业务内存兜底数据")
            appendFormatLog("✓ [Offline:memory] ", preview(cached))
        }

        appendLog("→ [Offline:disk] 请求磁盘缓存（离线时强制 FORCE_CACHE）...")
        enqueue(
            cacheClient,
            buildArticleRequest(cacheAliveSeconds = CACHE_ALIVE_SECONDS),
            TAG_OFFLINE_DISK,
        ) { body ->
            lastSuccessBody = body
        }
    }

    private fun armRecoveryRetry() {
        if (lastOnline == true) {
            appendLog("→ [Recovery] 当前在线，直接发起请求")
            requestNormal()
            return
        }
        pendingRecoveryRetry.set(true)
        appendLog("→ [Recovery] 已挂起自动重试，网络恢复后由 NetworkMonitor 边沿触发")
        updateLog(NETWORK_LOG_KEY, "network=${if (lastOnline == false) "offline" else "unknown"} | recovery=armed")
    }

    private fun enqueue(
        target: OkHttpClient,
        request: Request,
        tag: String,
        onSuccess: ((String) -> Unit)? = null,
    ) {
        target.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                appendLog("✗ $tag 请求失败：${e.message}")
                if (lastSuccessBody != null || tag == TAG_NORMAL) {
                    appendLog("→ $tag 可点击「5. 离线缓存兜底读取」")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        appendLog("✗ $tag 响应异常：HTTP ${response.code}")
                        return
                    }
                    val body = response.body.string()
                    onSuccess?.invoke(body)
                    val fromCache = response.cacheResponse != null && response.networkResponse == null
                    if (fromCache) {
                        appendLog("✓ $tag 命中磁盘缓存")
                    } else {
                        appendLog("✓ $tag 请求成功")
                    }
                    appendFormatLog("✓ $tag ", preview(body))
                }
            }
        })
    }

    private suspend fun awaitRequest(client: OkHttpClient, request: Request): Result<String> = suspendCancellableCoroutine { continuation ->
        val call = client.newCall(request)
        continuation.invokeOnCancellation { call.cancel() }
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                continuation.resume(Result.failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        continuation.resume(Result.failure(IOException("HTTP ${it.code}")))
                    } else {
                        continuation.resume(Result.success(it.body.string()))
                    }
                }
            }
        })
    }

    private fun buildArticleRequest(cacheAliveSeconds: Int): Request = Request.Builder()
        .url(articleListUrl())
        .header(ControlHeaders.CACHE_ALIVE_SECONDS, cacheAliveSeconds.toString())
        .get()
        .build()

    private fun articleListUrl(): String = Constants.Url_Article_List.replace("{page}", "0")

    private fun preview(body: String): String {
        val compact = body.replace('\n', ' ').trim()
        return if (compact.length <= PREVIEW_MAX_LENGTH) {
            compact
        } else {
            compact.take(PREVIEW_MAX_LENGTH) + "…"
        }
    }

    private companion object {
        private const val NORMAL_TIMEOUT_SECONDS = 15L
        private const val SHORT_TIMEOUT_SECONDS = 2L
        private const val CACHE_ALIVE_SECONDS = 300
        private const val MAX_RETRY = 3
        private const val INITIAL_BACKOFF_MS = 1_000L
        private const val BACKOFF_MULTIPLIER = 2L
        private const val PREVIEW_MAX_LENGTH = 200
        private const val NETWORK_LOG_KEY = "network"
        private const val TAG_NORMAL = "[Normal]"
        private const val TAG_SHORT_TIMEOUT = "[ShortTimeout]"
        private const val TAG_OFFLINE_DISK = "[Offline:disk]"
    }
}
