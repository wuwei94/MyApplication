package com.example.william.my.module.systemservice.activity.timezone

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.timezone.LiveTimeZoneMonitor
import com.example.william.my.core.base.timezone.TimeZoneMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * 系统时区监视器 — TimeZoneMonitor（Environment Monitors）
 *
 * 与 NetworkMonitor 构成环境监视器平行对：
 * 1. 【契约】：[TimeZoneMonitor.currentTimeZone] 暴露系统时区 Flow；
 * 2. 【生产实现】：[LiveTimeZoneMonitor] 监听 ACTION_TIMEZONE_CHANGED；
 * 3. 【痛点】：不重启 App 时，系统改时区后若 UI 只在启动时读一次 TimeZone，时间戳会漂移；
 * 4. 【消费方式】：收集 Flow，在每次时区变更时用新时区重新格式化时间。
 *
 * 操作：进入页面后到系统设置修改时区，观察日志中的时区与格式化时间是否自动更新。
 *
 * 官方参考：
 * https://developer.android.google.cn/reference/android/content/Intent#ACTION_TIMEZONE_CHANGED
 */
@Route(path = RouterPath.SystemService.TimeZoneMonitor)
class TimeZoneMonitorActivity : BasicResponseActivity() {

    private val monitor: TimeZoneMonitor by lazy { LiveTimeZoneMonitor(applicationContext) }

    private val monitorScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var collectJob: Job? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription(
            "点击下方列表项观察系统时区 Flow。\n" +
                "可到系统设置修改时区，监听开启时日志会自动打印新时区与格式化时间。",
        )
        appendCurrentTimeZoneOnce()
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 读取当前系统时区（启动瞬时值）",
        "2. 开启时区变更监听（collect Flow）",
        "3. 停止时区变更监听",
        "4. 用当前时区格式化时间戳示例",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> appendCurrentTimeZoneOnce()
            1 -> startObserveTimeZone()
            2 -> stopObserveTimeZone()
            3 -> formatTimestampWithSystemZone()
        }
    }

    override fun onDestroy() {
        stopObserveTimeZone()
        monitorScope.cancel()
        super.onDestroy()
    }

    private fun appendCurrentTimeZoneOnce() {
        val zone = TimeZone.getDefault()
        appendLog("→ 当前系统时区: ${zone.id} (offset=${zone.getOffset(Date().time) / 60_000}min)")
    }

    private fun startObserveTimeZone() {
        if (collectJob?.isActive == true) {
            appendLog("监听已在运行，请到系统设置切换时区观察推流")
            return
        }
        appendLog("→ 开启 TimeZoneMonitor 监听（ACTION_TIMEZONE_CHANGED）...")
        collectJob = monitorScope.launch {
            monitor.currentTimeZone.collect { zone ->
                val formatted = formatNow(zone)
                appendLog("[TimeZoneMonitor] 时区变更: ${zone.id} | now=$formatted")
            }
        }
        appendLog("✓ 监听已开启，切换系统时区后观察日志")
    }

    private fun stopObserveTimeZone() {
        if (collectJob?.isActive != true) {
            appendLog("监听未在运行")
            return
        }
        collectJob?.cancel()
        collectJob = null
        appendLog("✓ 已停止时区变更监听")
    }

    private fun formatTimestampWithSystemZone() {
        val zone = TimeZone.getDefault()
        appendLog("→ 使用系统时区 ${zone.id} 格式化: ${formatNow(zone)}")
    }

    private fun formatNow(zone: TimeZone): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.US)
        formatter.timeZone = zone
        return formatter.format(Date())
    }
}
