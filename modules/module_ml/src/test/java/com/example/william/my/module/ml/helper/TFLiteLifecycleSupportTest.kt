package com.example.william.my.module.ml.helper

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

/**
 * 引擎串行队列与「取消即释放」的行为约束
 *
 * 这些逻辑不依赖 Android，可以在纯 JVM 上验证——而它们恰是最难写对的部分：
 * 资源释放发生在「挂起点取消」这条异常路径上，一旦写错就是 96 MB 级的原生实例泄漏，
 * 而且现场不会有任何报错，只能靠这类测试守住。
 */
class TFLiteLifecycleSupportTest {

    private class ProbeResource : AutoCloseable {
        val closed = AtomicBoolean(false)

        override fun close() {
            closed.set(true)
        }
    }

    /**
     * 取消路径：`withContext` 不把返回值交给调用方（改抛取消异常），
     * 此时刚建好的实例已无人持有，必须在 finally 中就势关闭。
     */
    @Test
    fun build_cancelledReleasesBuiltResource() {
        val jobRef = AtomicReference<Job?>(null)
        val resource = ProbeResource()

        val job = CoroutineScope(Dispatchers.Default).launch {
            try {
                buildOffMainThreadSafely(Dispatchers.IO) {
                    // 等外部拿到 job 句柄后再取消，模拟「构建完成时页面已被销毁」
                    while (jobRef.get() == null) {
                        Thread.sleep(1)
                    }
                    jobRef.get()!!.cancel()
                    Thread.sleep(30)
                    resource
                }
            } catch (_: CancellationException) {
                // 取消异常应照常抛出，由调用方的取消语义处理
            }
        }
        jobRef.set(job)
        runBlocking { job.join() }

        assertTrue("取消后已构建完成的原生资源必须被释放", resource.closed.get())
    }

    /** 正常路径：资源交给调用方持有，helper 不得越权关闭 */
    @Test
    fun build_successKeepsResource() = runBlocking {
        val resource = ProbeResource()

        val returned = buildOffMainThreadSafely(Dispatchers.IO) { resource }

        assertSame("应把资源原样交还给调用方", resource, returned)
        assertFalse("正常路径不得关闭资源", resource.closed.get())
    }

    /** factory 自身抛异常：异常原样传播，且不会去关一个根本没建成的对象 */
    @Test
    fun build_factoryExceptionPropagates() {
        var message: String? = null

        runBlocking {
            try {
                buildOffMainThreadSafely<AutoCloseable>(Dispatchers.IO) {
                    throw IllegalStateException("boom")
                }
            } catch (e: Exception) {
                message = e.message
            }
        }

        assertEquals("boom", message)
    }

    /**
     * 关键不变量：`limitedParallelism(1)` 必须真的把并发放成串行。
     *
     * 这正是「连点按钮触发并发 run()」与「close 撞上正在执行的 run()」两类原生竞态的修复基础。
     */
    @Test
    fun engineQueue_singleTaskAtATime() {
        val dispatcher = createEngineDispatcher()
        val running = AtomicInteger(0)
        val peak = AtomicInteger(0)
        val finished = CountDownLatch(8)

        repeat(8) {
            CoroutineScope(dispatcher).launch {
                val now = running.incrementAndGet()
                peak.updateAndGet { maxOf(it, now) }
                Thread.sleep(10)
                running.decrementAndGet()
                finished.countDown()
            }
        }

        assertTrue("任务应在超时前全部完成", finished.await(10, TimeUnit.SECONDS))
        assertEquals("单并发队列不得同时执行两个任务", 1, peak.get())
    }

    /** 关闭顺序由调用方决定：GPU 场景必须「先 delegate 后 interpreter」 */
    @Test
    fun closeEngineOn_closesInOrder() {
        val dispatcher = createEngineDispatcher()
        val order = Collections.synchronizedList(mutableListOf<String>())

        val delegate = object : AutoCloseable {
            override fun close() {
                order += "delegate"
            }
        }
        val interpreter = object : AutoCloseable {
            override fun close() {
                order += "interpreter"
            }
        }

        closeEngineOn(dispatcher, delegate, interpreter)
        drain(dispatcher)

        assertEquals(listOf("delegate", "interpreter"), order)
    }

    /** 单个资源释放失败不应阻断其余资源，否则后面那个就永久泄漏了 */
    @Test
    fun closeEngineOn_failureDoesNotBlockOthers() {
        val dispatcher = createEngineDispatcher()
        val second = ProbeResource()

        val failing = object : AutoCloseable {
            override fun close(): Unit = throw IllegalStateException("close failed")
        }

        closeEngineOn(dispatcher, failing, second)
        drain(dispatcher)

        assertTrue("前一个资源抛异常时，后一个仍必须被关闭", second.closed.get())
    }

    /**
     * 清理必须异步：调用点都在 `onDestroy` / UI 回调里，一旦同步等待就会阻塞主线程。
     *
     * 用「关闭动作被闭锁卡住」把性质变成确定性可观测的：只要实现是异步派发，
     * `closeEngineOn` 就会在 `close()` 完成之前返回；若被改成同步等待，
     * 调用方会阻塞在 `close()` 的 `await` 上，随后的「尚未完成」断言立即失败。
     * （早先用「返回/关闭两个标记的先后顺序」判定，依赖派发慢于同线程下一条语句
     * 这一非保证性时序，高负载 CI 上可能偶发误报。）
     *
     * （取代原先的「全空入参」用例：该用例声称验证的性质在实现中不可观测，
     * `filterNotNull()` 与空集短路的存在与否不改变任何可观察行为，任何断言都恒定成立。）
     */
    @Test
    fun closeEngineOn_returnsBeforeCleanup() {
        val dispatcher = createEngineDispatcher()
        val completed = AtomicBoolean(false)
        val allowClose = CountDownLatch(1)

        val resource = object : AutoCloseable {
            override fun close() {
                // 卡住关闭动作，直到调用方显式放行；同步实现会阻塞在这一行
                allowClose.await(2, TimeUnit.SECONDS)
                completed.set(true)
            }
        }

        closeEngineOn(dispatcher, resource)
        assertFalse(
            "调用必须早于清理完成返回，否则 onDestroy 中会阻塞主线程",
            completed.get(),
        )

        // 放行关闭动作，再排空队列确认它确实能正常跑完
        allowClose.countDown()
        drain(dispatcher)
        assertTrue("放行后清理应能正常完成", completed.get())
    }

    /**
     * 清理任务的异常绝不能逃逸到线程默认处理器——在 Android 上那就是直接闪退。
     * 清理失败只该留下一条日志，不该把进程带走。
     */
    @Test
    fun engineQueue_cleanupExceptionStaysContained() {
        val dispatcher = createEngineDispatcher()
        val escaped = AtomicReference<Throwable?>(null)
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { _, e -> escaped.set(e) }
        try {
            runOnEngineQueue(dispatcher) {
                throw IllegalStateException("cleanup boom")
            }
            drain(dispatcher)
        } finally {
            Thread.setDefaultUncaughtExceptionHandler(previousHandler)
        }

        assertNull("清理任务的异常若逃逸到默认处理器会直接杀进程", escaped.get())
    }

    /**
     * 用同一条队列提交一个空任务并等它跑完，
     * 即可保证此前提交的清理任务都已执行完毕（单并发队列是 FIFO 的）。
     */
    private fun drain(dispatcher: kotlinx.coroutines.CoroutineDispatcher) = runBlocking {
        withContext(dispatcher) { /* 排空队列 */ }
    }
}
