package com.example.william.my.module.ml.helper

import android.app.Activity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

/**
 * 端侧推理页面的生命周期闸门
 *
 * 背景一（崩溃）：`BaseVBActivity` 会先把 `binding` 置空、再调用 `super.onDestroy()`，而
 * `lifecycleScope` 的取消恰好发生在同一次 `super.onDestroy()` 内。于是「页面退出时仍在后台跑的任务」
 * 恢复后拿到的是已经为 null 的 binding：
 *
 * ```
 * try {
 *     withContext(Dispatchers.Default) { CompiledModel.create(...) }  // 页面在此期间被销毁
 * } catch (e: Exception) {                                            // 取消异常同样走进这里
 *     binding.tvStatus.text = "..."                                   // binding 已置空 → NPE → 崩溃
 * }
 * ```
 *
 * 背景二（原生竞态）：`CompiledModel` 底层的解释器**不是线程安全**的，LiteRT 的
 * `run()` / `close()` 都没有同步保护，`close()` 会释放 `run()` 正在读取的原生 handle。
 * 因此「同一实例并发 run()」与「run() 执行期间被 close()」都会踩到已释放的原生内存。
 * 单纯在 `onDestroy` 里把字段置 null 挡不住——各页都用局部快照持有实例，后台那个 run() 照跑。
 *
 * 因此本模块遵守三条纪律：
 * 1. 任何 UI 写入前先过 [canTouchUi]，不再假设 binding 一定可用；
 * 2. 引擎构建统一走 [buildOffMainThreadSafely]，构建完成但调用方已被取消时就地释放，
 *    不让 MB 级的原生实例只靠 GC 兜底；
 * 3. 推理、引擎构建与引擎关闭共用 [createEngineDispatcher] 这条单并发队列，
 *    避免并发 run 与「close 掉正在推理的实例」两类原生竞态。
 */

/** 页面尚未进入销毁流程时才允许写 UI */
internal val Activity.canTouchUi: Boolean
    get() = !isFinishing && !isDestroyed

/**
 * 创建一条「同一时刻只跑一件事」的引擎队列
 *
 * `limitedParallelism(1)` 同时提供互斥与顺序保证：把推理、引擎构建、引擎关闭都排进来，
 * 三者天然互斥且按提交顺序执行。注意这是**每个页面实例各自持有**的队列，不会跨页面互相阻塞。
 *
 * 用 `Mutex` 达不到同样效果——它只保证互斥，关闭动作可能插到两次推理中间。
 */
internal fun createEngineDispatcher(): CoroutineDispatcher = Dispatchers.Default.limitedParallelism(1)

/**
 * 把任意清理动作排进引擎串行队列
 *
 * 适用于不实现 [AutoCloseable] 的原生资源，例如 `Bitmap`（它只有 `recycle()`）：
 * 后台的预处理正在 `getPixels()` 时若主线程 `recycle()`，同样会踩到已释放的像素内存。
 * 与 [closeEngineOn] 共用同一条队列，因此「先关引擎、再回收位图」的顺序由提交顺序保证。
 *
 * 另起独立作用域派发：调用点都在 `onDestroy` 或 UI 回调里，此刻 `lifecycleScope` 已被取消，
 * 用它派发会被静默丢弃、资源就永久泄漏了；这条新作用域的 Job 无人取消。
 *
 * 清理失败只记录、不外抛：异常若逃逸到线程默认处理器会直接杀进程（Android 上即闪退），
 * 而清理失败本身不该让 App 崩——与 [closeEngineOn] 逐个资源隔离的容错策略保持一致。
 */
internal fun runOnEngineQueue(
    dispatcher: CoroutineDispatcher,
    block: () -> Unit,
) {
    CoroutineScope(dispatcher).launch {
        try {
            block()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

/**
 * 在页面销毁时安全释放原生引擎资源
 *
 * 必须排进与推理同一条队列：直接在主线程 `close()` 会释放后台 `run()` 正在用的原生 handle
 * （`onDestroy` 只取消了 `lifecycleScope`，而 `run()` 不可中断，仍会跑完）。
 * 关闭动作另起独立作用域派发——此刻 `lifecycleScope` 已经被取消，用它派发会被静默丢弃，
 * 那样原生实例就永久泄漏了。
 *
 * @param resources 按**关闭顺序**传入；[TensorBuffer] 应先于 [CompiledModel] 关闭。
 */
internal fun closeEngineOn(
    dispatcher: CoroutineDispatcher,
    vararg resources: AutoCloseable?,
) {
    val closing = resources.filterNotNull()
    if (closing.isEmpty()) return
    runOnEngineQueue(dispatcher) {
        closing.forEach { resource ->
            try {
                resource.close()
            } catch (e: Exception) {
                // 单个资源释放失败不应阻断其余资源，否则后面那个就永久泄漏了
                e.printStackTrace()
            }
        }
    }
}

/**
 * 通用版「后台构建 + 取消即释放」
 *
 * 适用于任何需要长时间构建的原生资源（`CompiledModel`、`TensorBuffer` 及其组合）。
 * 注意 [AutoCloseable.close] 的调用顺序由资源自身决定。
 */
internal suspend fun <T : AutoCloseable> buildOffMainThreadSafely(
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    factory: () -> T,
): T {
    var created: T? = null
    try {
        return withContext(dispatcher) { factory().also { created = it } }
    } finally {
        if (!coroutineContext.isActive) {
            created?.close()
        }
    }
}
