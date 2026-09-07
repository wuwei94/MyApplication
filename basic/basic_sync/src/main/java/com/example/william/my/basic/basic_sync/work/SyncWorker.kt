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
 * 离线优先架构后台增量同步工作者（SyncWorker）
 *
 * 核心机制：
 * 1. 【确定性后台调度】：由 Jetpack WorkManager 统一编排，配置网络连通性等硬件约束；
 * 2. 【高阶同步契约】：自身实现 [Synchronizer] 接口，代理游标版本的读取与原子更新；
 * 3. 【后台幂等】：调用各业务仓储的 [Syncable.sync] 增量拉取，无新数据时不产生写开销；
 * 4. 【失败重试机制】：遇到不可抗力网络错误或解析异常时返回 [Result.retry]，由 WorkManager 指数退避重试。
 */
class SyncWorker(
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
        const val SYNC_WORK_NAME = "SyncWork"

        /**
         * 构建单次增量同步任务（带连网约束）
         */
        fun buildOneTimeWorkRequest(): OneTimeWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            return OneTimeWorkRequestBuilder<SyncWorker>()
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

            return PeriodicWorkRequestBuilder<SyncWorker>(6, TimeUnit.HOURS)
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
