package com.example.william.my.module.reactive.samples

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.zip

/**
 * Kotlin Flow 操作符示例：每个方法只负责一条操作符管道，返回可被 Turbine 逐项断言的 Flow。
 *
 * 与 [RxJavaOperatorSamples] 分组一一对应，便于对照学习。
 * 这里不感知 Android，也不负责日志渲染，页面只做收集与展示。
 *
 * https://developer.android.google.cn/kotlin/flow
 */

/** 错误恢复的降级值：上游异常被 [catchFlow] 捕获后改发该值，保证下游平稳结束 */
const val FALLBACK_NUMBER = -1

/**
 * 创建操作符：由区间构建冷流，订阅时才开始发射。
 *
 * 与 [flowOf] 的区别是 [asFlow] 直接把已有集合/序列转成流，省去逐个 [flowOf] 入参。
 */
fun createFlow(): Flow<Int> = (1..3).asFlow()

/** 变换操作符 map：逐项映射，输入输出一一对齐 */
fun mapFlow(source: Flow<String>): Flow<String> = source.map { it.uppercase() }

/**
 * 变换操作符 flatMapConcat：把每个元素展开成内层流并按顺序拼接。
 *
 * 与 flatMapMerge 的区别是 concat 严格保序，内层流不会并发。
 */
fun flatMapConcatFlow(source: Flow<Int>): Flow<Int> = source.flatMapConcat { value -> flowOf(value, value * 10) }

/**
 * 变换操作符 buffer：让发射与收集在不同协程并发运行，形成缓冲队列缓解背压。
 *
 * 注意 buffer 不改变发射内容，只改变上下游的并发关系。
 */
fun bufferFlow(source: Flow<Int>): Flow<Int> = source.buffer()

/**
 * 过滤操作符：[distinctUntilChanged] 相邻去重、[filter] 条件过滤、[take] 限量后取消上游。
 *
 * 三者顺序敏感：先去重再过滤能减少下游判断次数，[take] 放最后才能在取够数量时立刻取消。
 */
fun filterFlow(source: Flow<Int>): Flow<Int> = source.distinctUntilChanged().filter { it > 1 }.take(3)

/**
 * 组合操作符 zip：严格按下标一对一配对，任一方结束即整体结束，长的一方剩余项被丢弃。
 */
fun zipFlow(titles: Flow<String>, versions: Flow<String>): Flow<String> = titles.zip(versions) { title, version -> "$title @ $version" }

/**
 * 组合操作符 combine：任一方发射即用双方**当前最新值**重新组合，因此发射次数可能多于任一方。
 */
fun combineFlow(numbers: Flow<Int>, letters: Flow<String>): Flow<String> = numbers.combine(letters) { number, letter -> "$number$letter" }

/**
 * 错误恢复 catch：捕获**上游**异常并降级发射 [fallback]，下游不会收到异常。
 *
 * catch 只作用于其上游；若在 catch 之后的 collect 中抛异常，catch 无法拦截。
 */
fun catchFlow(source: NumberSource, fallback: Int = FALLBACK_NUMBER): Flow<Int> = source.numbers().catch { emit(fallback) }

/**
 * 错误重试 retry：上游失败时重新订阅，最多重试 [maxAttempts] 次（即最多订阅 `maxAttempts + 1` 次）。
 *
 * 重试次数用尽后异常照常抛给下游，需要由 [catchFlow] 之类的兜底操作符或收集方处理。
 * 重试会重新执行上游，因此上游必须是可重复订阅的冷流且副作用幂等。
 */
fun retryFlow(source: NumberSource, maxAttempts: Int): Flow<Int> = source.numbers().retryWhen { _, attempt -> attempt < maxAttempts }
