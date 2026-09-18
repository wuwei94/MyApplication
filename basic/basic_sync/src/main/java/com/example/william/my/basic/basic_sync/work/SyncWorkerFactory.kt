package com.example.william.my.basic.basic_sync.work

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sync Worker 装配平行对专用 WorkerFactory
 *
 * Application 实现 `Configuration.Provider` 后需显式声明如何创建各类 Worker：
 * 1. [HiltSyncWorker]：委托 [HiltWorkerFactory] 完成 `@HiltWorker` 构造注入；
 * 2. [ServiceLocatorSyncWorker]：由本工厂按 `(Context, WorkerParameters)` 直接构造；
 * 3. 其他 Worker 返回 null，交由 WorkManager 默认创建逻辑。
 */
@Singleton
class SyncWorkerFactory @Inject constructor(
    private val hiltWorkerFactory: HiltWorkerFactory,
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? = when (workerClassName) {
        HiltSyncWorker::class.java.name ->
            hiltWorkerFactory.createWorker(appContext, workerClassName, workerParameters)

        ServiceLocatorSyncWorker::class.java.name ->
            ServiceLocatorSyncWorker(appContext, workerParameters)

        else -> null
    }
}
