package com.example.william.my.core.rx.download.queue

import com.example.william.my.core.retrofit.exception.ApiException
import com.example.william.my.core.rx.download.RxDownload
import com.example.william.my.core.rx.download.callback.RxDownloadCallback
import com.example.william.my.core.rx.download.exception.toDownloadApiException
import com.example.william.my.core.rx.download.model.DownloadProgress
import com.example.william.my.core.rx.download.model.DownloadResult
import com.example.william.my.core.rx.download.queue.model.DownloadQueueEvent
import com.example.william.my.core.rx.download.queue.model.DownloadQueueProgress
import com.example.william.my.core.rx.download.queue.model.DownloadQueueResult
import com.example.william.my.core.rx.download.queue.model.DownloadQueueTask
import com.example.william.my.core.rx.download.queue.model.DownloadTaskFailure
import com.example.william.my.core.rx.download.queue.model.DownloadTaskResult
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

/** 可重复订阅的不可变批量下载队列。 */
class RxDownloadQueue internal constructor(
    private val config: DownloadQueueConfig,
) {

    /**
     * 使用统一下载回调订阅队列。
     *
     * 单任务失败进入最终 [DownloadQueueResult]，只有队列无法继续执行时才调用回调失败方法；
     * 细粒度任务事件仍可通过 [asFlowable] 获取。
     */
    fun subscribeWith(
        callback: RxDownloadCallback<DownloadQueueProgress, DownloadQueueResult>,
    ): Disposable {
        return asFlowable()
            .doOnSubscribe { callback.onLoading() }
            .subscribe(
                { event ->
                    when (event) {
                        is DownloadQueueEvent.OverallProgress -> {
                            callback.onProgress(event.progress)
                        }

                        is DownloadQueueEvent.Completed -> callback.onResponse(event.result)
                        else -> Unit
                    }
                },
                { error -> callback.onFailure(error.toDownloadApiException()) },
            )
    }

    fun asFlowable(): Flowable<DownloadQueueEvent> {
        var source = Flowable.create<DownloadQueueEvent>({ emitter ->
            val output = emitter.serialize()
            val state = QueueState(config.tasks)
            val eventLock = Any()
            val terminationTracker = DownloadQueueTerminationTracker(config.onFinally)
            output.onNext(DownloadQueueEvent.OverallProgress(state.progress()))

            val disposable = Flowable.fromIterable(config.tasks)
                .flatMapSingle(
                    {
                        task -> createTaskSingle(
                            task = task,
                            state = state,
                            eventLock = eventLock,
                            emit = output::onNext,
                            terminationTracker = terminationTracker,
                        )
                    },
                    false,
                    config.maxConcurrency,
                )
                .subscribe(
                    { outcome ->
                        when (outcome) {
                            is TaskOutcome.Success -> {
                                synchronized(eventLock) {
                                    val progress = state.succeed(outcome.task, outcome.result)
                                    output.onNext(
                                        DownloadQueueEvent.TaskSucceeded(
                                            outcome.task,
                                            outcome.result,
                                        )
                                    )
                                    output.onNext(DownloadQueueEvent.OverallProgress(progress))
                                }
                            }

                            is TaskOutcome.Failure -> {
                                synchronized(eventLock) {
                                    val progress = state.fail(outcome.task, outcome.error)
                                    output.onNext(
                                        DownloadQueueEvent.TaskFailed(
                                            outcome.task,
                                            outcome.error,
                                        )
                                    )
                                    output.onNext(DownloadQueueEvent.OverallProgress(progress))
                                }
                            }
                        }
                    },
                    { error ->
                        terminationTracker.close()
                        output.onError(error)
                    },
                    {
                        terminationTracker.close()
                        synchronized(eventLock) {
                            output.onNext(DownloadQueueEvent.Completed(state.result()))
                            output.onComplete()
                        }
                    },
                )
            emitter.setCancellable {
                terminationTracker.close()
                disposable.dispose()
            }
        }, BackpressureStrategy.ERROR)

        config.lifecycle?.let { lifecycle ->
            source = source.compose(lifecycle.bindToLifecycle())
        }
        return source.observeOn(
            config.observeScheduler ?: AndroidSchedulers.mainThread(),
            false,
            EVENT_BUFFER_SIZE,
        )
    }

    private fun createTaskSingle(
        task: DownloadQueueTask,
        state: QueueState,
        eventLock: Any,
        emit: (DownloadQueueEvent) -> Unit,
        terminationTracker: DownloadQueueTerminationTracker,
    ): Single<TaskOutcome> {
        val resources = DownloadQueueTaskResources(
            terminationTracker = terminationTracker,
            concurrencyLimiter = config.concurrencyLimiter,
            destinationRegistry = config.destinationRegistry,
            destination = task.destination,
        )
        return Single.fromCallable {
            resources.acquire()
        }
        .subscribeOn(config.subscribeScheduler)
        .flatMap {
            RxDownload.builder()
                .api(task.url)
                .destination(task.destination)
                .addHeader(task.headers)
                .resume(task.resume)
                .retrofit(config.retrofit)
                .subscribeOn(config.subscribeScheduler)
                .observeOn(Schedulers.trampoline())
                .progressOn(Schedulers.trampoline())
                .progressIntervalMillis(config.progressIntervalMillis)
                .onOperationStart(resources::startOperation)
                .onFinally(resources::finishOperation)
                .onProgress { progress ->
                    synchronized(eventLock) {
                        val overall = state.update(task, progress)
                        emit(DownloadQueueEvent.TaskProgress(task, progress))
                        emit(DownloadQueueEvent.OverallProgress(overall))
                    }
                }
                .buildSingle()
                .doOnSubscribe {
                    synchronized(eventLock) {
                        val overall = state.start(task)
                        emit(DownloadQueueEvent.TaskStarted(task))
                        emit(DownloadQueueEvent.OverallProgress(overall))
                    }
                }
        }
        .doOnDispose(resources::cancel)
        .doFinally(resources::finishIfReady)
        .map<TaskOutcome> { result -> TaskOutcome.Success(task, result) }
        .onErrorReturn { error -> TaskOutcome.Failure(task, error.toDownloadApiException()) }
    }

    private sealed interface TaskOutcome {
        val task: DownloadQueueTask

        data class Success(
            override val task: DownloadQueueTask,
            val result: DownloadResult,
        ) : TaskOutcome

        data class Failure(
            override val task: DownloadQueueTask,
            val error: ApiException,
        ) : TaskOutcome
    }

    private companion object {
        const val EVENT_BUFFER_SIZE = 1024
    }

    private class QueueState(tasks: List<DownloadQueueTask>) {
        private val orderedTasks = tasks.toList()
        private val taskProgress = tasks.associateWith {
            DownloadProgress(currentBytes = 0L, totalBytes = -1L)
        }.toMutableMap()
        private val active = linkedSetOf<DownloadQueueTask>()
        private val successes = linkedMapOf<DownloadQueueTask, DownloadResult>()
        private val failures = linkedMapOf<DownloadQueueTask, ApiException>()

        @Synchronized
        fun start(task: DownloadQueueTask): DownloadQueueProgress {
            active += task
            return progress()
        }

        @Synchronized
        fun update(
            task: DownloadQueueTask,
            progress: DownloadProgress,
        ): DownloadQueueProgress {
            taskProgress[task] = progress
            return progress()
        }

        @Synchronized
        fun succeed(
            task: DownloadQueueTask,
            result: DownloadResult,
        ): DownloadQueueProgress {
            active -= task
            successes[task] = result
            taskProgress[task] = DownloadProgress(result.totalBytes, result.totalBytes)
            return progress()
        }

        @Synchronized
        fun fail(
            task: DownloadQueueTask,
            error: ApiException,
        ): DownloadQueueProgress {
            active -= task
            failures[task] = error
            return progress()
        }

        @Synchronized
        fun progress(): DownloadQueueProgress {
            val values = taskProgress.values
            val allTotalsKnown = values.all { it.totalBytes >= 0L }
            return DownloadQueueProgress(
                currentBytes = values.sumOf { it.currentBytes.coerceAtLeast(0L) },
                totalBytes = if (allTotalsKnown) {
                    values.sumOf { it.totalBytes }
                } else {
                    -1L
                },
                completedCount = successes.size + failures.size,
                successCount = successes.size,
                failedCount = failures.size,
                totalCount = orderedTasks.size,
                activeTaskIds = active.map { it.id },
            )
        }

        @Synchronized
        fun result(): DownloadQueueResult {
            return DownloadQueueResult(
                successes = orderedTasks.mapNotNull { task ->
                    successes[task]?.let { DownloadTaskResult(task, it) }
                },
                failures = orderedTasks.mapNotNull { task ->
                    failures[task]?.let { DownloadTaskFailure(task, it) }
                },
            )
        }
    }
}
