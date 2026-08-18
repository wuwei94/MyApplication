package com.example.william.my.module.async.activity

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
import com.example.william.my.module.async.service.MyJobSchedulerService
import java.lang.ref.WeakReference

/**
 * JobScheduler — 系统任务调度演示
 *
 * JobScheduler 是 Android 提供的任务调度 API，可以根据条件（网络、充电等）执行任务
 *
 * 核心步骤：
 * 1. 创建 JobInfo.Builder，配置 jobId 和 JobService
 * 2. 设置约束条件（延迟、网络、空闲、充电等）
 * 3. 调用 JobScheduler.schedule() 提交任务
 * 4. 系统满足条件时回调 JobService 的 onStartJob()
 */
@Route(path = RouterPath.Async.JobScheduler)
class JobSchedulerActivity : BasicResponseActivity() {

    private var mJobId = 0

    private class JobSchedulerHandler(activity: JobSchedulerActivity) :
        Handler(Looper.getMainLooper()) {

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

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "schedule() — 调度任务",
            "cancelAll() — 取消所有任务"
        )
    }

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
            setExtras(PersistableBundle().apply {
                putLong(KEY_WORK_DURATION, 1000)
            })
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
        const val KEY_MESSENGER = "com.example.async.MESSENGER_INTENT_KEY"
        const val KEY_WORK_DURATION = "com.example.async.WORK_DURATION_KEY"
    }
}
