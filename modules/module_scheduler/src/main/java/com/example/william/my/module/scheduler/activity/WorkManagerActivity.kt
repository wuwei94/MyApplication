package com.example.william.my.module.scheduler.activity

import android.os.Bundle
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.scheduler.work.ExpeditedWorker
import com.example.william.my.module.scheduler.work.UploadWorker
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * WorkManager — 现代化后台任务调度框架
 *
 * WorkManager 用于处理可靠的、带约束条件的后台异步与定时工作。
 *
 * 核心特性：
 * 1. 可靠性：即使应用退出或设备重启，任务也会可靠执行；
 * 2. 约束条件：支持网络类型、充电状态、空闲状态、电量等约束；
 * 3. 任务链式编排：支持 beginWith -> then 串并行编排；
 * 4. 指数退避重试策略：支持 BackoffPolicy 灵活退避；
 * 5. 加急任务（Expedited Work）：支持 OutOfQuotaPolicy 搭配前台通知即时调度。
 *
 * 与 JobScheduler / HandlerThread / AsyncTask 的选型边界：
 * - WorkManager：需要跨进程/重启持久化可靠执行的任务、带约束后台任务、加急前台 Worker；
 * - JobScheduler：系统级定时 JobService；
 * - HandlerThread / 协程：应用内生命周期相关的轻量异步并发。
 */
@Route(path = RouterPath.Scheduler.WorkManager)
class WorkManagerActivity : BasicResponseActivity() {

    private val workManager by lazy { WorkManager.getInstance(applicationContext) }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("点击下方列表项提交与管理 WorkManager 后台任务")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "提交一次性任务 (带约束与参数)",
            "提交加急任务 (Expedited Worker)",
            "提交唯一任务 (UniqueWork REPLACE)",
            "链式执行任务 (beginWith -> then)",
            "取消所有任务 (Cancel All)"
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> enqueueOneTimeWork()
            1 -> enqueueExpeditedWork()
            2 -> enqueueUniqueWork()
            3 -> enqueueChainWork()
            4 -> cancelWork()
        }
    }

    /**
     * 1. 提交带约束和输入参数的一次性任务
     */
    private fun enqueueOneTimeWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val inputData = Data.Builder()
            .putString("key", "用户图片数据.jpg")
            .build()

        val request = OneTimeWorkRequestBuilder<UploadWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .setBackoffCriteria(BackoffPolicy.LINEAR, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
            .addTag(TAG_WORK)
            .build()

        workManager.enqueue(request)
        appendLog("已提交一次性任务: ${request.id}")
        observeWork(request.id, "一次性任务")
    }

    /**
     * 2. 提交加急任务（Expedited Work，支持前台服务通知）
     */
    private fun enqueueExpeditedWork() {
        val request = OneTimeWorkRequestBuilder<ExpeditedWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .addTag(TAG_WORK)
            .build()

        workManager.enqueue(request)
        appendLog("已提交加急任务 (Expedited): ${request.id}")
        observeWork(request.id, "加急任务")
    }

    /**
     * 3. 提交唯一任务（如果已存在则替换 REPLACE）
     */
    private fun enqueueUniqueWork() {
        val inputData = Data.Builder()
            .putString("key", "唯一同步任务")
            .build()

        val request = OneTimeWorkRequestBuilder<UploadWorker>()
            .setInputData(inputData)
            .addTag(TAG_WORK)
            .build()

        workManager.beginUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.REPLACE, request).enqueue()
        appendLog("已提交唯一任务 (REPLACE): $UNIQUE_WORK_NAME -> ${request.id}")
        observeWork(request.id, "唯一任务")
    }

    /**
     * 4. 链式任务执行：并行执行 Task A 和 Task B，全部完成后执行 Task C
     */
    private fun enqueueChainWork() {
        val taskA = OneTimeWorkRequestBuilder<UploadWorker>()
            .setInputData(Data.Builder().putString("key", "任务 A (并行)").build())
            .addTag(TAG_WORK)
            .build()

        val taskB = OneTimeWorkRequestBuilder<UploadWorker>()
            .setInputData(Data.Builder().putString("key", "任务 B (并行)").build())
            .addTag(TAG_WORK)
            .build()

        val taskC = OneTimeWorkRequestBuilder<UploadWorker>()
            .setInputData(Data.Builder().putString("key", "任务 C (串行收尾)").build())
            .addTag(TAG_WORK)
            .build()

        workManager.beginWith(listOf(taskA, taskB))
            .then(taskC)
            .enqueue()

        appendLog("已提交链式任务: [Task A, Task B] -> then Task C")
        observeWork(taskA.id, "Task A")
        observeWork(taskB.id, "Task B")
        observeWork(taskC.id, "Task C")
    }

    /**
     * 监听任务执行状态、进度与输出
     */
    private fun observeWork(id: UUID, label: String) {
        workManager.getWorkInfoByIdLiveData(id).observe(this) { info: WorkInfo? ->
            if (info != null) {
                val progress = info.progress.getInt("progress", -1)
                val progressText = if (progress >= 0) ", 进度: $progress%" else ""
                val output = info.outputData.getString("result")
                val outputText = if (!output.isNullOrEmpty()) ", 结果: $output" else ""
                appendLog("[$label] 状态: ${info.state}$progressText$outputText (ID: ${id.toString().substring(0, 8)}...)")
            }
        }
    }

    /**
     * 取消所有任务
     */
    private fun cancelWork() {
        workManager.cancelAllWorkByTag(TAG_WORK)
        appendLog("已取消所有带有 tag [$TAG_WORK] 的任务")
    }

    override fun onDestroy() {
        super.onDestroy()
        workManager.cancelAllWorkByTag(TAG_WORK)
    }

    companion object {
        private const val TAG_WORK = "scheduler_work_sample"
        private const val UNIQUE_WORK_NAME = "scheduler_unique_upload_work"
    }
}
