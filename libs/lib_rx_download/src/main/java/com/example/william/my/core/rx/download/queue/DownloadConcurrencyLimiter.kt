package com.example.william.my.core.rx.download.queue

import java.util.concurrent.Semaphore

/** 同一个下载 Manager 下所有队列共享的并发额度。 */
internal class DownloadConcurrencyLimiter(maxConcurrency: Int) {
    private val semaphore = Semaphore(maxConcurrency, true)

    @Throws(InterruptedException::class)
    fun acquire() {
        semaphore.acquire()
    }

    fun release() {
        semaphore.release()
    }
}
