package com.example.william.my.module.jetpack.activity

import android.os.Bundle
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.jetpack.work.ExpeditedWorker
import com.example.william.my.module.jetpack.work.UploadWorker
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * WorkManager
 * https://developer.android.google.cn/topic/libraries/architecture/workmanager
 */
@Route(path = RouterPath.Jetpack.WorkManager)
class WorkManagerActivity : BasicResponseActivity() {

    private lateinit var constraints: Constraints
    private lateinit var oneTimeWorkRequest: OneTimeWorkRequest
    private lateinit var periodicWorkRequest: PeriodicWorkRequest

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("点击下方列表项提交 WorkManager 任务")
        initConstraints()
        initWorkRequest()
    }

    private fun initConstraints() {
        constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED) // 约束运行工作所需的网络类型。例如 Wi-Fi (UNMETERED)。
            .setRequiresBatteryNotLow(true) // 如果设置为 true，那么当设备处于“电量不足模式”时，工作不会运行。
            .setRequiresCharging(true) // 如果设置为 true，那么工作只能在设备充电时运行。
            //.setRequiresDeviceIdle(true) // 如果设置为 true，则要求用户的设备必须处于空闲状态，才能运行工作。
            .setRequiresStorageNotLow(true) // 如果设置为 true，那么当用户设备上的存储空间不足时，工作不会运行。
            .build()
    }

    private fun initWorkRequest() {
        oneTimeWorkRequest = OneTimeWorkRequestBuilder<UploadWorker>() // 一次性工作
            .build()

        periodicWorkRequest =
            PeriodicWorkRequestBuilder<UploadWorker>(1, TimeUnit.HOURS) // 定期工作，可以定义的最短重复间隔是 15 分钟
                .build()

        val expeditedRequest = OneTimeWorkRequestBuilder<ExpeditedWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST) // 执行加急工作
            .build()

        val myWorkRequest = OneTimeWorkRequestBuilder<UploadWorker>()
            // 工作约束
            .setConstraints(constraints)
            // 延迟工作
            .setInitialDelay(3, TimeUnit.SECONDS)
            // 重试和退避政策
            .setBackoffCriteria(
                BackoffPolicy.LINEAR, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS
            )
            // 标记工作
            .addTag("upload")
            // 分配输入数据
            .setInputData(
                Data.Builder().putString("key", "inputData").build()
            ).build()
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf("提交一次性任务", "提交唯一任务 (UniqueWork)", "链式执行任务 (beginWith -> then)", "取消所有任务")
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> enqueueOneTimeWork()
            1 -> enqueueUniqueWork()
            2 -> enqueueChainWork()
            3 -> cancelWork()
        }
    }

    private fun enqueueOneTimeWork() {
        WorkManager.getInstance(this).enqueue(oneTimeWorkRequest)
        appendLog("提交一次性任务: ${oneTimeWorkRequest.id}")
        observeWork(oneTimeWorkRequest.id)
    }

    private fun enqueueUniqueWork() {
        WorkManager.getInstance(this)
            .beginUniqueWork("upload", ExistingWorkPolicy.REPLACE, oneTimeWorkRequest).enqueue()
        appendLog("提交唯一任务 (REPLACE): upload")
        observeWork(oneTimeWorkRequest.id)
    }

    private fun enqueueChainWork() {
        WorkManager.getInstance(this)
            .beginWith(listOf(oneTimeWorkRequest, oneTimeWorkRequest))
            .then(oneTimeWorkRequest)
            .enqueue()
        appendLog("提交链式任务: beginWith -> then")
        observeWork(oneTimeWorkRequest.id)
    }

    private fun observeWork(id: UUID) {
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(id)
            .observe(this) { value ->
                if (value != null) {
                    appendLog("任务状态变更: state=${value.state}, id=$id")
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelWork()
    }

    private fun cancelWork() {
        WorkManager.getInstance(this).cancelWorkById(oneTimeWorkRequest.id)
        WorkManager.getInstance(this).cancelWorkById(periodicWorkRequest.id)
        appendLog("已取消所有任务")
    }
}