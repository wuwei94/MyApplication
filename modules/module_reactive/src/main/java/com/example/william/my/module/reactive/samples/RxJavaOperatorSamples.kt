package com.example.william.my.module.reactive.samples

import io.reactivex.rxjava3.core.Observable

/**
 * RxJava 3 操作符示例：每个方法只负责一条操作符管道，返回可被 Turbine 逐项断言的 Observable。
 *
 * 与 [FlowOperatorSamples] 分组一一对应，便于对照学习。
 * 这里不感知 Android 与线程调度，页面只负责 subscribeOn / observeOn 与日志渲染。
 *
 * https://github.com/ReactiveX/RxJava
 */

/** 错误恢复的降级值：上游异常被 [onErrorReturnObservable] 转成该值后正常 onComplete */
const val FALLBACK_NUMBER_RX = -1

/** 创建操作符：按区间构建数据流 */
fun createObservable(): Observable<Int> = Observable.range(1, 3)

/** 变换操作符 map：逐项映射 */
fun mapObservable(source: Observable<String>): Observable<String> = source.map { it.uppercase() }

/**
 * 变换操作符 flatMap：把每个元素展开成内层 Observable 并合并发射。
 *
 * 与 Flow 的 flatMapConcat 不同，flatMap 默认**不保序**（内层并发合并）。
 */
fun flatMapObservable(source: Observable<Int>): Observable<Int> = source.flatMap { value -> Observable.just(value, value * 10) }

/**
 * 变换操作符 buffer：按固定数量打包成列表发射。
 *
 * 与 Flow 的 buffer 不同，RxJava 的 buffer 会改变发射类型（单值 → 列表）。
 */
fun bufferObservable(source: Observable<Int>): Observable<List<Int>> = source.buffer(2)

/** 过滤操作符：distinct 全局去重、filter 条件过滤、take 限量 */
fun filterObservable(source: Observable<Int>): Observable<Int> = source.distinct().filter { it > 1 }.take(3)

/** 组合操作符 zip：严格按下标一对一配对，任一方结束即整体结束 */
fun zipObservable(titles: Observable<String>, versions: Observable<String>): Observable<String> = Observable.zip(titles, versions) { title, version -> "$title @ $version" }

/** 组合操作符 concat：第一个流 onComplete 后才开始订阅第二个流 */
fun concatObservable(first: Observable<String>, second: Observable<String>): Observable<String> = Observable.concat(first, second)

/** 错误恢复 onErrorReturn：把 onError 转成一次正常发射并立即 onComplete */
fun <T : Any> onErrorReturnObservable(source: Observable<T>, fallback: T): Observable<T> = source.onErrorReturn { fallback }

/**
 * 错误重试 retry：上游失败时重新订阅，最多重试 [maxAttempts] 次。
 *
 * 与 [retryFlow] 一样依赖上游可重复订阅；次数用尽后异常照常传给 onError。
 */
fun retryObservable(source: Observable<Int>, maxAttempts: Int): Observable<Int> = source.retry(maxAttempts.toLong())
