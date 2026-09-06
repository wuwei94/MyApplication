package com.example.william.my.module.reactive.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.reactive.samples.FALLBACK_NUMBER_RX
import com.example.william.my.module.reactive.samples.bufferObservable
import com.example.william.my.module.reactive.samples.concatObservable
import com.example.william.my.module.reactive.samples.createObservable
import com.example.william.my.module.reactive.samples.filterObservable
import com.example.william.my.module.reactive.samples.flatMapObservable
import com.example.william.my.module.reactive.samples.mapObservable
import com.example.william.my.module.reactive.samples.onErrorReturnObservable
import com.example.william.my.module.reactive.samples.retryObservable
import com.example.william.my.module.reactive.samples.zipObservable
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable

/**
 * RxJava 3 — 响应式编程框架操作符
 *
 * RxJava 是一个基于观察者模式的异步编程库，提供丰富的操作符处理数据流。
 *
 * 核心特性：
 * 1. 创建操作符：just / range 快速构建数据流
 * 2. 变换操作符：map 逐项映射、flatMap 扁平展开、buffer 背压缓冲
 * 3. 过滤操作符：filter 条件过滤、take 限量、distinct 去重
 * 4. 组合操作符：zip 严格配对、concat 依次连接
 * 5. 错误恢复：onErrorReturn 捕获异常并降级
 * 6. 错误重试：retry 重新订阅上游
 *
 * 操作符管道集中在 `samples` 包，页面只负责线程切换与日志渲染，
 * 同一份管道可被单元测试直接用 Turbine 逐项断言。
 *
 * 本页与同模块 FlowOperatorsActivity 分组一一对应，便于对照学习。
 *
 * https://github.com/ReactiveX/RxJava
 */
@Route(path = RouterPath.Reactive.RxJavaOperators)
class RxJavaOperatorsActivity : BasicResponseActivity() {

    private val compositeDisposable = CompositeDisposable()

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("演示 RxJava 3 核心操作符流式处理（创建 / 变换 / 过滤 / 组合 / 错误恢复 / 重试）")
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "创建操作符（just / range）",
        "变换操作符（map / flatMap / buffer）",
        "过滤操作符（filter / take / distinct）",
        "组合操作符（zip / concat）",
        "错误恢复（onErrorReturn）",
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
        subscribe(createObservable(), "创建操作符 range") { "onNext: $it" }
    }

    // ─────────────────────────────────────────────
    // 2. 变换操作符
    // ─────────────────────────────────────────────
    private fun testTransformOperators() {
        subscribe(mapObservable(Observable.just("apple", "banana")), "变换操作符 map") {
            "大写转换: $it"
        }
        subscribe(flatMapObservable(Observable.just(1, 2)), "变换操作符 flatMap") {
            "展开结果: $it"
        }
        subscribe(bufferObservable(Observable.just(1, 2, 3)), "变换操作符 buffer") {
            "缓冲接收: $it"
        }
    }

    // ─────────────────────────────────────────────
    // 3. 过滤操作符
    // ─────────────────────────────────────────────
    private fun testFilterOperators() {
        subscribe(
            filterObservable(Observable.just(1, 2, 2, 3, 4, 1, 5)),
            "过滤操作符 distinct+filter+take",
        ) { "接收: $it" }
    }

    // ─────────────────────────────────────────────
    // 4. 组合操作符
    // ─────────────────────────────────────────────
    private fun testCombineOperators() {
        subscribe(
            zipObservable(Observable.just("Android", "Kotlin"), Observable.just("14", "2.0")),
            "组合操作符 zip",
        ) { "配对结果: $it" }
        subscribe(
            concatObservable(Observable.just("A", "B"), Observable.just("C", "D")),
            "组合操作符 concat",
        ) { "依次连接: $it" }
    }

    // ─────────────────────────────────────────────
    // 5. 错误恢复操作符
    // ─────────────────────────────────────────────
    private fun testErrorOperators() {
        subscribe(
            onErrorReturnObservable(flakyObservable(), FALLBACK_NUMBER_RX),
            "错误恢复 onErrorReturn",
        ) { "收到数据: $it" }
    }

    // ─────────────────────────────────────────────
    // 6. 错误重试操作符
    // ─────────────────────────────────────────────
    private fun testRetryOperators() {
        subscribe(retryObservable(flakyObservable(), RETRY_MAX_ATTEMPTS), "错误重试 retry") {
            "重新订阅后收到: $it"
        }
    }

    /**
     * 易失败数据源：每次订阅都在发射首项后立即 onError，
     * 用于演示 retry 的重新订阅语义与 onErrorReturn 的降级兜底。
     */
    private fun flakyObservable(): Observable<Int> = Observable.concat(
        Observable.just(1),
        Observable.error<Int>(RuntimeException(ERROR_MESSAGE)),
    )

    /** 统一订阅入口：切回主线程并按 [format] 渲染每一项 */
    private fun <T : Any> subscribe(
        source: Observable<T>,
        tag: String,
        format: (T) -> String,
    ) {
        compositeDisposable.add(
            source
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ item ->
                    appendLog("【$tag】${format(item)}")
                }, { error ->
                    appendLog("【$tag】onError: ${error.message}")
                }, {
                    appendLog("【$tag】onComplete 完成")
                }),
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    private companion object {
        /** 演示中重试的最多尝试次数 */
        const val RETRY_MAX_ATTEMPTS = 2
        const val ERROR_MESSAGE = "模拟下游数据异常"
    }
}
