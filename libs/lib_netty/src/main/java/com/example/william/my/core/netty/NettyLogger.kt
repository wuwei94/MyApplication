package com.example.william.my.core.netty

import org.slf4j.LoggerFactory

object NettyLogger {

    private val logger = LoggerFactory.getLogger("Netty")

    fun debug(msg: String) {
        logger.debug(msg)
    }

    fun error(msg: String, t: Throwable? = null) {
        logger.error(msg, t)
    }
}
