package com.example.william.my.module.opensource.activity.utils

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable

/**
 * RxJava 3 核心响应式操作符演示
 *
 * 覆盖：
 * 1. 创建操作符：`just`、`fromArray`、`range`。
 * 2. 变换操作符：`map`、`flatMap`、`buffer`。
 * 3. 过滤操作符：`filter`、`take`、`distinct`。
 * 4. 组合操作符：`concat`、`merge`、`zip`。
 * 5. 错误处理操作符：`onErrorReturn`、`onErrorResumeNext`。
 *
 * 页面退出时通过 `CompositeDisposable` 统一取消订阅，防止内存泄漏。
 */
@Route(path = RouterPath.OpenSource.RxJava)
class RxJavaActivity : BasicResponseActivity() {

    private val compositeDisposable = CompositeDisposable()

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("演示 RxJava 3 核心操作符流式处理（创建 / 变换 / 过滤 / 组合 / 错误恢复）")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "创建操作符（just / range）",
            "变换操作符（map / flatMap / buffer）",
            "过滤操作符（filter / take / distinct）",
            "组合操作符（zip / concat）",
            "错误恢复（onErrorReturn）"
        )
    }

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
        val disposable = Observable.range(1, 3)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ item ->
                appendLog("【创建操作符 range】onNext: $item")
            }, { error ->
                appendLog("【创建操作符】onError: ${error.message}")
            }, {
                appendLog("【创建操作符】onComplete 完成")
            })
        compositeDisposable.add(disposable)
    }

    // ─────────────────────────────────────────────
    // 2. 变换操作符
    // ─────────────────────────────────────────────
    private fun testTransformOperators() {
        val disposable = Observable.just("apple", "banana")
            .map { it.uppercase() }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ item ->
                appendLog("【变换操作符 map】大写转换: $item")
            }, { error ->
                appendLog("【变换操作符】onError: ${error.message}")
            }, {
                appendLog("【变换操作符】onComplete 完成")
            })
        compositeDisposable.add(disposable)
    }

    // ─────────────────────────────────────────────
    // 3. 过滤操作符
    // ─────────────────────────────────────────────
    private fun testFilterOperators() {
        val disposable = Observable.just(1, 2, 2, 3, 4, 1, 5)
            .distinct()
            .filter { it > 1 }
            .take(3)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ item ->
                appendLog("【过滤操作符 distinct+filter+take】接收: $item")
            }, { error ->
                appendLog("【过滤操作符】onError: ${error.message}")
            }, {
                appendLog("【过滤操作符】onComplete 完成")
            })
        compositeDisposable.add(disposable)
    }

    // ─────────────────────────────────────────────
    // 4. 组合操作符
    // ─────────────────────────────────────────────
    private fun testMergeOperators() {
        val titles = Observable.just("Android", "Kotlin")
        val versions = Observable.just("14", "2.0")

        val disposable = Observable.zip(titles, versions) { t, v -> "$t @ $v" }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ item ->
                appendLog("【组合操作符 zip】配对结果: $item")
            }, { error ->
                appendLog("【组合操作符】onError: ${error.message}")
            }, {
                appendLog("【组合操作符】onComplete 完成")
            })
        compositeDisposable.add(disposable)
    }

    // ─────────────────────────────────────────────
    // 5. 错误恢复操作符
    // ─────────────────────────────────────────────
    private fun testErrorOperators() {
        val disposable = Observable.error<String>(RuntimeException("模拟下游数据异常"))
            .onErrorReturn { "【降级默认数据】" }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ item ->
                appendLog("【错误恢复 onErrorReturn】收到数据: $item")
            }, { error ->
                appendLog("【错误恢复】onError: ${error.message}")
            }, {
                appendLog("【错误恢复】onComplete 流程平稳结束")
            })
        compositeDisposable.add(disposable)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }
}
