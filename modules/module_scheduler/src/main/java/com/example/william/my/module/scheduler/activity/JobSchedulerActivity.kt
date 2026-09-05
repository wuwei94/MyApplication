package com.example.william.my.module.scheduler.activity

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.PersistableBundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.basic.basic_shared.utils.Utils
import com.example.william.my.module.scheduler.service.MyJobSchedulerService
import java.lang.ref.WeakReference

/**
 * JobScheduler — 系统任务调度
 *
 * JobScheduler 是 Android 提供的任务调度 API，用于在满足特定条件时执行任务。
 *
 * 核心特性：
 * 1. 条件调度：根据网络、充电、空闲等条件执行任务
 * 2. 系统优化：系统统一调度，优化电池和性能
 * 3. 可靠性：即使应用退出，任务也会执行
 * 4. 约束条件：支持多种约束条件组合
 *
 * 约束条件：
 * - setMinimumLatency()：最小延迟时间
 * - setOverrideDeadline()：最大截止时间
 * - setRequiredNetworkType()：网络类型要求
 * - setRequiresDeviceIdle()：设备空闲要求
 * - setRequiresCharging()：充电状态要求
 *
 * 基本用法：
 * ```kotlin
 * // 创建 JobInfo
 * val jobInfo = JobInfo.Builder(jobId, serviceName)
 *     .setMinimumLatency(5000L)  // 5 秒延迟
 *     .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)  // 任何网络
 *     .build()
 *
 * // 调度任务
 * val jobScheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
 * jobScheduler.schedule(jobInfo)
 * ```
 *
 * 适用场景：
 * - 后台数据同步
 * - 日志上传
 * - 需要特定条件执行的任务
 *
 * https://developer.android.google.cn/reference/android/app/job/JobScheduler
 */
@Route(path = RouterPath.Scheduler.JobScheduler)
class JobSchedulerActivity : BasicResponseActivity() {

    private var mJobId = 0

    private class JobSchedulerHandler(activity: JobSchedulerActivity) : Handler(Looper.getMainLooper()) {

        private val weakReference: WeakReference<JobSchedulerActivity?> = WeakReference(activity)

        override fun handleMessage(msg: Message) {
            val activity = weakReference.get() ?: return
            when (msg.what) {
                MSG_COLOR_START -> activity.appendLog("onStartJob — Job ID ${msg.obj}")
                MSG_COLOR_STOP -> activity.appendLog("onStopJob — Job ID ${msg.obj}")
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("JobScheduler — 系统任务调度")
        startService()
    }

    private fun startService() {
        val messenger = Messenger(JobSchedulerHandler(this))
        val intent = Intent(this, MyJobSchedulerService::class.java).apply {
            putExtra(KEY_MESSENGER, messenger)
        }
        startService(intent)
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "schedule() — 调度任务",
        "cancelAll() — 取消所有任务",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        when (position) {
            0 -> scheduleJob()
            1 -> cancelAllJobs()
        }
    }

    private fun scheduleJob() {
        val jobService = ComponentName(this, MyJobSchedulerService::class.java)

        val builder = JobInfo.Builder(mJobId++, jobService).apply {
            setMinimumLatency(3 * 1000L)
            setOverrideDeadline(5 * 1000L)
            setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            setRequiresDeviceIdle(true)
            setRequiresCharging(true)
            setExtras(
                PersistableBundle().apply {
                    putLong(KEY_WORK_DURATION, 1000)
                },
            )
        }

        val jobScheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.schedule(builder.build())
        appendLog("schedule — 任务已调度，Job ID: ${mJobId - 1}")
    }

    private fun cancelAllJobs() {
        val jobScheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.cancelAll()
        Utils.toast("所有任务已取消")
        appendLog("cancelAll — 所有任务已取消")
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelAllJobs()
        stopService(Intent(this, MyJobSchedulerService::class.java))
    }

    companion object {
        const val MSG_COLOR_START = 0
        const val MSG_COLOR_STOP = 1
        const val KEY_MESSENGER = "com.example.scheduler.MESSENGER_INTENT_KEY"
        const val KEY_WORK_DURATION = "com.example.scheduler.WORK_DURATION_KEY"
    }
}
