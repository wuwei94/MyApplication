package com.example.william.my.core.rx.download

import com.example.william.my.core.rx.download.builder.RxDownloadBuilder
import com.example.william.my.core.rx.download.queue.RxDownloadQueueBuilder

/** Rx 文件下载入口。 */
object RxDownload {

    private val defaultManager by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        RxDownloadManager.builder().build()
    }

    @JvmStatic
    fun builder(): RxDownloadBuilder = RxDownloadBuilder()

    /** 使用默认共享 Rx Retrofit 和 3 个并发创建批量下载队列。 */
    @JvmStatic
    fun queue(): RxDownloadQueueBuilder = defaultManager.download()
}
