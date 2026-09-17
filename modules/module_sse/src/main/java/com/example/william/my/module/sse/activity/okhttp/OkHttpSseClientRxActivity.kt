package com.example.william.my.module.sse.activity.okhttp

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.okttpsse.OkHttpSseInfo
import com.example.william.my.core.okttpsse.client.OkHttpSseClientRx
import com.example.william.my.module.sse.utils.LlmStreamParser
import io.reactivex.rxjava3.disposables.Disposable

/**
 * OkHttp SSE — RxJava Observable 封装版流式客户端
 *
 * 将 OkHttp SSE 的事件流封装为 RxJava3 Observable，通过 subscribe 订阅响应式数据，
 * 对接 DeepSeek 官方 POST + SSE 流式对话接口。与原生回调版相比，获得操作符组合能力。
 *
 * 核心机制与避坑点：
 * 1. Observable 封装：OkHttpSseInfo 密封类（Open / Event / Closed / Error）映射 SSE 事件
 * 2. 响应式订阅：subscribe 三参形式处理 onNext / onError / onComplete
 * 3. 生命周期管理：Disposable 控制订阅，页面销毁时自动 dispose 防泄漏
 * 4. 逐 Token 解析：从 Event.data 提取 delta.content，[DONE] 触发 dispose 终止
 *
 * 官方参考：
 * https://square.github.io/okhttp/features/sse/
 */
@Route(path = RouterPath.SSE.OkHttpSseClientRx)
class OkHttpSseClientRxActivity : BasicResponseActivity() {

    private val serverUrl: String = Constants.Url_DeepSeek
    private val responseBuffer = StringBuilder()
    private var streamDisposable: Disposable? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("[OkHttp SSE]DeepSeek AI 流式对话 (RxJava 响应式流)\n地址：$serverUrl\n模型：deepseek-chat\n特性：Observable 订阅 -> 逐 Token 上屏 -> onComplete() 自动完结\n覆盖上行 Prompt / 下行 Token 流监听 / Dispose 注销")
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 发起 DeepSeek 对话（POST Observable + 下行监听）",
        "2. 中断当前生成（Dispose Stream + 注销下行）",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> sendDeepSeekPrompt(LlmStreamParser.DEFAULT_PROMPT)
            1 -> cancelStream()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        streamDisposable?.dispose()
    }

    private fun sendDeepSeekPrompt(prompt: String) {
        if (Constants.DeepSeek_ApiKey.isBlank()) {
            appendLog("----------------------------------------")
            appendLog("[提示]未配置 DeepSeek API Key！")
            appendLog("👉 请在工程根目录 local.properties 中配置：deepseek.api.key=sk-xxxx 后重新编译。")
            return
        }

        streamDisposable?.dispose()
        responseBuffer.clear()
        appendLog("----------------------------------------")
        appendLog("[DeepSeek 目标]$serverUrl")
        appendLog("[用户提问]$prompt")
        appendLog("[AI 思考中... 正在建立 Rx 响应式流]")
        updateLog("deepseek_response", "[AI 思考中...]")

        val jsonBody = LlmStreamParser.buildChatRequestBody(prompt, "deepseek-chat")
        val headers = mapOf("Authorization" to "Bearer ${Constants.DeepSeek_ApiKey}")

        streamDisposable = OkHttpSseClientRx
            .createEventSource(url = serverUrl, jsonBody = jsonBody, headers = headers)
            .subscribe(
                { info ->
                    when (info) {
                        is OkHttpSseInfo.Open -> {
                            appendLogAccent("[连接]Rx SSE 流已连接 (HTTP ${info.response.code})，开始流式接收...")
                        }
                        is OkHttpSseInfo.Event -> {
                            if (info.data.trim() == "[DONE]") {
                                removeUpdatingLog("deepseek_response")
                                appendLogAccent("[AI 完整回答]\n$responseBuffer")
                                appendLog("✓ 收到 [DONE]，DeepSeek 生成完毕，Rx 流正常完结")
                                streamDisposable?.dispose()
                                return@subscribe
                            }
                            val delta = LlmStreamParser.parseDeltaContent(info.data)
                            if (delta.isNotEmpty()) {
                                responseBuffer.append(delta)
                                updateLog("deepseek_response", "[AI 正在打字...]\n$responseBuffer")
                            }
                        }
                        is OkHttpSseInfo.Closed -> {
                            appendLog("[关闭]Rx SSE 数据源已结束")
                        }
                        is OkHttpSseInfo.Error -> {
                            removeUpdatingLog("deepseek_response")
                            appendLog("✗ ${info.throwable.message}")
                        }
                    }
                },
                { error ->
                    removeUpdatingLog("deepseek_response")
                    appendLog("[Rx 错误]${error.message}")
                },
                {
                    appendLog("[Rx onComplete]本次 DeepSeek 流式会话已成功完结")
                },
            )
    }

    private fun cancelStream() {
        streamDisposable?.dispose()
        removeUpdatingLog("deepseek_response")
        appendLog("→ 已 Dispose 取消当前 Rx 数据流")
    }
}
