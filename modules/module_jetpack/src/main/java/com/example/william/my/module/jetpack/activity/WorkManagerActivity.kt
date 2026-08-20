package com.example.william.my.module.jetpack.activity

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
import com.example.william.my.module.jetpack.work.ExpeditedWorker
import com.example.william.my.module.jetpack.work.UploadWorker
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * WorkManager — 后台任务调度框架
 *
 * WorkManager 是 Android Jetpack 提供的后台任务调度框架，用于处理可靠的后台工作。
 *
 * 核心特性：
 * 1. 可靠性：即使应用退出或设备重启，任务也会执行
 * 2. 约束条件：支持网络、电量、存储等约束条件
 * 3. 任务链：支持任务链式执行，按顺序或并行
 * 4. 重试策略：支持指数退避重试策略
 * 5. 加急任务：支持 Expedited 前台服务即时调度
 *
 * 任务类型：
 * 1. OneTimeWorkRequest：一次性任务
 * 2. PeriodicWorkRequest：定期任务（最短 15 分钟）
 * 3. 加急任务：使用 setExpedited()，适合高优先级即时任务
 *
 * 基本用法：
 * ```kotlin
 * // 创建一次性任务
 * val workRequest = OneTimeWorkRequestBuilder<MyWorker>()
 *     .setConstraints(constraints)
 *     .setInitialDelay(10, TimeUnit.SECONDS)
 *     .build()
 *
 * // 提交任务
 * WorkManager.getInstance(context).enqueue(workRequest)
 *
 * // 观察任务状态
 * WorkManager.getInstance(context).getWorkInfoByIdLiveData(workRequest.id)
 *     .observe(this) { workInfo ->
 *         // 处理任务状态变化
 *     }
 * ```
 *
 * 适用场景：
 * - 后台数据同步
 * - 文件上传下载
 * - 定期日志上传
 * - 需要可靠执行的任务
 *
 * https://developer.android.google.cn/topic/libraries/architecture/workmanager
 */
@Route(path = RouterPath.Jetpack.WorkManager)
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
        private const val TAG_WORK = "jetpack_work_sample"
        private const val UNIQUE_WORK_NAME = "jetpack_unique_upload_work"
    }
}