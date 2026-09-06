package com.example.william.my.core.netty

import org.slf4j.LoggerFactory

/**
 * Netty 日志工具类（基于 SLF4J）
 */
object NettyLogger {

    private val logger = LoggerFactory.getLogger("Netty")

    fun debug(msg: String) {
        logger.debug(msg)
    }

    fun error(msg: String, t: Throwable? = null) {
        logger.error(msg, t)
    }
}
