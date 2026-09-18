package com.example.william.my.module.scheduler.activity

import android.os.Bundle
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.basic.basic_sync.work.HiltSyncWorker
import com.example.william.my.basic.basic_sync.work.ServiceLocatorSyncWorker
import java.util.UUID

/**
 * Sync Worker 装配平行对 — ServiceLocatorSyncWorker vs HiltSyncWorker
 *
 * 核心机制与避坑点：
 * 1. ServiceLocator 路径：[ServiceLocatorSyncWorker] 在 `lazy` 中调用
 *    [Sync.provideSyncPreferencesDataSource]，运行时查找单例；
 * 2. Hilt 路径：[HiltSyncWorker] 使用 `@HiltWorker` + `@AssistedInject`，
 *    依赖由 Application 的 `SyncWorkerFactory` 注入；
 * 3. 共享游标：两条路径绑定同一 Proto DataStore 单例，避免同文件多实例打开；
 * 4. 调度面：均经 `WorkManager.enqueue*` 入队，约束为有网，业务同步逻辑同构。
 *
 * 官方参考：
 * https://developer.android.google.cn/training/dependency-injection/hilt-android#workmanager
 */
@Route(path = RouterPath.Scheduler.SyncWorkerParallel)
class SyncWorkerParallelActivity : BasicResponseActivity() {

    private val workManager by lazy { WorkManager.getInstance(applicationContext) }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription(
            "点击下方列表项，分别提交 ServiceLocatorSyncWorker（运行时查单例）与 HiltSyncWorker（@HiltWorker 构造注入）",
        )
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 提交 ServiceLocatorSyncWorker（ServiceLocator 查单例）",
        "2. 提交 HiltSyncWorker（@HiltWorker 构造注入）",
        "3. 查询两条路径的 WorkInfo",
        "4. 取消两条路径的任务",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> enqueueServiceLocatorSyncWorker()
            1 -> enqueueHiltSyncWorker()
            2 -> queryBothWorkInfo()
            3 -> cancelBoth()
        }
    }

    private fun enqueueServiceLocatorSyncWorker() {
        val request = ServiceLocatorSyncWorker.buildOneTimeWorkRequest()
        workManager.enqueueUniqueWork(
            ServiceLocatorSyncWorker.SYNC_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request,
        )
        appendLog("→ ServiceLocatorSyncWorker 已入队 name=${ServiceLocatorSyncWorker.SYNC_WORK_NAME}")
        observeWork(request.id, "ServiceLocatorSyncWorker")
    }

    private fun enqueueHiltSyncWorker() {
        HiltSyncWorker.enqueueOneTime(applicationContext)
        appendLog("→ HiltSyncWorker 已入队 name=${HiltSyncWorker.SYNC_WORK_NAME}")
        workManager.getWorkInfosForUniqueWorkLiveData(HiltSyncWorker.SYNC_WORK_NAME)
            .observe(this) { infos ->
                infos.firstOrNull()?.let { info ->
                    appendLog("[HiltSyncWorker] 状态: ${info.state} (ID: ${shortId(info.id)})")
                }
            }
    }

    private fun queryBothWorkInfo() {
        workManager.getWorkInfosForUniqueWorkLiveData(ServiceLocatorSyncWorker.SYNC_WORK_NAME)
            .observe(this) { infos ->
                val text = infos.joinToString { "${it.state}(${shortId(it.id)})" }
                    .ifEmpty { "无任务" }
                appendLog("[查询][ServiceLocatorSyncWorker] $text")
            }
        workManager.getWorkInfosForUniqueWorkLiveData(HiltSyncWorker.SYNC_WORK_NAME)
            .observe(this) { infos ->
                val text = infos.joinToString { "${it.state}(${shortId(it.id)})" }
                    .ifEmpty { "无任务" }
                appendLog("[查询][HiltSyncWorker] $text")
            }
    }

    private fun cancelBoth() {
        workManager.cancelUniqueWork(ServiceLocatorSyncWorker.SYNC_WORK_NAME)
        workManager.cancelUniqueWork(HiltSyncWorker.SYNC_WORK_NAME)
        appendLog("✓ 已取消 ServiceLocatorSyncWorker / HiltSyncWorker 任务")
    }

    private fun observeWork(id: UUID, label: String) {
        workManager.getWorkInfoByIdLiveData(id).observe(this) { info: WorkInfo? ->
            if (info != null) {
                appendLog("[$label] 状态: ${info.state} (ID: ${shortId(info.id)})")
            }
        }
    }

    private fun shortId(id: UUID): String = id.toString().substring(0, 8)
}
