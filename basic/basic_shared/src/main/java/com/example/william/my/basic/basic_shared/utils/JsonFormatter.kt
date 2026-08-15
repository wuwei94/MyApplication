package com.example.william.my.basic.basic_shared.utils

import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import com.google.gson.JsonParser

/** JSON 文本格式化工具。 */
object JsonFormatter {

    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    /**
     * 格式化 JSON 对象或数组；非 JSON 以及解析失败的内容保持原样。
     */
    fun format(value: String): String {
        val content = value.trim()
        if (!content.startsWith("{") && !content.startsWith("[")) {
            return value
        }
        return try {
            gson.toJson(JsonParser.parseString(content))
        } catch (_: JsonParseException) {
            value
        }
    }
}
