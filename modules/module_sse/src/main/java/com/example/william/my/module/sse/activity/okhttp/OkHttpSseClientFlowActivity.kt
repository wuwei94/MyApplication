package com.example.william.my.module.sse.activity.okhttp

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.okttpsse.OkHttpSseInfo
import com.example.william.my.core.okttpsse.client.OkHttpSseClientFlow
import com.example.william.my.module.sse.utils.LlmStreamParser
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * OkHttp SSE 客户端示例（Kotlin Coroutines Flow 封装版本 - DeepSeek 大模型流式对话）
 *
 * 演示使用 OkHttpSseClientFlow 将 DeepSeek 大模型 POST 流式响应转换为 Coroutines Flow。
 * 在生命周期感知协程域中直接 collect 流式 Token，收到 [DONE] 时 Flow 自动结束。
 */
@Route(path = RouterPath.SSE.OkHttpSseClientFlow)
class OkHttpSseClientFlowActivity : BasicResponseActivity() {

    private val serverUrl: String = Constants.Url_DeepSeek
    private val responseBuffer = StringBuilder()
    private var streamJob: Job? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("【OkHttp SSE】DeepSeek AI 流式对话 (Coroutines Flow 封装)\n地址：$serverUrl\n模型：deepseek-chat\n特性：Flow 响应式收集 -> 逐字流式打字机 -> 协程生命周期感知自动释放")
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "发起 DeepSeek 对话（POST Coroutines Flow）",
        "中断当前生成（Cancel Job）",
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
        streamJob?.cancel()
    }

    private fun sendDeepSeekPrompt(prompt: String) {
        if (Constants.DeepSeek_ApiKey.isBlank()) {
            appendLog("----------------------------------------")
            appendLog("【提示】未配置 DeepSeek API Key！")
            appendLog("👉 请在工程根目录 local.properties 中配置：deepseek.api.key=sk-xxxx 后重新编译。")
            return
        }

        streamJob?.cancel()
        responseBuffer.clear()
        appendLog("----------------------------------------")
        appendLog("【DeepSeek 目标】$serverUrl")
        appendLog("【用户提问】$prompt")
        appendLog("【AI 思考中... 正在启动协程收集 Flow】")
        updateLog("deepseek_response", "【AI 思考中...】")

        val jsonBody = LlmStreamParser.buildChatRequestBody(prompt, "deepseek-chat")
        val headers = mapOf("Authorization" to "Bearer ${Constants.DeepSeek_ApiKey}")

        streamJob = lifecycleScope.launch {
            OkHttpSseClientFlow
                .createEventSource(url = serverUrl, jsonBody = jsonBody, headers = headers)
                .collect { info ->
                    when (info) {
                        is OkHttpSseInfo.Open -> {
                            appendLogAccent("【连接】DeepSeek Flow SSE 连接成功 (HTTP ${info.response.code})")
                        }
                        is OkHttpSseInfo.Event -> {
                            if (info.data.trim() == "[DONE]") {
                                removeUpdatingLog("deepseek_response")
                                appendLogAccent("【AI 完整回答】\n$responseBuffer")
                                appendLog("【完成】收到 [DONE] 标识，DeepSeek 流式响应自然完结")
                                streamJob?.cancel()
                                return@collect
                            }
                            val delta = LlmStreamParser.parseDeltaContent(info.data)
                            if (delta.isNotEmpty()) {
                                responseBuffer.append(delta)
                                updateLog("deepseek_response", "【AI 正在打字...】\n$responseBuffer")
                            }
                        }
                        is OkHttpSseInfo.Closed -> {
                            appendLog("【关闭】Flow 数据流通道已关闭")
                        }
                        is OkHttpSseInfo.Error -> {
                            removeUpdatingLog("deepseek_response")
                            appendLog("【异常】${info.throwable.message}")
                        }
                    }
                }
            appendLog("【Flow 结束】本次 DeepSeek 对话 Flow 收集完毕")
        }
    }

    private fun cancelStream() {
        streamJob?.cancel()
        removeUpdatingLog("deepseek_response")
        appendLog("【中断】已 Cancel 协程 Job，自动断开底层 EventSource")
    }
}
