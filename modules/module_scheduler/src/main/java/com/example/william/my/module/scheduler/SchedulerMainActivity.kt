package com.example.william.my.module.scheduler

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * 后台任务调度模块入口 — 导航到 JobScheduler、WorkManager 示例页面。
 *
 * 本模块聚焦「后台任务调度」主题：系统原生的 JobScheduler 与 Jetpack 的 WorkManager，
 * 与 module_async（线程/协程异步）区分开。
 */
@Route(path = RouterPath.Scheduler.Main)
class SchedulerMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        return arrayListOf(
            RouterItem("JobScheduler（系统原生后台任务调度）", RouterPath.Scheduler.JobScheduler),
            RouterItem("WorkManager（Jetpack 可靠后台任务与前台加急）", RouterPath.Scheduler.WorkManager)
        )
    }
}
