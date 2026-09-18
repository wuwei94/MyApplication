package com.example.william.my.basic.basic_sync.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.william.my.basic.basic_datastore.SyncPreferencesDataSource
import com.example.william.my.basic.basic_model.ChangeListVersions
import com.example.william.my.basic.basic_repo.data.ServiceLocator
import com.example.william.my.basic.basic_repo.sync.Syncable
import com.example.william.my.basic.basic_repo.sync.Synchronizer
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Hilt 注入型后台增量同步工作者（HiltSyncWorker）
 *
 * 与 [ServiceLocatorSyncWorker]（运行时查单例）构成 Worker 依赖装配平行对：
 * 1. 【构造注入】：`@HiltWorker` + `@AssistedInject`，依赖由 Hilt 编译期图注入，Worker 不自行查找单例；
 * 2. 【协助参数】：`Context` / `WorkerParameters` 必须标注 `@Assisted`，由 WorkManager 运行时提供；
 * 3. 【同步契约】：自身实现 [Synchronizer]，游标读写与业务 [Syncable.sync] 逻辑同构；
 * 4. 【实例约束】：游标 [SyncPreferencesDataSource] 经 Hilt 绑定到 [Sync] 进程内单例，避免 DataStore 同文件多实例。
 *
 * 官方参考：
 * https://developer.android.google.cn/training/dependency-injection/hilt-android#workmanager
 */
@HiltWorker
class HiltSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncPreferences: SyncPreferencesDataSource,
) : CoroutineWorker(appContext, workerParams),
    Synchronizer {

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
        const val SYNC_WORK_NAME = "HiltSyncWork"

        fun buildOneTimeWorkRequest(): OneTimeWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            return OneTimeWorkRequestBuilder<HiltSyncWorker>()
                .setConstraints(constraints)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
        }

        /**
         * 以 Hilt 装配路径发起一次唯一增量同步
         */
        fun enqueueOneTime(context: Context) {
            WorkManager.getInstance(context).enqueueUniqueWork(
                SYNC_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                buildOneTimeWorkRequest(),
            )
        }
    }
}
