package com.example.william.my.core.okhttp.utils

import com.google.gson.Gson

/** JSON 转换工具。 */
object JsonUtils {
    private val gson = Gson()

    /** 将对象转换为 JSON 字符串。 */
    fun toJson(value: Any): String {
        return gson.toJson(value)
    }
}
