package com.example.william.my.basic.basic_sync.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.william.my.basic.basic_model.ChangeListVersions
import com.example.william.my.basic.basic_repo.data.ServiceLocator
import com.example.william.my.basic.basic_repo.sync.Syncable
import com.example.william.my.basic.basic_repo.sync.Synchronizer
import com.example.william.my.basic.basic_sync.Sync
import java.util.concurrent.TimeUnit

/**
 * ServiceLocator 装配型后台增量同步工作者（ServiceLocatorSyncWorker）
 *
 * 与 [HiltSyncWorker]（Hilt 构造注入）构成 Worker 依赖装配平行对：
 * 1. 【运行时查单例】：`lazy` 中调用 [Sync.provideSyncPreferencesDataSource] 获取游标数据源；
 * 2. 【高阶同步契约】：自身实现 [Synchronizer]，代理游标读取与原子更新；
 * 3. 【确定性调度】：WorkManager 编排，有网约束，失败 [Result.retry] 指数退避；
 * 4. 【业务同构】：`doWork` 与 Hilt 路径相同，差异仅在依赖获取方式。
 *
 * 官方参考：
 * https://developer.android.google.cn/topic/libraries/architecture/workmanager
 */
class ServiceLocatorSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams),
    Synchronizer {

    private val syncPreferences by lazy {
        Sync.provideSyncPreferencesDataSource(applicationContext)
    }

    override suspend fun getChangeListVersions(): ChangeListVersions = syncPreferences.getChangeListVersions()

    override suspend fun updateChangeListVersions(update: ChangeListVersions.() -> ChangeListVersions) = syncPreferences.updateChangeListVersions(update)

    override suspend fun doWork(): Result {
        val articleRepo = ServiceLocator.provideArticleRepository(applicationContext)
        val syncable = articleRepo as? Syncable ?: return Result.success()

        val isSuccess = syncable.sync()
        return if (isSuccess) {
            Result.success()
        } else {
            Result.retry()
        }
    }

    companion object {
        const val SYNC_WORK_NAME = "ServiceLocatorSyncWork"

        /**
         * 构建单次增量同步任务（带连网约束）
         */
        fun buildOneTimeWorkRequest(): OneTimeWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            return OneTimeWorkRequestBuilder<ServiceLocatorSyncWorker>()
                .setConstraints(constraints)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
        }

        /**
         * 构建后台周期性轮询增量同步任务（默认 6 小时间隔，带连网约束）
         */
        fun buildPeriodicWorkRequest(): PeriodicWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            return PeriodicWorkRequestBuilder<ServiceLocatorSyncWorker>(6, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()
        }

        /**
         * 应用启动时发起静默单次同步（若已有正在执行的同步则保留，避免重复）
         */
        fun startUpSyncWork(context: Context) {
            WorkManager.getInstance(context).enqueueUniqueWork(
                SYNC_WORK_NAME,
                ExistingWorkPolicy.KEEP,
                buildOneTimeWorkRequest(),
            )
        }
    }
}
