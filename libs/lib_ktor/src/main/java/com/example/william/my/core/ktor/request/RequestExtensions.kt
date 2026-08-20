package com.example.william.my.core.ktor.request

import com.example.william.my.core.ktor.client.KtorClient
import com.example.william.my.core.ktor.converter.convertKtorResponseBody
import com.example.william.my.core.ktor.exception.ExceptionHandler
import com.example.william.my.core.ktor.response.KtorResponse
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.parameters

suspend inline fun <reified T> KtorClient.getResult(
    url: String,
    params: Map<String, String> = emptyMap(),
    headers: Map<String, String> = emptyMap()
): Result<T> = resultOf {
    get(url) {
        url { params.forEach { (key, value) -> parameters.append(key, value) } }
        appendHeaders(headers)
    }.body<T>()
}

/** GET 业务响应，支持统一信封及无信封响应自动包装。 */
suspend inline fun <reified T> KtorClient.getResponse(
    url: String,
    params: Map<String, String> = emptyMap(),
    headers: Map<String, String> = emptyMap()
): Result<KtorResponse<T>> = resultOf {
    get(url) {
        url { params.forEach { (key, value) -> parameters.append(key, value) } }
        appendHeaders(headers)
    }.bodyAsText().let(::convertKtorResponseBody)
}

suspend inline fun <reified T> KtorClient.postFormResult(
    url: String,
    params: Map<String, String> = emptyMap(),
    headers: Map<String, String> = emptyMap()
): Result<T> = formRequest(url, HttpMethod.Post, params, headers)

/** POST Form 业务响应。 */
suspend inline fun <reified T> KtorClient.postFormResponse(
    url: String,
    params: Map<String, String> = emptyMap(),
    headers: Map<String, String> = emptyMap()
): Result<KtorResponse<T>> = formResponseRequest(url, HttpMethod.Post, params, headers)

suspend inline fun <reified T> KtorClient.putFormResult(
    url: String,
    params: Map<String, String> = emptyMap(),
    headers: Map<String, String> = emptyMap()
): Result<T> = formRequest(url, HttpMethod.Put, params, headers)

/** PUT Form 业务响应。 */
suspend inline fun <reified T> KtorClient.putFormResponse(
    url: String,
    params: Map<String, String> = emptyMap(),
    headers: Map<String, String> = emptyMap()
): Result<KtorResponse<T>> = formResponseRequest(url, HttpMethod.Put, params, headers)

suspend inline fun <reified T> KtorClient.patchFormResult(
    url: String,
    params: Map<String, String> = emptyMap(),
    headers: Map<String, String> = emptyMap()
): Result<T> = formRequest(url, HttpMethod.Patch, params, headers)

/** PATCH Form 业务响应。 */
suspend inline fun <reified T> KtorClient.patchFormResponse(
    url: String,
    params: Map<String, String> = emptyMap(),
    headers: Map<String, String> = emptyMap()
): Result<KtorResponse<T>> = formResponseRequest(url, HttpMethod.Patch, params, headers)

suspend inline fun <reified T> KtorClient.deleteFormResult(
    url: String,
    params: Map<String, String> = emptyMap(),
    headers: Map<String, String> = emptyMap()
): Result<T> = formRequest(url, HttpMethod.Delete, params, headers)

/** DELETE Form 业务响应。 */
suspend inline fun <reified T> KtorClient.deleteFormResponse(
    url: String,
    params: Map<String, String> = emptyMap(),
    headers: Map<String, String> = emptyMap()
): Result<KtorResponse<T>> = formResponseRequest(url, HttpMethod.Delete, params, headers)

suspend inline fun <reified T> KtorClient.requestBodyResult(
    url: String,
    method: HttpMethod,
    body: Any,
    mediaType: ContentType? = null,
    headers: Map<String, String> = emptyMap()
): Result<T> = resultOf {
    request(url) {
        this.method = method
        mediaType?.let(::contentType)
        appendHeaders(headers)
        setBody(body)
    }.body<T>()
}

/** JSON、Raw 或 ContentNegotiation Body 的业务响应。 */
suspend inline fun <reified T> KtorClient.requestBodyResponse(
    url: String,
    method: HttpMethod,
    body: Any,
    mediaType: ContentType? = null,
    headers: Map<String, String> = emptyMap()
): Result<KtorResponse<T>> = resultOf {
    request(url) {
        this.method = method
        mediaType?.let(::contentType)
        appendHeaders(headers)
        setBody(body)
    }.bodyAsText().let(::convertKtorResponseBody)
}

@PublishedApi
internal suspend inline fun <reified T> KtorClient.formRequest(
    url: String,
    method: HttpMethod,
    params: Map<String, String>,
    headers: Map<String, String>
): Result<T> = resultOf {
    when (method) {
        HttpMethod.Post -> post(url) {
            appendHeaders(headers)
            setBody(formBody(params))
        }
        HttpMethod.Put -> put(url) {
            appendHeaders(headers)
            setBody(formBody(params))
        }
        HttpMethod.Patch -> patch(url) {
            appendHeaders(headers)
            setBody(formBody(params))
        }
        HttpMethod.Delete -> delete(url) {
            appendHeaders(headers)
            setBody(formBody(params))
        }
        else -> error("Unsupported form method: $method")
    }.body<T>()
}

@PublishedApi
internal suspend inline fun <reified T> KtorClient.formResponseRequest(
    url: String,
    method: HttpMethod,
    params: Map<String, String>,
    headers: Map<String, String>
): Result<KtorResponse<T>> = resultOf {
    request(url) {
        this.method = method
        appendHeaders(headers)
        setBody(formBody(params))
    }.bodyAsText().let(::convertKtorResponseBody)
}

@PublishedApi
internal fun formBody(params: Map<String, String>): FormDataContent {
    return FormDataContent(parameters {
        params.forEach { (key, value) -> append(key, value) }
    })
}

@PublishedApi
internal fun io.ktor.client.request.HttpRequestBuilder.appendHeaders(
    values: Map<String, String>
) {
    values.forEach { (key, value) -> header(key, value) }
}

@PublishedApi
internal suspend fun <T> resultOf(block: suspend () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (error: Exception) {
        Result.failure(ExceptionHandler.handleException(error))
    }
}
