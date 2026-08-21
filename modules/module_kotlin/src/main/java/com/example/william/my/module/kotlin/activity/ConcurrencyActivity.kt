package com.example.william.my.module.kotlin.activity

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger

/**
 * Kotlin 协程高阶并发控制与非阻塞同步
 *
 * 演示特性：
 * 1. Mutex 互斥锁：非阻塞挂起锁，避免 Java synchronized 导致的线程阻塞与死锁
 * 2. Semaphore 信号量：高并发任务最大并行度限流
 * 3. select 多路复用：竞速模式，谁先就绪优先响应
 * 4. NonCancellable 上下文：协程取消后保证关键清理/上报逻辑完整执行
 *
 * https://kotlinlang.org/docs/shared-mutable-state-and-concurrency.html
 */
@Route(path = RouterPath.Kotlin.Concurrency)
class ConcurrencyActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("演示 Kotlin 协程高阶并发控制：Mutex 互斥锁、Semaphore 限流、select 竞速与 NonCancellable 清理")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "1. 协程非阻塞互斥锁（Mutex.withLock 保护共享状态）",
            "2. 信号量限流调度（Semaphore 控制最大并发数）",
            "3. select 多路复用选择器（异步竞速响应）",
            "4. 不可取消资源释放（withContext(NonCancellable)）"
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> testMutex()
            1 -> testSemaphore()
            2 -> testSelect()
            3 -> testNonCancellable()
        }
    }

    // ─────────────────────────────────────────────
    // 1. Mutex 非阻塞互斥锁
    // ─────────────────────────────────────────────
    private fun testMutex() {
        lifecycleScope.launch(Dispatchers.Default) {
            val mutex = Mutex()
            var unsafeCounter = 0
            var safeCounter = 0
            val jobsCount = 1000

            appendLog("【1. Mutex 互斥锁】启动 $jobsCount 个高并发协程累加计数器...")

            // 1. 无锁并发累加（存在竞态条件）
            val unsafeJobs = List(jobsCount) {
                launch {
                    unsafeCounter++
                }
            }
            unsafeJobs.joinAll()

            // 2. 使用 Mutex.withLock 保护临界区
            val safeJobs = List(jobsCount) {
                launch {
                    mutex.withLock {
                        safeCounter++
                    }
                }
            }
            safeJobs.joinAll()

            appendLog("【1. Mutex 对比】无保护累加结果: $unsafeCounter (可能丢数), Mutex 保护结果: $safeCounter (精确无误)")
            appendLog("【1. Mutex 原理】Mutex 是挂起锁（挂起协程而非阻塞线程），完全杜绝了 Android UI 线程卡死与线程池饥饿。")
        }
    }

    // ─────────────────────────────────────────────
    // 2. Semaphore 信号量限流
    // ─────────────────────────────────────────────
    private fun testSemaphore() {
        lifecycleScope.launch {
            val maxConcurrency = 3
            val semaphore = Semaphore(permits = maxConcurrency)
            val runningCount = AtomicInteger(0)

            appendLog("【2. Semaphore 限流】同时提交 6 个下载任务，限制最大并行度 = $maxConcurrency...")

            val downloadJobs = List(6) { index ->
                val taskId = index + 1
                launch(Dispatchers.IO) {
                    semaphore.withPermit {
                        val current = runningCount.incrementAndGet()
                        appendLog("【任务 #$taskId 开始】进入执行区 (当前活跃并发数: $current / $maxConcurrency)")
                        delay(300) // 模拟下载耗时
                        runningCount.decrementAndGet()
                        appendLog("【任务 #$taskId 完成】释放信号量许可")
                    }
                }
            }

            downloadJobs.joinAll()
            appendLog("【2. Semaphore 限流】所有任务均在限流阈值内平稳执行完毕。")
        }
    }

    // ─────────────────────────────────────────────
    // 3. select 多路复用竞速
    // ─────────────────────────────────────────────
    private fun testSelect() {
        lifecycleScope.launch {
            appendLog("【3. select 竞速】同时发起 Cache 快速拉取 (耗时 100ms) 与 Network 慢速请求 (耗时 300ms)...")

            val cacheDeferred = async(Dispatchers.IO) {
                delay(100)
                "来自本地缓存的数据 (Hit Local Cache)"
            }

            val networkDeferred = async(Dispatchers.IO) {
                delay(300)
                "来自云端服务器的数据 (Server Response)"
            }

            // 使用 select 监听最先完成的 Deferred
            val winnerResult = select<String> {
                cacheDeferred.onAwait { result ->
                    "最快就绪通道: $result"
                }
                networkDeferred.onAwait { result ->
                    "最快就绪通道: $result"
                }
            }

            appendLog("【3. select 竞速结果】$winnerResult")
        }
    }

    // ─────────────────────────────────────────────
    // 4. NonCancellable 不可取消资源清理
    // ─────────────────────────────────────────────
    private fun testNonCancellable() {
        lifecycleScope.launch {
            appendLog("【4. NonCancellable】启动一个长生命周期任务并在 150ms 后主动取消它...")

            val job = launch(Dispatchers.IO) {
                try {
                    appendLog("【工作协程】正在执行核心业务逻辑...")
                    delay(1000)
                    appendLog("【工作协程】正常执行结束")
                } finally {
                    appendLog("【finally 清理】检测到协程取消状态，执行关键资源释放...")

                    // 在已取消的协程中使用 withContext(NonCancellable) 允许挂起操作继续执行
                    withContext(NonCancellable) {
                        delay(200) // 模拟网络 session 关闭或文件句柄 flush 挂起耗时
                        appendLog("【finally 清理】withContext(NonCancellable) 成功完成远端 Session 登出与资源回收！")
                    }
                }
            }

            delay(150)
            appendLog("【外部调度】主动取消工作协程 -> job.cancelAndJoin()")
            job.cancelAndJoin()
            appendLog("【外部调度】工作协程生命周期已彻底安全结束。")
        }
    }
}
