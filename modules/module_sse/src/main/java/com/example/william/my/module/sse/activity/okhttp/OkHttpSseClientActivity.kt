package com.example.william.my.module.sse.activity.okhttp

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.okttpsse.client.OkHttpSseClient
import com.example.william.my.core.okttpsse.client.OkHttpSseListener
import com.example.william.my.module.sse.utils.LlmStreamParser
import okhttp3.Response
import okhttp3.sse.EventSource

/**
 * OkHttp SSE — 原生 EventSource 回调版流式客户端
 *
 * 使用 OkHttp 内置的 okhttp3.sse 模块，通过 EventSource 建立 SSE 长连接，
 * 直接对接 DeepSeek 官方 POST + SSE 流式对话接口，逐 Token 接收大模型响应。
 *
 * 核心机制与避坑点：
 * 1. POST 流式请求：EventSource 发起带 JSON Body 的 POST，服务端以 text/event-stream 推送
 * 2. 事件回调：OkHttpSseListener 回调 onOpen / onEvent / onClosed / onFailure 四段生命周期
 * 3. 逐 Token 解析：从 data 帧中提取 delta.content，累积为完整回答
 * 4. 会话终止：收到 [DONE] 标志后主动 cancel，或页面销毁时自动断开
 *
 * 官方参考：
 * https://square.github.io/okhttp/features/sse/
 */
@Route(path = RouterPath.SSE.OkHttpSseClient)
class OkHttpSseClientActivity : BasicResponseActivity() {

    private val serverUrl: String = Constants.Url_DeepSeek
    private val responseBuffer = StringBuilder()

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("[OkHttp SSE]DeepSeek AI 流式对话 (Listener 回调)\n地址：$serverUrl\n模型：deepseek-chat\n特性：POST Prompt -> 逐 Token 流式响应 -> 收到 [DONE] 完成\n覆盖上行 Prompt / 下行 Token 流监听 / 关闭注销")
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 发起 DeepSeek 对话（POST Stream + 下行监听）",
        "2. 中断当前生成（Cancel Stream + 注销下行）",
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
        OkHttpSseClient.cancel(serverUrl)
    }

    private fun sendDeepSeekPrompt(prompt: String) {
        if (Constants.DeepSeek_ApiKey.isBlank()) {
            appendLog("----------------------------------------")
            appendLog("[提示]未配置 DeepSeek API Key！")
            appendLog("👉 请在工程根目录 local.properties 中配置：deepseek.api.key=sk-xxxx 后重新编译。")
            return
        }

        responseBuffer.clear()
        appendLog("----------------------------------------")
        appendLog("[DeepSeek 目标]$serverUrl")
        appendLog("[用户提问]$prompt")
        appendLog("[AI 思考中... 正在建立 SSE 流式连接]")
        updateLog("deepseek_response", "[AI 思考中...]")

        val jsonBody = LlmStreamParser.buildChatRequestBody(prompt, "deepseek-chat")
        val headers = mapOf("Authorization" to "Bearer ${Constants.DeepSeek_ApiKey}")

        OkHttpSseClient.connect(
            url = serverUrl,
            jsonBody = jsonBody,
            headers = headers,
            listener = object : OkHttpSseListener() {
                override fun onOpen(eventSource: EventSource, response: Response) {
                    appendLogAccent("[连接]DeepSeek SSE 连接成功 (HTTP ${response.code})，开始流式接收 Token...")
                }

                override fun onEvent(
                    eventSource: EventSource,
                    id: String?,
                    type: String?,
                    data: String,
                ) {
                    if (data.trim() == "[DONE]") {
                        removeUpdatingLog("deepseek_response")
                        appendLogAccent("[AI 完整回答]\n$responseBuffer")
                        appendLog("✓ 收到 [DONE] 标志，DeepSeek 模型生成完毕！")
                        OkHttpSseClient.cancel(serverUrl)
                        return
                    }

                    val delta = LlmStreamParser.parseDeltaContent(data)
                    if (delta.isNotEmpty()) {
                        responseBuffer.append(delta)
                        updateLog("deepseek_response", "[AI 正在打字...]\n$responseBuffer")
                    }
                }

                override fun onClosed(eventSource: EventSource) {
                    appendLog("[关闭]SSE 会话结束，本次对话交互完毕")
                }

                override fun onFailure(
                    eventSource: EventSource,
                    t: Throwable?,
                    response: Response?,
                ) {
                    removeUpdatingLog("deepseek_response")
                    appendLog("✗ ${t?.message ?: "HTTP ${response?.code}"}")
                }
            },
        )
    }

    private fun cancelStream() {
        OkHttpSseClient.cancel(serverUrl)
        removeUpdatingLog("deepseek_response")
        appendLog("→ 已主动取消当前大模型流式输出")
    }
}
