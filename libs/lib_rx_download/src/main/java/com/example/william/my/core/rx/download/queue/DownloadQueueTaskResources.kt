package com.example.william.my.core.rx.download.queue

import java.io.File
import java.io.InterruptedIOException

/** 管理单个队列任务从预处理到物理下载结束期间持有的资源。 */
internal class DownloadQueueTaskResources(
    private val terminationTracker: DownloadQueueTerminationTracker,
    private val concurrencyLimiter: DownloadConcurrencyLimiter,
    private val destinationRegistry: DownloadDestinationRegistry,
    private val destination: File,
) {

    private val lock = Any()
    private var state = State.NEW
    private var cancelled = false
    private var permitAcquired = false
    private var destinationLease: DownloadDestinationRegistry.Lease? = null

    /** 获取目标路径租约和并发许可，并将整个预处理阶段纳入终止计数。 */
    @Throws(Exception::class)
    fun acquire() {
        if (!beginPreflight()) throw cancelledException()
        try {
            throwIfCancelled()
            attachDestinationLease(destinationRegistry.acquire(destination))
            throwIfCancelled()

            concurrencyLimiter.acquire()
            attachPermit()

            val ready = synchronized(lock) {
                if (state == State.PREFLIGHT && !cancelled) {
                    state = State.READY
                    true
                } else {
                    false
                }
            }
            if (!ready) throw cancelledException()
        } catch (error: Exception) {
            finish(State.PREFLIGHT)
            throw error
        }
    }

    /** 物理下载即将开始；返回 false 时请求不得继续执行。 */
    fun startOperation(): Boolean {
        return synchronized(lock) {
            if (state != State.READY || cancelled) return@synchronized false
            state = State.OPERATION
            true
        }
    }

    /** 物理下载、响应体和文件流全部退出后释放任务资源。 */
    fun finishOperation() {
        finish(State.OPERATION)
    }

    /** 标记下游已经取消，预处理中的任务由执行线程完成实际清理。 */
    fun cancel() {
        val ready = synchronized(lock) {
            cancelled = true
            state == State.READY
        }
        if (ready) finish(State.READY)
    }

    /** 请求未进入物理下载时，保证已就绪资源不会遗留。 */
    fun finishIfReady() {
        finish(State.READY)
    }

    private fun beginPreflight(): Boolean {
        return synchronized(lock) {
            if (state != State.NEW || cancelled || !terminationTracker.start()) {
                return@synchronized false
            }
            state = State.PREFLIGHT
            true
        }
    }

    private fun attachDestinationLease(lease: DownloadDestinationRegistry.Lease) {
        val attached = synchronized(lock) {
            if (state == State.PREFLIGHT && !cancelled) {
                destinationLease = lease
                true
            } else {
                false
            }
        }
        if (!attached) {
            lease.release()
            throw cancelledException()
        }
    }

    private fun attachPermit() {
        val attached = synchronized(lock) {
            if (state == State.PREFLIGHT && !cancelled) {
                permitAcquired = true
                true
            } else {
                false
            }
        }
        if (!attached) {
            concurrencyLimiter.release()
            throw cancelledException()
        }
    }

    private fun throwIfCancelled() {
        val isCancelled = synchronized(lock) {
            cancelled || state != State.PREFLIGHT
        }
        if (isCancelled || Thread.currentThread().isInterrupted) {
            throw cancelledException()
        }
    }

    private fun finish(expectedState: State) {
        val resources = synchronized(lock) {
            if (state != expectedState) return
            state = State.FINISHED
            Resources(
                releasePermit = permitAcquired.also { permitAcquired = false },
                destinationLease = destinationLease.also { destinationLease = null },
            )
        }
        try {
            if (resources.releasePermit) concurrencyLimiter.release()
            resources.destinationLease?.release()
        } finally {
            terminationTracker.finish()
        }
    }

    private fun cancelledException(): InterruptedIOException {
        return InterruptedIOException("下载队列任务已取消")
    }

    private data class Resources(
        val releasePermit: Boolean,
        val destinationLease: DownloadDestinationRegistry.Lease?,
    )

    private enum class State {
        NEW,
        PREFLIGHT,
        READY,
        OPERATION,
        FINISHED,
    }
}
