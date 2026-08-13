@file:JvmName("KtorDsl")

package com.example.william.my.core.ktor

import com.example.william.my.core.ktor.builder.KtorClientBuilder
import com.example.william.my.core.ktor.client.KtorClient

/**
 * 创建独立的 KtorClient，每次调用都新建实例。
 *
 * ```kotlin
 * val client = ktorClient {
 *     timeout(15)
 *     logging()
 * }
 * ```
 */
fun ktorClient(init: KtorClientBuilder.() -> Unit): KtorClient {
    return KtorClientBuilder().apply(init).build()
}
