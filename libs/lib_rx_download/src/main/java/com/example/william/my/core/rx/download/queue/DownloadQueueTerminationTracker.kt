package com.example.william.my.core.rx.download.queue

/** 等待队列中已启动的底层下载操作全部退出。 */
internal class DownloadQueueTerminationTracker(
    private val onFinally: (() -> Unit)?,
) {

    private var activeCount = 0
    private var closed = false
    private var notified = false

    @Synchronized
    fun start(): Boolean {
        if (closed) return false
        activeCount++
        return true
    }

    fun finish() {
        val action = synchronized(this) {
            check(activeCount > 0) { "下载队列终止计数不匹配" }
            activeCount--
            takeFinallyAction()
        }
        runCatching { action?.invoke() }
    }

    fun close() {
        val action = synchronized(this) {
            closed = true
            takeFinallyAction()
        }
        runCatching { action?.invoke() }
    }

    private fun takeFinallyAction(): (() -> Unit)? {
        if (!closed || activeCount != 0 || notified) return null
        notified = true
        return onFinally
    }
}
