package com.example.william.my.module.scheduler.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.example.william.my.module.scheduler.R
import com.example.william.my.module.scheduler.work.utils.createNotification

/**
 * 示例加急后台任务工作者 (Expedited Work)
 */
class ExpeditedWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // 模拟加急任务耗时处理（前台服务通知展示）
        kotlinx.coroutines.delay(2000)
        return Result.success()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            NOTIFICATION_ID, createNotification(
                applicationContext, id,
                applicationContext.getString(R.string.scheduler_notification_title_saving_image)
            )
        )
    }

    companion object {
        private const val NOTIFICATION_ID = 1
    }
}
