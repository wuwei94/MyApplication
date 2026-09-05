package com.example.william.my.module.performance.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.metrics.performance.JankStats
import androidx.metrics.performance.PerformanceMetricsState
import androidx.tracing.Trace
import androidx.tracing.trace
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import java.util.Locale

/**
 * Jetpack JankStats + AndroidX Tracing —— 运行时卡顿监控闭环
 *
 * 与 [com.example.william.my.module.performance.activity.BaselineProfilesActivity] 的分工：
 * - Baseline Profiles + Macrobenchmark：解决**实验室阶段**的冷启动与掉帧，属于发布前的离线优化；
 * - JankStats + 自定义 Trace Section：解决**线上运行阶段**的真实设备卡顿归因，属于运行时的持续观测。
 *
 * 核心 API：
 * 1. [JankStats.createAndTrack]：注册逐帧回调，回调在后台线程触发，逐个上报 [androidx.metrics.performance.FrameData]
 *    （isJank 卡顿标记、frameDurationUiNanos 帧耗时、states 该帧所处的 UI 状态）；
 * 2. [PerformanceMetricsState.getHolderForHierarchy]：为视图树绑定状态容器，卡顿帧会自动携带业务状态，
 *    便于把卡顿归因到具体页面/操作而不是只得到一个百分比；
 * 3. [trace] / [Trace.beginAsyncSection]：写入自定义 trace section，让 System Trace / Perfetto 中
 *    除了系统帧数据外还能看到业务自身的耗时区间。
 */
@Route(path = RouterPath.Performance.JankStats)
class JankStatsActivity : BasicResponseActivity() {

    private val mainHandler = Handler(Looper.getMainLooper())

    private lateinit var metricsStateHolder: PerformanceMetricsState.Holder

    private var jankStats: JankStats? = null

    /** 逐帧回调在后台线程触发，统计数据用锁保护，UI 只读取快照 */
    private val statsLock = Any()
    private var totalFrames = 0
    private var jankFrames = 0
    private var totalFrameNanos = 0L
    private var slowestFrameNanos = 0L
    private var lastJankStates = ""

    private var stateMarkerAttached = false
    private var asyncCookie = 0
    private var heavyWorkCount = 0
    private var pendingAsyncEnd: Runnable? = null

    /** 统计快照：持锁拷贝一份，避免在 UI 线程上长时间持有锁 */
    private data class StatsSnapshot(
        val totalFrames: Int,
        val jankFrames: Int,
        val totalFrameNanos: Long,
        val slowestFrameNanos: Long,
        val lastJankStates: String,
    )

    private fun readStats(): StatsSnapshot = synchronized(statsLock) {
        StatsSnapshot(totalFrames, jankFrames, totalFrameNanos, slowestFrameNanos, lastJankStates)
    }

    private val frameListener = JankStats.OnFrameListener { frameData ->
        synchronized(statsLock) {
            totalFrames++
            totalFrameNanos += frameData.frameDurationUiNanos
            if (frameData.frameDurationUiNanos > slowestFrameNanos) {
                slowestFrameNanos = frameData.frameDurationUiNanos
            }
            if (frameData.isJank) {
                jankFrames++
                lastJankStates = frameData.states.joinToString(", ") { "${it.key}=${it.value}" }
            }
        }
    }

    private val statsRefreshRunnable = object : Runnable {
        override fun run() {
            updateLog(KEY_STATS, formatStats(readStats()))
            mainHandler.postDelayed(this, STATS_REFRESH_INTERVAL_MS)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        metricsStateHolder = PerformanceMetricsState.getHolderForHierarchy(window.decorView)
        showDescription(
            """
            JankStats 运行时卡顿监控 + 自定义系统追踪：
            • JankStats：逐帧采集，标记卡顿帧并携带 UI 状态用于归因；
            • PerformanceMetricsState：为卡顿帧打上页面/操作标签；
            • androidx.tracing：写入自定义 trace section，可在 System Trace / Perfetto 中对齐查看。
            """.trimIndent(),
        )
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 启动 / 停止 JankStats 采集（实时卡顿率面板）",
        "2. 主线程注入一次卡顿（同步 trace section 包裹）",
        "3. 连续注入 5 次卡顿（观察卡顿率面板变化）",
        "4. 打印当前统计汇总",
        "5. 自定义同步 Trace Section 与 Counter",
        "6. 自定义异步 Trace Section（beginAsyncSection）",
        "7. 切换 UI 状态标记（PerformanceMetricsState）",
        "8. JankStats 判定规则与 System Trace 查看方式",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> toggleTracking()
            1 -> injectSingleJank()
            2 -> injectJankBurst(BURST_TIMES)
            3 -> printStatsSummary()
            4 -> demoSyncTraceSection()
            5 -> demoAsyncTraceSection()
            6 -> toggleStateMarker()
            7 -> showDiagnosticsGuide()
        }
    }

    /**
     * 1. 启动 / 停止逐帧采集
     *
     * [JankStats.createAndTrack] 内部按 API 等级选择实现：API 24+ 使用 FrameMetrics 拿到真实帧耗时，
     * API 16~23 退化为 Choreographer 估算。回调在 JankStats 自己的后台线程触发，因此 UI 侧统一通过
     * 定时刷新的 [statsRefreshRunnable] 读取快照，避免每帧都回到主线程造成新的抖动。
     */
    private fun toggleTracking() {
        val current = jankStats
        if (current != null) {
            current.isTrackingEnabled = false
            jankStats = null
            stopStatsRefresh()
            metricsStateHolder.state?.removeState(STATE_KEY_PAGE)
            removeUpdatingLog(KEY_STATS)
            appendLog("── JankStats 采集已停止 ──")
            return
        }

        synchronized(statsLock) {
            totalFrames = 0
            jankFrames = 0
            totalFrameNanos = 0L
            slowestFrameNanos = 0L
            lastJankStates = ""
        }
        jankStats = JankStats.createAndTrack(window, frameListener)
        metricsStateHolder.state?.putState(STATE_KEY_PAGE, "JankStats")
        startStatsRefresh()
        appendLog("── JankStats 采集已启动，卡顿率面板每 ${STATS_REFRESH_INTERVAL_MS}ms 刷新 ──")
    }

    /**
     * 2/3. 在主线程制造可控的阻塞，模拟同步 I/O 或重计算导致的掉帧
     *
     * 阻塞区间包在 [trace] 内，System Trace 中这段耗时会显示为一个具名 section。
     */
    private fun injectSingleJank() {
        if (!isTracking()) {
            return
        }
        val elapsedMs = trace(SECTION_INJECT_JANK) {
            blockMainThreadFor(BLOCK_FRAME_BUDGET_MS)
        }
        appendLog("注入主线程阻塞 ${formatMillis(elapsedMs)}（trace section=\"$SECTION_INJECT_JANK\"）")
    }

    private fun injectJankBurst(times: Int) {
        if (!isTracking()) {
            return
        }
        appendLog("── 连续注入 $times 次主线程阻塞，每次 ${BLOCK_FRAME_BUDGET_MS}ms ──")
        repeat(times) { index ->
            trace("$SECTION_INJECT_JANK#$index") {
                blockMainThreadFor(BLOCK_FRAME_BUDGET_MS)
            }
        }
        appendLogAccent("已注入完毕，观察上方卡顿率面板（面板最多延迟 ${STATS_REFRESH_INTERVAL_MS}ms 更新）")
    }

    /**
     * 4. 打印累计统计
     */
    private fun printStatsSummary() {
        if (!isTracking()) {
            return
        }
        val stats = readStats()
        appendLog("── 统计汇总 ──\n${formatStats(stats)}")
        if (stats.lastJankStates.isNotEmpty()) {
            appendLog("最近一次卡顿帧携带的 UI 状态：${stats.lastJankStates}")
        }
    }

    /**
     * 5. 同步 trace section + counter
     *
     * [trace] 是 [Trace.beginSection] / [Trace.endSection] 的 Kotlin 封装，带返回值且异常安全；
     * [Trace.setCounter] 写入数值轨道（API 29+，低版本为空实现），适合观察队列长度等连续量。
     */
    private fun demoSyncTraceSection() {
        val result = trace(SECTION_HEAVY_WORK) {
            Trace.setCounter(COUNTER_HEAVY_WORK, ++heavyWorkCount)
            blockMainThreadFor(HEAVY_WORK_MS)
            "累计执行第 $heavyWorkCount 次"
        }
        appendLog("同步 section \"$SECTION_HEAVY_WORK\" 写入完成：$result")
        appendLogAccent(
            "Trace.isEnabled=${Trace.isEnabled()}（未开启系统追踪时 section 为空操作，运行时开销可忽略）",
        )
    }

    /**
     * 6. 异步 trace section
     *
     * 跨多帧的任务无法用 begin/endSection 表达，需要用 cookie 区分同名并发任务。
     */
    private fun demoAsyncTraceSection() {
        val cookie = asyncCookie++
        Trace.beginAsyncSection(SECTION_ASYNC_TASK, cookie)
        appendLog("异步 section 开始：\"$SECTION_ASYNC_TASK\" cookie=$cookie")
        val end = Runnable {
            Trace.endAsyncSection(SECTION_ASYNC_TASK, cookie)
            pendingAsyncEnd = null
            appendLog("异步 section 结束：\"$SECTION_ASYNC_TASK\" cookie=$cookie")
        }
        pendingAsyncEnd = end
        // 用固定时延模拟一次跨帧的异步任务，使 section 在 System Trace 中呈现为一段有跨度的区间
        mainHandler.postDelayed(end, ASYNC_TASK_MS)
    }

    /**
     * 7. UI 状态标记
     *
     * 状态会随每一帧的 [androidx.metrics.performance.FrameData.states] 一起上报，
     * 是把「卡顿率」下钻到「哪个页面/哪种操作在卡」的关键。
     *
     * [PerformanceMetricsState.putSingleFrameState] 只作用于下一帧，适合标记一次性的瞬时事件。
     */
    private fun toggleStateMarker() {
        val state = metricsStateHolder.state
        if (state == null) {
            appendLog("状态容器尚未就绪")
            return
        }
        if (stateMarkerAttached) {
            state.removeState(STATE_KEY_MODE)
            appendLog("已移除状态标记：$STATE_KEY_MODE")
        } else {
            state.putState(STATE_KEY_MODE, "heavy-list")
            appendLog("已写入状态标记：$STATE_KEY_MODE=heavy-list（卡顿帧上报时会携带）")
        }
        stateMarkerAttached = !stateMarkerAttached
    }

    /**
     * 8. 判定规则与查看方式
     */
    private fun showDiagnosticsGuide() {
        appendLog(
            """
            ── JankStats 判定规则 ──
            • 卡顿判定：单帧耗时 > 预期帧耗时 × jankHeuristicMultiplier（默认 2.0）即标记为卡顿帧；
              60Hz 屏幕预期 16.7ms，超过约 33.4ms 记一次卡顿；120Hz 屏幕阈值自动减半。
            • 阈值可调：JankStats.jankHeuristicMultiplier = 1.5f 可收紧判定。
            • 与 Macrobenchmark 的关系：FrameTimingMetric 在实验室给出可复现的 P50/P90/P99，
              JankStats 在真实设备上长期采样，两者共用同一套「卡顿帧」口径，可交叉验证。

            ── 查看自定义 trace section ──
            1. Android Studio Profiler → CPU → System Trace 录制，搜索 JankStatsDemo 前缀的 section；
            2. 命令行抓取（需要 debuggable 或 profileable 配置）：
               adb shell perfetto -o /data/misc/perfetto-traces/trace.pb -t 10s sched freq idle am wm gfx view
            3. 打开 https://ui.perfetto.dev 导入 trace，即可同时看到帧数据与自定义 section 的对齐时间轴。
            """.trimIndent(),
        )
    }

    private fun isTracking(): Boolean {
        if (jankStats == null) {
            appendLog("请先执行第 1 项启动采集")
        }
        return jankStats != null
    }

    /** 占用主线程至指定耗时，返回实际耗时（毫秒） */
    private fun blockMainThreadFor(timeoutMs: Long): Float {
        val startNanos = System.nanoTime()
        val budgetNanos = timeoutMs * NANOS_PER_MILLI
        while (System.nanoTime() - startNanos < budgetNanos) {
            // 故意阻塞主线程：模拟同步 I/O 或重计算占满该帧预算
        }
        return (System.nanoTime() - startNanos).toFloat() / NANOS_PER_MILLI
    }

    private fun startStatsRefresh() {
        mainHandler.removeCallbacks(statsRefreshRunnable)
        mainHandler.post(statsRefreshRunnable)
    }

    private fun stopStatsRefresh() {
        mainHandler.removeCallbacks(statsRefreshRunnable)
    }

    private fun formatStats(stats: StatsSnapshot): String {
        if (stats.totalFrames == 0) {
            return "等待首帧数据…"
        }
        val jankRate = stats.jankFrames * 100f / stats.totalFrames
        val avgMs = stats.totalFrameNanos.toFloat() / stats.totalFrames / NANOS_PER_MILLI
        val slowestMs = stats.slowestFrameNanos.toFloat() / NANOS_PER_MILLI
        return "总帧数=${stats.totalFrames} | 卡顿帧=${stats.jankFrames} | " +
            "卡顿率=${formatMillis(jankRate)}% | 平均帧耗时=${formatMillis(avgMs)}ms | " +
            "最长帧=${formatMillis(slowestMs)}ms"
    }

    private fun formatMillis(value: Float): String = String.format(Locale.getDefault(), "%.1f", value)

    override fun onDestroy() {
        super.onDestroy()
        stopStatsRefresh()
        pendingAsyncEnd?.let { mainHandler.removeCallbacks(it) }
        jankStats?.isTrackingEnabled = false
    }

    private companion object {
        const val NANOS_PER_MILLI = 1_000_000L
        const val STATS_REFRESH_INTERVAL_MS = 500L

        /** 单次阻塞时长：约 7 个 60Hz 帧预算，必然触发卡顿帧 */
        const val BLOCK_FRAME_BUDGET_MS = 120L
        const val BURST_TIMES = 5
        const val HEAVY_WORK_MS = 30L
        const val ASYNC_TASK_MS = 800L

        const val KEY_STATS = "jankStatsPanel"
        const val STATE_KEY_PAGE = "Page"
        const val STATE_KEY_MODE = "Mode"

        const val SECTION_INJECT_JANK = "JankStatsDemo:injectJank"
        const val SECTION_HEAVY_WORK = "JankStatsDemo:heavyWork"
        const val SECTION_ASYNC_TASK = "JankStatsDemo:asyncTask"
        const val COUNTER_HEAVY_WORK = "JankStatsDemo:heavyWorkCount"
    }
}
