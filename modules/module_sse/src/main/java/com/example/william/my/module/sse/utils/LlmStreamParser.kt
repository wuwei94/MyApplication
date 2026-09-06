package com.example.william.my.module.sse.utils

import org.json.JSONObject

/**
 * LLM 流解析器
 *
 * 解析 LLM 流式接口返回的 SSE 数据。
 */
object LlmStreamParser {

    const val DEFAULT_PROMPT = "请用一句话介绍你自己和你的核心优势"

    /**
     * 构造标准 OpenAI / DeepSeek 格式的 POST 请求体
     */
    fun buildChatRequestBody(prompt: String, model: String = "deepseek-chat"): String {
        val json = JSONObject()
        json.put("model", model)
        json.put("stream", true)
        val messages = org.json.JSONArray()
        val userMsg = JSONObject()
        userMsg.put("role", "user")
        userMsg.put("content", prompt)
        messages.put(userMsg)
        json.put("messages", messages)
        return json.toString()
    }

    /**
     * 从 SSE 原始 data 中解析 delta.content 文本（同时兼容 DeepSeek R1 的 reasoning_content 思考过程）
     */
    fun parseDeltaContent(data: String): String {
        if (data.trim() == "[DONE]") {
            return ""
        }
        return try {
            val json = JSONObject(data)
            val choices = json.optJSONArray("choices") ?: return ""
            if (choices.length() > 0) {
                val first = choices.getJSONObject(0)
                val delta = first.optJSONObject("delta") ?: return ""
                val content = delta.optString("content", "")
                val reasoning = delta.optString("reasoning_content", "")
                when {
                    reasoning.isNotEmpty() -> "[思考] $reasoning"
                    content.isNotEmpty() -> content
                    else -> ""
                }
            } else {
                ""
            }
        } catch (_: Exception) {
            data
        }
    }
}
