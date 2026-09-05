package com.example.william.my.module.reactive.activity

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch

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
 *
 * 本页与同模块 RxJavaOperatorsActivity 分组一一对应，便于对照学习。
 *
 * https://developer.android.google.cn/kotlin/flow
 */
@Route(path = RouterPath.Reactive.FlowOperators)
class FlowOperatorsActivity : BasicResponseActivity() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("演示 Kotlin Flow 核心操作符流式处理（创建 / 变换 / 过滤 / 组合 / 错误恢复）")
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "创建操作符（flowOf / asFlow）",
        "变换操作符（map / flatMapConcat / buffer）",
        "过滤操作符（filter / take / distinct）",
        "组合操作符（zip / combine）",
        "错误恢复（catch 降级）",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> testCreateOperators()
            1 -> testTransformOperators()
            2 -> testFilterOperators()
            3 -> testMergeOperators()
            4 -> testErrorOperators()
        }
    }

    // ─────────────────────────────────────────────
    // 1. 创建操作符
    // ─────────────────────────────────────────────
    private fun testCreateOperators() {
        lifecycleScope.launch {
            (1..3).asFlow()
                .collect { item ->
                    appendLog("【创建操作符 asFlow】onNext: $item")
                }
            appendLog("【创建操作符】collect 完成")
        }
    }

    // ─────────────────────────────────────────────
    // 2. 变换操作符
    // ─────────────────────────────────────────────
    private fun testTransformOperators() {
        lifecycleScope.launch {
            flowOf("apple", "banana")
                .map { it.uppercase() }
                .collect { item ->
                    appendLog("【变换操作符 map】大写转换: $item")
                }
            appendLog("【变换操作符】map 完成")

            flowOf(1, 2)
                .flatMapConcat { value -> flowOf(value, value * 10) }
                .collect { item ->
                    appendLog("【变换操作符 flatMapConcat】展开结果: $item")
                }
            appendLog("【变换操作符】flatMapConcat 完成")

            flowOf(1, 2, 3)
                .buffer()
                .collect { item ->
                    appendLog("【变换操作符 buffer】缓冲接收: $item")
                }
            appendLog("【变换操作符】buffer 完成")
        }
    }

    // ─────────────────────────────────────────────
    // 3. 过滤操作符
    // ─────────────────────────────────────────────
    private fun testFilterOperators() {
        lifecycleScope.launch {
            flowOf(1, 2, 2, 3, 4, 1, 5)
                .distinctUntilChanged()
                .filter { it > 1 }
                .take(3)
                .collect { item ->
                    appendLog("【过滤操作符 distinctUntilChanged+filter+take】接收: $item")
                }
            appendLog("【过滤操作符】collect 完成")
        }
    }

    // ─────────────────────────────────────────────
    // 4. 组合操作符
    // ─────────────────────────────────────────────
    private fun testMergeOperators() {
        lifecycleScope.launch {
            val titles = flowOf("Android", "Kotlin")
            val versions = flowOf("14", "2.0")

            titles.zip(versions) { t, v -> "$t @ $v" }
                .collect { item ->
                    appendLog("【组合操作符 zip】配对结果: $item")
                }
            appendLog("【组合操作符】zip 完成")

            val numbers = flowOf(1, 2, 3)
            val letters = flowOf("a", "b")
            numbers.combine(letters) { n, s -> "$n$s" }
                .collect { item ->
                    appendLog("【组合操作符 combine】最新组合: $item")
                }
            appendLog("【组合操作符】combine 完成")
        }
    }

    // ─────────────────────────────────────────────
    // 5. 错误恢复操作符
    // ─────────────────────────────────────────────
    private fun testErrorOperators() {
        lifecycleScope.launch {
            flow {
                emit(1)
                throw RuntimeException("模拟下游数据异常")
            }
                .catch { error ->
                    appendLog("【错误恢复 catch】捕获异常: ${error.message}")
                    emit(-1)
                }
                .collect { item ->
                    appendLog("【错误恢复 catch】收到数据: $item")
                }
            appendLog("【错误恢复】collect 平稳结束")
        }
    }
}
