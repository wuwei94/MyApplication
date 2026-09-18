package com.example.william.my.module.event.rxeventbus.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.eventbus.rxjava.RxEventBus
import com.example.william.my.module.event.event.GlobalEvent
import com.example.william.my.module.event.event.StickyEvent
import io.reactivex.rxjava3.disposables.CompositeDisposable

/**
 * RxEventBus — 基于 RxJava Subject 的进程内事件总线
 *
 * 核心机制与避坑点：
 * 1. 订阅模型：PublishSubject 推送普通事件，BehaviorSubject 保留最近一条供粘性订阅；post 仅对已 subscribe 的观察者生效
 * 2. 线程调度：subscribeOn / observeOn 在订阅链上自行切换线程，事件总线本身不绑定主线程
 * 3. 资源释放：不具备生命周期感知，必须将 Disposable 汇入 CompositeDisposable 并在 onDestroy clear，防止订阅者泄漏
 * 4. 背压：高频事件场景需在链上配置背压策略（onBackpressure*），避免 MissingBackpressureException
 *
 * 官方参考：
 * https://github.com/ReactiveX/RxJava
 */
@Route(path = RouterPath.Event.RxEventBus)
class RxEventBusActivity : BasicResponseActivity() {

    private val disposables = CompositeDisposable()
    private var isObserving = false

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription(
            "RxEventBus 示例：开启监听 / 手动注销 / 普通事件 / 粘性事件 / 生命周期约束",
        )
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 开启监听 (Observe)",
        "2. 手动注销监听 (Dispose)",
        "3. 发送普通事件 (Post Event)",
        "4. 发送粘性事件 (Post Sticky Event)",
        "5. 生命周期绑定说明 (Lifecycle)",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> observeRxEventBus()
            1 -> unobserveRxEventBus()
            2 -> postGlobalEvent()
            3 -> postSticky()
            4 -> showLifecyclePolicy()
        }
    }

    private fun observeRxEventBus() {
        if (isObserving) {
            appendLog("RxEventBus 已处于监听状态")
            return
        }
        disposables.clear()
        disposables.add(
            RxEventBus.observeEvent(GlobalEvent::class.java).subscribe {
                appendLog("收到普通事件：${it.message}")
            },
        )
        disposables.add(
            RxEventBus.observeEvent(StickyEvent::class.java).subscribe {
                appendLog("收到粘性事件：${it.message}")
            },
        )
        isObserving = true
        appendLog("已开启 RxEventBus 监听")
    }

    private fun unobserveRxEventBus() {
        if (!isObserving) {
            appendLog("RxEventBus 当前未监听")
            return
        }
        disposables.clear()
        isObserving = false
        appendLog("已取消 RxEventBus 监听")
    }

    private fun postGlobalEvent() {
        appendLog("发送普通事件：GlobalEvent")
        RxEventBus.postEvent(
            GlobalEvent("RxEventBus post by Activity"),
        )
    }

    private fun postSticky() {
        appendLog("发送粘性事件：StickyEvent")
        RxEventBus.postStickyEvent(
            StickyEvent("RxEventBus postSticky by Activity"),
        )
    }

    private fun showLifecyclePolicy() {
        appendLog("RxEventBus 不绑定 LifecycleOwner，onDestroy 执行 disposables.clear() 保底")
        appendLog("当前监听状态：$isObserving")
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
        isObserving = false
    }
}
