package com.example.william.my.module.reactive.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.coroutine.launchAndRepeatWithLifecycle
import com.example.william.my.module.reactive.samples.FALLBACK_NUMBER
import com.example.william.my.module.reactive.samples.NumberSource
import com.example.william.my.module.reactive.samples.bufferFlow
import com.example.william.my.module.reactive.samples.catchFlow
import com.example.william.my.module.reactive.samples.combineFlow
import com.example.william.my.module.reactive.samples.createFlow
import com.example.william.my.module.reactive.samples.filterFlow
import com.example.william.my.module.reactive.samples.flatMapConcatFlow
import com.example.william.my.module.reactive.samples.mapFlow
import com.example.william.my.module.reactive.samples.retryFlow
import com.example.william.my.module.reactive.samples.zipFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach

/**
 * Kotlin Flow — 响应式数据流操作符
 *
 * Flow 是 Kotlin 协程的响应式数据流，与 RxJava 操作符一一对应。
 *
 * 核心特性：
 * 1. 创建操作符：flowOf / asFlow 快速构建冷数据流
 * 2. 变换操作符：map 逐项映射、flatMapConcat 扁平展开、buffer 背压缓冲
 * 3. 过滤操作符：filter 条件过滤、take 限量、distinct 去重
 * 4. 组合操作符：zip 严格配对、combine 最新值组合
 * 5. 错误恢复：catch 捕获上游异常并降级
 * 6. 错误重试：retry 重新订阅上游
 *
 * 操作符管道集中在 `samples` 包，页面只负责收集与日志渲染，
 * 同一份管道可被单元测试直接用 Turbine 逐项断言。
 *
 * 本页与同模块 RxJavaOperatorsActivity 分组一一对应，便于对照学习。
 *
 * https://developer.android.google.cn/kotlin/flow
 */
@Route(path = RouterPath.Reactive.FlowOperators)
class FlowOperatorsActivity : BasicResponseActivity() {

    /**
     * 易失败数据源：每次订阅都在发射首项后立即抛异常，
     * 用于演示 retry 的重新订阅语义与 catch 的降级兜底。
     */
    private val flakySource = NumberSource {
        flow {
            emit(1)
            throw RuntimeException(ERROR_MESSAGE)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("演示 Kotlin Flow 核心操作符流式处理（创建 / 变换 / 过滤 / 组合 / 错误恢复 / 重试）")
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "创建操作符（flowOf / asFlow）",
        "变换操作符（map / flatMapConcat / buffer）",
        "过滤操作符（filter / take / distinct）",
        "组合操作符（zip / combine）",
        "错误恢复（catch 降级）",
        "错误重试（retry 重新订阅）",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> testCreateOperators()
            1 -> testTransformOperators()
            2 -> testFilterOperators()
            3 -> testCombineOperators()
            4 -> testErrorOperators()
            5 -> testRetryOperators()
        }
    }

    // ─────────────────────────────────────────────
    // 1. 创建操作符
    // ─────────────────────────────────────────────
    private fun testCreateOperators() {
        launchAndRepeatWithLifecycle {
            createFlow().collect { appendLog("【创建操作符 asFlow】onNext: $it") }
            appendLog("【创建操作符】collect 完成")
        }
    }

    // ─────────────────────────────────────────────
    // 2. 变换操作符
    // ─────────────────────────────────────────────
    private fun testTransformOperators() {
        launchAndRepeatWithLifecycle {
            mapFlow(flowOf("apple", "banana")).collect { appendLog("【变换操作符 map】大写转换: $it") }
            appendLog("【变换操作符】map 完成")

            flatMapConcatFlow(flowOf(1, 2)).collect { appendLog("【变换操作符 flatMapConcat】展开结果: $it") }
            appendLog("【变换操作符】flatMapConcat 完成")

            bufferFlow(flowOf(1, 2, 3)).collect { appendLog("【变换操作符 buffer】缓冲接收: $it") }
            appendLog("【变换操作符】buffer 完成")
        }
    }

    // ─────────────────────────────────────────────
    // 3. 过滤操作符
    // ─────────────────────────────────────────────
    private fun testFilterOperators() {
        launchAndRepeatWithLifecycle {
            filterFlow(flowOf(1, 2, 2, 3, 4, 1, 5))
                .collect { appendLog("【过滤操作符 distinctUntilChanged+filter+take】接收: $it") }
            appendLog("【过滤操作符】collect 完成")
        }
    }

    // ─────────────────────────────────────────────
    // 4. 组合操作符
    // ─────────────────────────────────────────────
    private fun testCombineOperators() {
        launchAndRepeatWithLifecycle {
            zipFlow(flowOf("Android", "Kotlin"), flowOf("14", "2.0"))
                .collect { appendLog("【组合操作符 zip】配对结果: $it") }
            appendLog("【组合操作符】zip 完成")

            combineFlow(flowOf(1, 2, 3), flowOf("a", "b"))
                .collect { appendLog("【组合操作符 combine】最新组合: $it") }
            appendLog("【组合操作符】combine 完成")
        }
    }

    // ─────────────────────────────────────────────
    // 5. 错误恢复操作符
    // ─────────────────────────────────────────────
    private fun testErrorOperators() {
        launchAndRepeatWithLifecycle {
            catchFlow(flakySource).collect { value ->
                val suffix = if (value == FALLBACK_NUMBER) "（catch 已捕获异常并降级）" else ""
                appendLog("【错误恢复 catch】收到数据: $value$suffix")
            }
            appendLog("【错误恢复】collect 平稳结束")
        }
    }

    // ─────────────────────────────────────────────
    // 6. 错误重试操作符
    // ─────────────────────────────────────────────
    private fun testRetryOperators() {
        launchAndRepeatWithLifecycle {
            retryFlow(flakySource, RETRY_MAX_ATTEMPTS)
                .onEach { appendLog("【错误重试 retry】重新订阅后收到: $it") }
                .catch { appendLog("【错误重试】重试 $RETRY_MAX_ATTEMPTS 次后仍失败: ${it.message}") }
                .collect { }
        }
    }

    private companion object {
        /** 演示中重试的最多尝试次数 */
        const val RETRY_MAX_ATTEMPTS = 2
        const val ERROR_MESSAGE = "模拟下游数据异常"
    }
}
