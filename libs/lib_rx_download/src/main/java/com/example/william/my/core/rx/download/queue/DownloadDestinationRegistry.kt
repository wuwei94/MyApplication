package com.example.william.my.core.rx.download.queue

import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/** 防止同一个 Manager 下的并发任务写入相同目标文件。 */
internal class DownloadDestinationRegistry {
    private val activeDestinations = ConcurrentHashMap.newKeySet<String>()

    fun acquire(destination: File): Lease {
        val canonicalPath = destination.canonicalPath
        check(activeDestinations.add(canonicalPath)) {
            "下载目标正在被同一 Manager 的其他任务使用：$canonicalPath"
        }
        return Lease {
            activeDestinations.remove(canonicalPath)
        }
    }

    class Lease internal constructor(
        private val releaseAction: () -> Unit,
    ) {
        private val released = AtomicBoolean(false)

        fun release() {
            if (released.compareAndSet(false, true)) {
                releaseAction()
            }
        }
    }
}
