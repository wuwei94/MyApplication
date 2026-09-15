package com.example.william.my.module.sse.activity.ktor

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.ktorsse.KtorSseClient
import com.example.william.my.core.ktorsse.KtorSseListener
import com.example.william.my.module.sse.utils.LlmStreamParser

/**
 * Ktor SSE — 原生 Listener 回调版流式客户端
 *
 * 使用 Ktor Client 的 SSE 插件，通过 KtorSseListener 回调建立 SSE 长连接，
 * 直接对接 DeepSeek 官方 POST + SSE 流式对话接口。与 OkHttp 方案对比，
 * Ktor 原生支持 Kotlin 协程与挂起函数。
 *
 * 核心机制与避坑点：
 * 1. Ktor SSE 插件：HttpClient 内置 SSE 支持，无需额外依赖
 * 2. 事件回调：KtorSseListener 回调 onOpen / onEvent / onClosed / onFailure
 * 3. 逐 Token 解析：从 data 帧提取 delta.content，累积为完整回答
 * 4. 会话终止：收到 [DONE] 后主动 cancel，或页面销毁时自动断开
 *
 * https://ktor.io/docs/client-server-sent-events.html
 */
@Route(path = RouterPath.SSE.KtorSseClient)
class KtorSseClientActivity : BasicResponseActivity() {

    private val serverUrl: String = Constants.Url_DeepSeek
    private val responseBuffer = StringBuilder()

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("【Ktor SSE】DeepSeek AI 流式对话 (Listener 回调)\n地址：$serverUrl\n模型：deepseek-chat\n特性：POST Prompt -> 逐 Token 流式响应 -> 收到 [DONE] 完成")
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "发起 DeepSeek 对话（Ktor POST Stream）",
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
        KtorSseClient.cancel(serverUrl)
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
        appendLog("【AI 思考中... 正在建立 Ktor SSE 连接】")
        updateLog("deepseek_response", "【AI 思考中...】")

        val jsonBody = LlmStreamParser.buildChatRequestBody(prompt, "deepseek-chat")
        val headers = mapOf("Authorization" to "Bearer ${Constants.DeepSeek_ApiKey}")

        KtorSseClient.connect(
            urlString = serverUrl,
            jsonBody = jsonBody,
            headers = headers,
            listener = object : KtorSseListener {
                override fun onOpen() {
                    appendLogAccent("【连接】Ktor SSE 连接已建立，开始流式接收 Token...")
                }

                override fun onEvent(id: String?, event: String?, data: String?) {
                    if (data?.trim() == "[DONE]") {
                        removeUpdatingLog("deepseek_response")
                        appendLogAccent("【AI 完整回答】\n$responseBuffer")
                        appendLog("✓ 收到 [DONE] 标志，DeepSeek 模型生成完毕！")
                        KtorSseClient.cancel(serverUrl)
                        return
                    }

                    val delta = LlmStreamParser.parseDeltaContent(data ?: "")
                    if (delta.isNotEmpty()) {
                        responseBuffer.append(delta)
                        updateLog("deepseek_response", "【AI 正在打字...】\n$responseBuffer")
                    }
                }

                override fun onClosed(reason: String) {
                    appendLog("【关闭】Ktor SSE 会话结束，本次对话交互完毕")
                }

                override fun onFailure(t: Throwable) {
                    removeUpdatingLog("deepseek_response")
                    appendLog("✗ ${t.message ?: "未知异常"}")
                }
            },
        )
    }

    private fun cancelStream() {
        KtorSseClient.cancel(serverUrl)
        removeUpdatingLog("deepseek_response")
        appendLog("→ 已主动取消当前大模型流式输出")
    }
}
