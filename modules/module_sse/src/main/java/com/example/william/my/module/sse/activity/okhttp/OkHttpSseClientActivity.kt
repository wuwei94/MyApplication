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
 * OkHttp SSE 客户端示例（普通回调版本 - DeepSeek 大模型流式对话）
 *
 * 演示使用 OkHttpSseClient 发起 POST 请求直接连接 DeepSeek 官方流式接口。
 */
@Route(path = RouterPath.SSE.OkHttpSseClient)
class OkHttpSseClientActivity : BasicResponseActivity() {

    private val serverUrl: String = Constants.Url_DeepSeek
    private val responseBuffer = StringBuilder()

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("【OkHttp SSE】DeepSeek AI 流式对话 (Listener 回调)\n地址：$serverUrl\n模型：deepseek-chat\n特性：POST Prompt -> 逐 Token 流式响应 -> 收到 [DONE] 完成")
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "发起 DeepSeek 对话（POST Stream）",
        "中断当前生成（Cancel Stream）",
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
            appendLog("【提示】未配置 DeepSeek API Key！")
            appendLog("👉 请在工程根目录 local.properties 中配置：deepseek.api.key=sk-xxxx 后重新编译。")
            return
        }

        responseBuffer.clear()
        appendLog("----------------------------------------")
        appendLog("【DeepSeek 目标】$serverUrl")
        appendLog("【用户提问】$prompt")
        appendLog("【AI 思考中... 正在建立 SSE 流式连接】")
        updateLog("deepseek_response", "【AI 思考中...】")

        val jsonBody = LlmStreamParser.buildChatRequestBody(prompt, "deepseek-chat")
        val headers = mapOf("Authorization" to "Bearer ${Constants.DeepSeek_ApiKey}")

        OkHttpSseClient.connect(
            url = serverUrl,
            jsonBody = jsonBody,
            headers = headers,
            listener = object : OkHttpSseListener() {
                override fun onOpen(eventSource: EventSource, response: Response) {
                    appendLogAccent("【连接】DeepSeek SSE 连接成功 (HTTP ${response.code})，开始流式接收 Token...")
                }

                override fun onEvent(
                    eventSource: EventSource,
                    id: String?,
                    type: String?,
                    data: String,
                ) {
                    if (data.trim() == "[DONE]") {
                        removeUpdatingLog("deepseek_response")
                        appendLogAccent("【AI 完整回答】\n$responseBuffer")
                        appendLog("【完成】收到 [DONE] 标志，DeepSeek 模型生成完毕！")
                        OkHttpSseClient.cancel(serverUrl)
                        return
                    }

                    val delta = LlmStreamParser.parseDeltaContent(data)
                    if (delta.isNotEmpty()) {
                        responseBuffer.append(delta)
                        updateLog("deepseek_response", "【AI 正在打字...】\n$responseBuffer")
                    }
                }

                override fun onClosed(eventSource: EventSource) {
                    appendLog("【关闭】SSE 会话结束，本次对话交互完毕")
                }

                override fun onFailure(
                    eventSource: EventSource,
                    t: Throwable?,
                    response: Response?,
                ) {
                    removeUpdatingLog("deepseek_response")
                    appendLog("【错误】${t?.message ?: "HTTP ${response?.code}"}")
                }
            },
        )
    }

    private fun cancelStream() {
        OkHttpSseClient.cancel(serverUrl)
        removeUpdatingLog("deepseek_response")
        appendLog("【中断】已主动取消当前大模型流式输出")
    }
}
