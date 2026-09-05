package com.example.william.my.module.sse.activity.ktor

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.ktorsse.KtorSseClientFlow
import com.example.william.my.core.ktorsse.KtorSseInfo
import com.example.william.my.module.sse.utils.LlmStreamParser
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Ktor SSE 客户端示例（Kotlin Coroutines Flow 封装版本 - DeepSeek 大模型流式对话）
 *
 * 演示使用 Ktor Client 内置 SSE 插件发起 POST 请求直接连接 DeepSeek 官方流式接口。
 */
@Route(path = RouterPath.SSE.KtorSseClientFlow)
class KtorSseClientFlowActivity : BasicResponseActivity() {

    private val serverUrl: String = Constants.Url_DeepSeek
    private val responseBuffer = StringBuilder()
    private var streamJob: Job? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("【Ktor SSE】DeepSeek AI 流式对话 (Ktor Client + Flow 封装)\n地址：$serverUrl\n模型：deepseek-chat\n特性：Ktor 原生 Flow 收集 -> POST JSON Payload -> [DONE] 完成")
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "发起 DeepSeek 对话（Ktor POST Flow）",
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
        appendLog("【AI 思考中... 正在使用 Ktor 发起流式 POST 请求】")
        updateLog("deepseek_response", "【AI 思考中...】")

        val jsonBody = LlmStreamParser.buildChatRequestBody(prompt, "deepseek-chat")
        val headers = mapOf("Authorization" to "Bearer ${Constants.DeepSeek_ApiKey}")

        streamJob = lifecycleScope.launch {
            KtorSseClientFlow
                .createEventSource(urlString = serverUrl, jsonBody = jsonBody, headers = headers)
                .collect { info ->
                    when (info) {
                        is KtorSseInfo.Open -> {
                            appendLogAccent("【连接】Ktor SSE 流连接成功，开始接收 DeepSeek 响应...")
                        }
                        is KtorSseInfo.Event -> {
                            if (info.data?.trim() == "[DONE]") {
                                removeUpdatingLog("deepseek_response")
                                appendLogAccent("【AI 完整回答】\n$responseBuffer")
                                appendLog("【完成】收到 [DONE] 标识，DeepSeek 流式响应生成完毕")
                                streamJob?.cancel()
                                return@collect
                            }
                            val delta = LlmStreamParser.parseDeltaContent(info.data ?: "")
                            if (delta.isNotEmpty()) {
                                responseBuffer.append(delta)
                                updateLog("deepseek_response", "【AI 正在打字...】\n$responseBuffer")
                            }
                        }
                        is KtorSseInfo.Closed -> {
                            appendLog("【关闭】Ktor SSE 连接已断开")
                        }
                        is KtorSseInfo.Error -> {
                            removeUpdatingLog("deepseek_response")
                            appendLog("【异常】${info.throwable.message}")
                        }
                    }
                }
            appendLog("【Flow 结束】本次 DeepSeek 流式对话收集完毕")
        }
    }

    private fun cancelStream() {
        streamJob?.cancel()
        removeUpdatingLog("deepseek_response")
        appendLog("【中断】已 Cancel 协程 Job，断开 Ktor SSE 流")
    }
}
