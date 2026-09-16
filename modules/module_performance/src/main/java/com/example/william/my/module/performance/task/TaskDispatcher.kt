package com.example.william.my.module.performance.task

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * 启动任务核心抽象接口
 */
interface StartupTask {
    /**
     * 任务唯一标识（用于日志追踪与分析）
     */
    val id: String

    /**
     * 是否在主线程运行。若为 false，则由调度器派发至并发线程池执行。
     */
    val isRunOnMainThread: Boolean get() = false

    /**
     * 当前任务所依赖的前置任务类型列表。
     * 只有所有前置依赖任务均执行完毕后，当前任务才会被调度触发。
     */
    fun dependencies(): List<Class<out StartupTask>> = emptyList()

    /**
     * 任务实际业务逻辑执行体
     */
    fun execute()
}

/**
 * 锚点任务标记接口（Anchor Task）
 *
 * 声明为此接口的任务，即使运行在后台异步线程，调度器在执行 [TaskDispatcher.await] 时
 * 也会强制阻塞等待其完成，常用于首屏展示前必须就绪的关键配置或核心 SDK。
 */
interface IAnchorTask : StartupTask

/**
 * 任务调度事件监听器
 */
interface TaskListener {
    fun onTaskStart(task: StartupTask, threadName: String)
    fun onTaskFinish(task: StartupTask, threadName: String, costTimeMs: Long)
    fun onAnchorCompleted(totalAnchorCount: Int, waitCostTimeMs: Long)
    fun onAllCompleted(totalCostTimeMs: Long)
}

/**
 * 轻量级 DAG 多线程启动编排调度器
 *
 * 核心机制：
 * 1. DAG 拓扑排序与入度解算：基于依赖关系构建邻接表与入度表，确保前置任务必然先于后继任务完成；
 * 2. 主/子线程协同派发：主线程任务安全分发，I/O 与耗时计算任务交由后台线程池并行执行；
 * 3. 锚点卡点（Anchor Wait）：基于 [CountDownLatch] 阻塞主线程等待关键异步任务，非关键任务持续在后台运行。
 */
class TaskDispatcher private constructor(
    private val taskList: List<StartupTask>,
    private val isAnchorEnabled: Boolean,
    private val listener: TaskListener?,
) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val executorService: ExecutorService = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors().coerceAtLeast(2),
    )

    // 类类型到实例的映射
    private val taskMap = HashMap<Class<out StartupTask>, StartupTask>()

    // 每个任务的剩余入度计数（前置依赖尚未完成的数量）
    private val inDegreeMap = ConcurrentHashMap<Class<out StartupTask>, AtomicInteger>()

    // 后继依赖映射：任务 A 完成后，需要通知哪些后继任务
    private val dependentsMap = HashMap<Class<out StartupTask>, MutableList<Class<out StartupTask>>>()

    // 锚点计数锁
    private var anchorLatch: CountDownLatch? = null
    private val anchorTaskCount: Int

    // 统计变量
    private val remainingTasks = AtomicInteger(taskList.size)
    private var startTimeMillis: Long = 0L

    init {
        for (task in taskList) {
            taskMap[task.javaClass] = task
            dependentsMap[task.javaClass] = mutableListOf()
        }

        // 构建邻接表与初始入度表
        for (task in taskList) {
            val deps = task.dependencies()
            inDegreeMap[task.javaClass] = AtomicInteger(deps.size)
            for (dep in deps) {
                dependentsMap[dep]?.add(task.javaClass)
            }
        }

        // 筛选锚点任务
        val anchorTasks = if (isAnchorEnabled) {
            taskList.filterIsInstance<IAnchorTask>()
        } else {
            emptyList()
        }
        anchorTaskCount = anchorTasks.size
        if (anchorTaskCount > 0) {
            anchorLatch = CountDownLatch(anchorTaskCount)
        }
    }

    /**
     * 启动调度器并分发所有无前置依赖（入度为 0）的初始任务
     */
    fun start(): TaskDispatcher {
        startTimeMillis = SystemClock.uptimeMillis()

        // 查找所有入度为 0 的起始任务
        val zeroInDegreeTasks = taskList.filter {
            (inDegreeMap[it.javaClass]?.get() ?: 0) == 0
        }

        if (zeroInDegreeTasks.isEmpty() && taskList.isNotEmpty()) {
            throw IllegalStateException("StartupTask DAG 存在循环依赖，找不到入度为 0 的初始任务！")
        }

        for (task in zeroInDegreeTasks) {
            dispatchTask(task)
        }
        return this
    }

    /**
     * 阻塞当前调用线程（通常是主线程），直到所有锚点任务完成或超时
     */
    fun await(timeoutMs: Long = 10_000L): Boolean {
        val latch = anchorLatch ?: return true
        val awaitStart = SystemClock.uptimeMillis()
        val success = latch.await(timeoutMs, TimeUnit.MILLISECONDS)
        val waitCost = SystemClock.uptimeMillis() - awaitStart
        listener?.onAnchorCompleted(anchorTaskCount, waitCost)
        return success
    }

    /**
     * 派发单个任务到目标线程执行
     */
    private fun dispatchTask(task: StartupTask) {
        val runnable = Runnable {
            val threadName = Thread.currentThread().name
            listener?.onTaskStart(task, threadName)

            val taskStart = SystemClock.uptimeMillis()
            task.execute()
            val costTime = SystemClock.uptimeMillis() - taskStart

            listener?.onTaskFinish(task, threadName, costTime)

            // 若当前任务是锚点任务，递减锚点计数
            if (isAnchorEnabled && task is IAnchorTask) {
                anchorLatch?.countDown()
            }

            // 通知后继任务
            notifyDependents(task)

            // 检查全局是否全部完成
            if (remainingTasks.decrementAndGet() == 0) {
                val totalCost = SystemClock.uptimeMillis() - startTimeMillis
                listener?.onAllCompleted(totalCost)
                executorService.shutdown()
            }
        }

        if (task.isRunOnMainThread) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                runnable.run()
            } else {
                mainHandler.post(runnable)
            }
        } else {
            executorService.execute(runnable)
        }
    }

    /**
     * 当任务完成后，解锁其后继依赖任务
     */
    private fun notifyDependents(completedTask: StartupTask) {
        val dependents = dependentsMap[completedTask.javaClass] ?: return
        for (dependentClass in dependents) {
            val counter = inDegreeMap[dependentClass] ?: continue
            if (counter.decrementAndGet() == 0) {
                val nextTask = taskMap[dependentClass]
                if (nextTask != null) {
                    dispatchTask(nextTask)
                }
            }
        }
    }

    class Builder {
        private val tasks = mutableListOf<StartupTask>()
        private var isAnchorEnabled: Boolean = true
        private var listener: TaskListener? = null

        fun addTask(task: StartupTask): Builder = apply {
            tasks.add(task)
        }

        fun setAnchorEnabled(enabled: Boolean): Builder = apply {
            this.isAnchorEnabled = enabled
        }

        fun setListener(listener: TaskListener): Builder = apply {
            this.listener = listener
        }

        fun build(): TaskDispatcher = TaskDispatcher(tasks, isAnchorEnabled, listener)
    }

    companion object {
        fun newBuilder(): Builder = Builder()
    }
}

// ──────────────────────── 模拟实际业务初始化的典型任务组 ────────────────────────

/**
 * 1. 日志 SDK 初始化（主线程执行，耗时 15ms，根节点无依赖）
 */
class InitLogTask(private val sleepMs: Long = 15L) : StartupTask {
    override val id: String = "InitLogTask"
    override val isRunOnMainThread: Boolean = true

    override fun execute() {
        SystemClock.sleep(sleepMs)
    }
}

/**
 * 2. 设备安全与指纹 SDK（异步子线程执行，耗时 50ms，依赖 InitLogTask）
 */
class InitSecurityTask(private val sleepMs: Long = 50L) : StartupTask {
    override val id: String = "InitSecurityTask"
    override val isRunOnMainThread: Boolean = false

    override fun dependencies(): List<Class<out StartupTask>> = listOf(InitLogTask::class.java)

    override fun execute() {
        SystemClock.sleep(sleepMs)
    }
}

/**
 * 3. 核心用户配置与权限信息（锚点任务，必须在首屏展示前就绪，异步耗时 80ms，依赖 InitSecurityTask）
 */
class UserConfigAnchorTask(private val sleepMs: Long = 80L) : IAnchorTask {
    override val id: String = "UserConfigAnchorTask"
    override val isRunOnMainThread: Boolean = false

    override fun dependencies(): List<Class<out StartupTask>> = listOf(InitSecurityTask::class.java)

    override fun execute() {
        SystemClock.sleep(sleepMs)
    }
}

/**
 * 4. 推送通道初始化（锚点任务，长链接建立，异步耗时 100ms，依赖 InitLogTask）
 */
class PushAnchorTask(private val sleepMs: Long = 100L) : IAnchorTask {
    override val id: String = "PushAnchorTask"
    override val isRunOnMainThread: Boolean = false

    override fun dependencies(): List<Class<out StartupTask>> = listOf(InitLogTask::class.java)

    override fun execute() {
        SystemClock.sleep(sleepMs)
    }
}

/**
 * 5. 全局 APM 监控上报 SDK（次要非锚点任务，无需阻塞首屏，异步耗时 180ms，依赖 InitLogTask）
 */
class ApmBackgroundTask(private val sleepMs: Long = 180L) : StartupTask {
    override val id: String = "ApmBackgroundTask"
    override val isRunOnMainThread: Boolean = false

    override fun dependencies(): List<Class<out StartupTask>> = listOf(InitLogTask::class.java)

    override fun execute() {
        SystemClock.sleep(sleepMs)
    }
}
