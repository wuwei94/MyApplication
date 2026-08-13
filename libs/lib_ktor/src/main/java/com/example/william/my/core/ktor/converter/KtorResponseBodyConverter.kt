package com.example.william.my.core.ktor.converter

import com.example.william.my.core.ktor.client.KtorClient
import com.example.william.my.core.ktor.response.KtorResponse
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import io.ktor.util.AttributeKey

@PublishedApi
internal const val DEFAULT_CODE_FIELD = "errorCode"

@PublishedApi
internal const val DEFAULT_MESSAGE_FIELD = "errorMsg"

@PublishedApi
internal const val DATA_FIELD = "data"

@PublishedApi
internal val KtorResponseCodeFieldKey = AttributeKey<String>("KtorResponseCodeField")

@PublishedApi
internal val KtorResponseMessageFieldKey = AttributeKey<String>("KtorResponseMessageField")

@PublishedApi
internal val KtorResponseGsonKey = AttributeKey<Gson>("KtorResponseGson")

@PublishedApi
internal inline fun <reified T> KtorClient.convertKtorResponseBody(body: String): KtorResponse<T> {
    val codeField = attributes.getOrNull(KtorResponseCodeFieldKey) ?: DEFAULT_CODE_FIELD
    val messageField = attributes.getOrNull(KtorResponseMessageFieldKey) ?: DEFAULT_MESSAGE_FIELD
    val gson = attributes.getOrNull(KtorResponseGsonKey) ?: Gson()
    if (body.isBlank()) {
        @Suppress("UNCHECKED_CAST")
        val data = if (T::class == Unit::class) Unit as T else null
        return KtorResponse.of(KtorResponse.SUCCESS, "", data)
    }
    val root = JsonParser.parseString(body)

    if (!root.isJsonObject || !root.asJsonObject.has(codeField)) {
        return KtorResponse.of(
            KtorResponse.SUCCESS,
            "",
            gson.fromJson(root, object : TypeToken<T>() {}.type)
        )
    }

    val json = root.asJsonObject
    val codeElement = json.get(codeField)
    val code = runCatching { codeElement.asInt }.getOrElse {
        throw JsonParseException("$codeField must be a number", it)
    }
    val message = json.stringOrEmpty(messageField)
    val data = json.get(DATA_FIELD)
        ?.takeUnless(JsonElement::isJsonNull)
        ?.let { gson.fromJson<T>(it, object : TypeToken<T>() {}.type) }

    return KtorResponse.of(code, message, data)
}

@PublishedApi
internal fun com.google.gson.JsonObject.stringOrEmpty(key: String): String {
    val value = get(key) ?: return ""
    return if (value.isJsonNull) "" else runCatching { value.asString }.getOrDefault("")
}
