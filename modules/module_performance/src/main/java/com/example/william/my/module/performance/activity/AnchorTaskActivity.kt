package com.example.william.my.module.performance.activity

import android.os.Bundle
import android.os.SystemClock
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.performance.task.ApmBackgroundTask
import com.example.william.my.module.performance.task.IAnchorTask
import com.example.william.my.module.performance.task.InitLogTask
import com.example.william.my.module.performance.task.InitSecurityTask
import com.example.william.my.module.performance.task.PushAnchorTask
import com.example.william.my.module.performance.task.StartupTask
import com.example.william.my.module.performance.task.TaskDispatcher
import com.example.william.my.module.performance.task.TaskListener
import com.example.william.my.module.performance.task.UserConfigAnchorTask

/**
 * AnchorTask — 启动任务 DAG 并发编排与锚点卡点等待
 *
 * 核心机制与避坑点：
 * 1. DAG 拓扑排序：按任务 dependencies 依赖关系构建邻接表与入度，入度为 0 时立即自动派发；
 * 2. 主/子线程分流：UI 强相关任务分配在主线程，耗时 I/O 与计算任务派发到线程池并发，榨干多核 CPU；
 * 3. Anchor 锚点阻断机制：基于 CountDownLatch 仅阻塞主线程等待标有 [IAnchorTask] 的关键任务（如核心配置、推送通道），非关键任务持续在后台运行，兼顾核心数据就绪与主线程极速放行。
 */
@Route(path = RouterPath.Performance.AnchorTask)
class AnchorTaskActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription(
            """
            AnchorTask 启动任务编排与主线程锚点卡点：
            • DAG 拓扑流转：依赖就绪后自动分发至对应主/子线程池；
            • Anchor 锚点卡点：CountDownLatch 仅等待核心任务，缩短主线程阻塞时间；
            • 与 App Startup 对照：突破官方组件纯主线程串行的局限，实现真多线程并发。
            """.trimIndent(),
        )
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 串行基准执行 (模拟传统单线程依次初始化)",
        "2. DAG 并发编排 + Anchor 锚点等待 (推荐企业级解法)",
        "3. DAG 并发编排 (无 Anchor 纯异步完全放行)",
        "4. 核心架构对比：App Startup vs AnchorTask 深度分析",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> runSerialExecution()
            1 -> runDagWithAnchorWait()
            2 -> runDagWithoutAnchor()
            3 -> showArchitectureComparison()
        }
    }

    /**
     * 1. 模拟传统单线程在主线程串行依次初始化所有任务
     */
    private fun runSerialExecution() {
        appendLog("→ 正在开始单线程串行执行（所有任务依次阻塞主线程）...")
        val startTime = SystemClock.uptimeMillis()

        val tasks = listOf(
            InitLogTask(sleepMs = 15L),
            InitSecurityTask(sleepMs = 50L),
            UserConfigAnchorTask(sleepMs = 80L),
            PushAnchorTask(sleepMs = 100L),
            ApmBackgroundTask(sleepMs = 180L),
        )

        for (task in tasks) {
            val taskStart = SystemClock.uptimeMillis()
            task.execute()
            val cost = SystemClock.uptimeMillis() - taskStart
            appendLog("  • 串行完成: [${task.id}], 耗时: ${cost}ms (运行于 ${Thread.currentThread().name})")
        }

        val totalCost = SystemClock.uptimeMillis() - startTime
        appendLog("✓ 串行执行完毕！主线程被完全阻塞，总耗时 = ${totalCost}ms\n")
    }

    /**
     * 2. DAG 多线程并发编排 + Anchor 锚点等待
     *
     * 仅将 UserConfigAnchorTask 与 PushAnchorTask 设为锚点。
     * 主线程在 start().await() 处仅等待锚点任务就绪即放行，次要的 ApmBackgroundTask 在后台继续执行。
     */
    private fun runDagWithAnchorWait() {
        appendLog("→ 启动 DAG 并发编排（启用 Anchor 锚点等待）...")

        val listener = object : TaskListener {
            override fun onTaskStart(task: StartupTask, threadName: String) {
                runOnUiThread {
                    appendLog("  → 任务启动: [${task.id}] 运行于 $threadName")
                }
            }

            override fun onTaskFinish(task: StartupTask, threadName: String, costTimeMs: Long) {
                runOnUiThread {
                    val anchorBadge = if (task is IAnchorTask) "[Anchor锚点]" else "[普通后台]"
                    appendLog("  ✓ 任务完成: [${task.id}] $anchorBadge 耗时 ${costTimeMs}ms ($threadName)")
                }
            }

            override fun onAnchorCompleted(totalAnchorCount: Int, waitCostTimeMs: Long) {
                runOnUiThread {
                    appendLog("✓ [主线程放行] 所有 Anchor 任务 ($totalAnchorCount 个) 已全部就绪，主线程仅等待 ${waitCostTimeMs}ms！")
                }
            }

            override fun onAllCompleted(totalCostTimeMs: Long) {
                runOnUiThread {
                    appendLog("✓ [全部完毕] 全局所有任务（含后台非关键任务）执行完成，端到端总跨度 = ${totalCostTimeMs}ms\n")
                }
            }
        }

        val dispatcher = TaskDispatcher.newBuilder()
            .addTask(InitLogTask(sleepMs = 15L))
            .addTask(InitSecurityTask(sleepMs = 50L))
            .addTask(UserConfigAnchorTask(sleepMs = 80L))
            .addTask(PushAnchorTask(sleepMs = 100L))
            .addTask(ApmBackgroundTask(sleepMs = 180L))
            .setAnchorEnabled(true)
            .setListener(listener)
            .build()

        // 启动调度并阻塞等待所有 Anchor 任务完成
        dispatcher.start()
        dispatcher.await()
    }

    /**
     * 3. DAG 多线程并发编排（无 Anchor 纯异步完全放行主线程）
     */
    private fun runDagWithoutAnchor() {
        appendLog("→ 启动 DAG 并发编排（禁用 Anchor，主线程立即放行）...")

        val listener = object : TaskListener {
            override fun onTaskStart(task: StartupTask, threadName: String) {
                runOnUiThread {
                    appendLog("  → 任务启动: [${task.id}] 运行于 $threadName")
                }
            }

            override fun onTaskFinish(task: StartupTask, threadName: String, costTimeMs: Long) {
                runOnUiThread {
                    appendLog("  ✓ 任务完成: [${task.id}] 耗时 ${costTimeMs}ms ($threadName)")
                }
            }

            override fun onAnchorCompleted(totalAnchorCount: Int, waitCostTimeMs: Long) {
                // 无锚点
            }

            override fun onAllCompleted(totalCostTimeMs: Long) {
                runOnUiThread {
                    appendLog("✓ [全部完毕] 纯异步 DAG 任务已全部就绪，端到端总耗时 = ${totalCostTimeMs}ms\n")
                }
            }
        }

        val dispatcher = TaskDispatcher.newBuilder()
            .addTask(InitLogTask(sleepMs = 15L))
            .addTask(InitSecurityTask(sleepMs = 50L))
            .addTask(UserConfigAnchorTask(sleepMs = 80L))
            .addTask(PushAnchorTask(sleepMs = 100L))
            .addTask(ApmBackgroundTask(sleepMs = 180L))
            .setAnchorEnabled(false)
            .setListener(listener)
            .build()

        val start = SystemClock.uptimeMillis()
        dispatcher.start()
        dispatcher.await()
        val waitCost = SystemClock.uptimeMillis() - start
        appendLog("✓ [主线程立即放行] 无锚点阻塞，主线程消耗调度时间 = ${waitCost}ms")
    }

    /**
     * 4. App Startup 与 AnchorTask 核心架构对比
     */
    private fun showArchitectureComparison() {
        appendLog(
            """
            ── Jetpack App Startup vs AnchorTask 深度对比 ──
            1. 核心定位差异：
               • App Startup：官方推出的 ContentProvider 聚合工具，旨在解决第三方库滥用 Provider 导致的无形开销。
               • AnchorTask：企业级冷启动任务编排调度引擎，旨在解决 Application 阶段数十上百个任务的并发调度与依赖治理。
            2. 线程与并发模型：
               • App Startup：默认所有 Initializer 均在主线程串行执行，无法分发至异步线程池并发。
               • AnchorTask：支持主线程与后台线程池混合并发执行，入度解算自动触发后继，充分压榨多核 CPU。
            3. 卡点阻断能力 (Anchor 机制)：
               • App Startup：无卡点概念，要么随启动串行完成，要么业务代码后续手动 initializeComponent 懒加载。
               • AnchorTask：基于 CountDownLatch 原语设置锚点，主线程仅等待关键业务任务就绪即放行首屏，非关键任务在后台持续并发，极大缩短用户首帧等待白屏时间。
            4. 推荐落地范式：
               • 启动前阶段 (Provider 阶段)：使用 App Startup 聚合轻量库；
               • 启动中阶段 (Application.onCreate 阶段)：使用 AnchorTask 进行多线程并发编排与关键数据锚点卡点；
               • 首屏渲染后 (空闲阶段)：配合 IdleHandler 延迟初始化次要模块。
            """.trimIndent(),
        )
    }
}
