package com.example.william.my.basic.basic_repo.sync

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.william.my.basic.basic_repo.sync.work.SyncWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.map

/**
 * 基于 Jetpack WorkManager 的增量同步管理实现
 *
 * 通过 [WorkManager.getWorkInfosForUniqueWorkFlow] 监听唯一任务的生命周期，
 * 将底层任务执行状态无缝桥接为响应式 [isSyncing] 流。
 */
class WorkManagerSyncManager(
    private val context: Context,
) : SyncManager {

    override val isSyncing: Flow<Boolean> =
        WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkFlow(SyncWorker.SYNC_WORK_NAME)
            .map { list -> list.any { it.state == WorkInfo.State.RUNNING } }
            .conflate()

    override fun requestSync() {
        val workRequest = SyncWorker.buildOneTimeWorkRequest()
        WorkManager.getInstance(context).enqueueUniqueWork(
            SyncWorker.SYNC_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            workRequest,
        )
    }
}
