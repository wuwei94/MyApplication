package com.example.william.my.core.base.utils

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

/**
 * App 全局线程池
 * Global executor pools for the whole application.
 *
 * 避免任务饥饿，例如磁盘读取不会延迟网络请求。
 * 演示 Google Architecture Components 经典的单例并发执行器编排设计模式。
 */
@Deprecated(
    message = "仅作为并发执行器编排教学示例保留。现代开发推荐使用 Kotlin 协程 @Dispatcher 限定符注入（如 Dispatchers.IO），或使用 Blankj 的 ThreadUtils",
    replaceWith = ReplaceWith("ThreadUtils", "com.blankj.utilcode.util.ThreadUtils"),
)
object AppExecutorsHelper {

    /**
     * UI线程
     */
    private val mMain: Executor = MainThreadExecutor()

    /**
     * 磁盘IO线程
     */
    private val mDiskIO: ExecutorService = Executors.newSingleThreadExecutor()

    /**
     * 网络IO线程
     */
    private val mNetworkIO: ExecutorService = Executors.newFixedThreadPool(3)

    /**
     * 定时任务线程池
     */
    private val scheduledExecutor: ScheduledExecutorService = Executors.newScheduledThreadPool(
        Runtime.getRuntime().availableProcessors() * 3 + 2,
    )

    fun main(): Executor = mMain

    fun diskIO(): ExecutorService = mDiskIO

    fun networkIO(): ExecutorService = mNetworkIO

    /**
     * 定时(延时)任务线程池
     * 替代Timer,执行定时任务,延时任务
     */
    fun scheduledExecutor(): ScheduledExecutorService = scheduledExecutor

    /**
     * MainThreadExecutor
     */
    private class MainThreadExecutor : Executor {

        override fun execute(command: Runnable) {
            Handler(Looper.getMainLooper()).post(command)
        }
    }
}
