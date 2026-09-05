package com.example.william.my.core.rx.download.queue

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.example.william.my.core.rx.download.queue.model.DownloadQueueEvent
import com.example.william.my.core.rx.download.queue.model.DownloadQueueTask
import com.trello.lifecycle4.android.lifecycle.AndroidLifecycle
import com.trello.rxlifecycle4.LifecycleProvider
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.Retrofit
import java.io.File

/** Rx 批量下载队列构建器。 */
class RxDownloadQueueBuilder internal constructor(
    private val retrofit: Retrofit,
    private val managerMaxConcurrency: Int,
    private val concurrencyLimiter: DownloadConcurrencyLimiter,
    private val destinationRegistry: DownloadDestinationRegistry,
) {

    private val tasks = mutableListOf<DownloadQueueTask>()
    private var maxConcurrency = managerMaxConcurrency
    private var lifecycle: LifecycleProvider<Lifecycle.Event>? = null
    private var subscribeScheduler: Scheduler = Schedulers.io()
    private var observeScheduler: Scheduler? = null
    private var progressIntervalMillis = DEFAULT_PROGRESS_INTERVAL_MILLIS
    private var onFinally: (() -> Unit)? = null

    fun addTask(task: DownloadQueueTask) = apply { tasks += task }

    fun addTask(
        api: String,
        destination: File,
        id: String = destination.absolutePath,
        headers: Map<String, String> = emptyMap(),
        resume: Boolean = true,
    ) = addTask(
        DownloadQueueTask(
            url = api,
            destination = destination,
            id = id,
            headers = headers,
            resume = resume,
        ),
    )

    fun addTasks(tasks: Iterable<DownloadQueueTask>) = apply {
        this.tasks += tasks
    }

    fun maxConcurrency(maxConcurrency: Int) = apply {
        require(maxConcurrency in 1..managerMaxConcurrency) {
            "单队列并发数必须在 1 到 Manager 上限 $managerMaxConcurrency 之间"
        }
        this.maxConcurrency = maxConcurrency
    }

    fun setProvider(owner: LifecycleOwner) = apply {
        lifecycle = AndroidLifecycle.createLifecycleProvider(owner)
    }

    fun subscribeOn(scheduler: Scheduler) = apply { subscribeScheduler = scheduler }

    fun observeOn(scheduler: Scheduler) = apply { observeScheduler = scheduler }

    fun progressIntervalMillis(intervalMillis: Long) = apply {
        require(intervalMillis >= MIN_PROGRESS_INTERVAL_MILLIS) {
            "批量下载进度间隔不能小于 $MIN_PROGRESS_INTERVAL_MILLIS ms"
        }
        progressIntervalMillis = intervalMillis
    }

    /** 队列中已启动的底层下载操作全部退出后调用。 */
    fun onFinally(action: () -> Unit) = apply { onFinally = action }

    fun build(): RxDownloadQueue = RxDownloadQueue(buildConfig())

    fun buildFlowable(): Flowable<DownloadQueueEvent> = build().asFlowable()

    internal fun buildConfig(): DownloadQueueConfig {
        require(tasks.isNotEmpty()) { "至少需要通过 addTask(...) 添加一个下载任务" }
        val snapshots = tasks.map { task ->
            require(task.id.isNotBlank()) { "下载任务 ID 不能为空" }
            require(task.url.isNotBlank()) { "下载地址不能为空：${task.id}" }
            require(!task.destination.isDirectory) {
                "目标路径不能是目录：${task.destination.path}"
            }
            task.copy(headers = task.headers.toMap())
        }
        require(snapshots.map { it.id }.distinct().size == snapshots.size) {
            "批量下载任务 ID 不能重复"
        }
        require(
            snapshots.map { it.destination.absoluteFile.path }.distinct().size == snapshots.size,
        ) {
            "批量下载目标文件不能重复"
        }
        return DownloadQueueConfig(
            tasks = snapshots,
            retrofit = retrofit,
            concurrencyLimiter = concurrencyLimiter,
            destinationRegistry = destinationRegistry,
            maxConcurrency = maxConcurrency,
            lifecycle = lifecycle,
            subscribeScheduler = subscribeScheduler,
            observeScheduler = observeScheduler,
            progressIntervalMillis = progressIntervalMillis,
            onFinally = onFinally,
        )
    }

    private companion object {
        const val DEFAULT_PROGRESS_INTERVAL_MILLIS = 100L
        const val MIN_PROGRESS_INTERVAL_MILLIS = 50L
    }
}
