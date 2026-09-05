package com.example.william.my.module.event.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.eventbus.rxjava.RxEventBus
import com.example.william.my.module.event.event.GlobalEvent
import com.example.william.my.module.event.event.StickyEvent
import io.reactivex.rxjava3.disposables.CompositeDisposable

/**
 * RxJava 事件总线 — 基于 RxJava 的事件总线
 *
 * 利用 RxJava 的 PublishSubject 实现的事件总线，支持响应式编程。
 *
 * 核心特性：
 * 1. 响应式编程：基于 RxJava，支持丰富的操作符
 * 2. 线程调度：支持灵活的线程切换
 * 3. 粘性事件：支持粘性事件，使用 BehaviorSubject
 * 4. 背压处理：支持背压策略，防止数据溢出
 *
 * 特性对比：
 * 延迟发送: ❌ | 有序接收: ✅ | Sticky: ✅ | 生命周期感知: ❌ | 可跨进程: ❌ | 线程分发: ✅
 *
 * 基本用法：
 * ```kotlin
 * // 订阅事件
 * val disposable = RxEventBus.observeEvent(MessageEvent::class.java)
 *     .subscribe { event ->
 *         // 处理事件
 *     }
 *
 * // 发送事件
 * RxEventBus.postEvent(MessageEvent("Hello"))
 *
 * // 取消订阅
 * disposable.dispose()
 * ```
 *
 * 注意：RxEventBus 不具备生命周期感知能力，必须通过 CompositeDisposable 在 onDestroy 中解绑，防止内存泄漏。
 *
 * https://github.com/ReactiveX/RxJava
 */
@Route(path = RouterPath.Event.RxEventBus)
class RxEventBusActivity : BasicResponseActivity() {

    private val mDisposable = CompositeDisposable()
    private var isObserving = false

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("RxEventBus 示例\n\n请点击下方按钮注册监听或发送事件")
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "注册/注销监听 (Observe/Unobserve)",
        "发送普通事件 (Post Event)",
        "发送粘性事件 (Post Sticky Event)",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> toggleObserve()
            1 -> {
                appendLog("发送普通事件：GlobalEvent")
                RxEventBus.postEvent(
                    GlobalEvent("RxEventBus post by Activity"),
                )
            }
            2 -> {
                appendLog("发送粘性事件：StickyEvent")
                RxEventBus.postStickyEvent(
                    StickyEvent("RxEventBus postSticky by Activity"),
                )
            }
        }
    }

    private fun toggleObserve() {
        if (!isObserving) {
            mDisposable.clear()
            mDisposable.add(
                RxEventBus.observeEvent(GlobalEvent::class.java).subscribe {
                    appendLog("收到普通事件：${it.message}")
                },
            )
            mDisposable.add(
                RxEventBus.observeEvent(StickyEvent::class.java).subscribe {
                    appendLog("收到粘性事件：${it.message}")
                },
            )
            isObserving = true
            appendLog("已开启 RxEventBus 监听")
        } else {
            mDisposable.clear()
            isObserving = false
            appendLog("已取消 RxEventBus 监听")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mDisposable.clear()
    }
}
